package com.database;

import com.models.*;
import java.sql.*;
import java.time.LocalDateTime;

public class DatabaseManager {
private static final String DB_URL = "jdbc:postgresql://localhost:5432/stock";
    
    private static final String DB_USER = "postgres"; // Often "postgres"
    private static final String DB_PASSWORD = "openaudit@123";

    private Connection con;

    public DatabaseManager() {
        try {
            Class.forName("org.postgresql.Driver");
            con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Connection successful!");
            initializeTables();
        } catch (Exception e) {
            System.err.println("Connection failed!");
            // Print the full error to see what went wrong
            e.printStackTrace(); 
        }
    }
    
    // A helper method so other classes can get the connection
    public Connection getConnection() {
        return this.con;
    }
    
    public void  initializeTables() throws SQLException {
    	Statement stat = con.createStatement();
    	stat.execute("CREATE TABLE IF NOT EXISTS users("+
    	"id SERIAL PRIMARY KEY,"+
    	"username VARCHAR(50) NOT NULL,"+
    	"email VARCHAR(100) NOT NULL,"+
    	"password VARCHAR(100) NOT NULL)");
    	
    	stat.execute("CREATE TABLE IF NOT EXISTS portfolio("
    			+ "id SERIAL PRIMARY KEY,"
    			+ "user_id INTEGER NOT NULL,"
    			+ "cash_acc FLOAT NOT NULL, "
    		  	+ "FOREIGN KEY (user_id) REFERENCES users (id))");
    	
    	stat.execute("CREATE TABLE IF NOT EXISTS stock("
    			+ "id SERIAL PRIMARY KEY,"
    			+ "portfolio_id INTEGER NOT NULL,"
    			+ "symbl VARCHAR(20) NOT NULL,"
    			+ "price FLOAT NOT NULL,"
    			+ "qty INTEGER NOT NULL,"
    			+ "avg_price FLOAT NOT NULL,"
    			+ "FOREIGN KEY(portfolio_id) REFERENCES portfolio(id))");
    
    	stat.execute("CREATE TABLE IF NOT EXISTS holdings("
    			+ "id SERIAL PRIMARY KEY,"
    			+ "portfolio_id INTEGER NOT NULL,"
    			+ "symbl VARCHAR(20) NOT NULL,"
    			+ "qty INTEGER NOT NULL,"
    			+ "avg_price FLOAT NOT NULL,"
    			+ "FOREIGN KEY (portfolio_id) REFERENCES portfolio(id))");
    	
    	stat.execute("CREATE TABLE IF NOT EXISTS transactions("
    			+ "id SERIAL PRIMARY KEY,"
    			+ "portfolio_id INTEGER NOT NULL,"
    			+ "symbl VARCHAR(20) NOT NULL,"
    			+ "type VARCHAR(10) NOT NULL,"
    			+ "qty INTEGER NOT NULL,"
    			+ "price FLOAT NOT NULL,"
    			+ "timestamp TIMESTAMPTZ NOT NULL,"
    			+ "FOREIGN KEY (portfolio_id) REFERENCES portfolio(id))");
    	
    	stat.close();
    }
    
    public int createUser(User user) throws SQLException{
    	String psql = "INSERT INTO users(username, email, password) VALUES (?,?,?)";
    	PreparedStatement stmt = con.prepareStatement(psql, Statement.RETURN_GENERATED_KEYS);
    	stmt.setString(1, user.getUsername());
    	stmt.setString(2, user.getEmail());
    	stmt.setString(3, user.getPassword());
    	stmt.executeUpdate();
    	
    	ResultSet rs = stmt.getGeneratedKeys();
    	int userId = rs.next() ?rs.getInt(1) : -1;
    	stmt.close();
    	if(userId>0) createPortfolio(userId,10000.0);
    	return userId;
    }
    
    public User getUserByUsername(String username) throws SQLException{
    	String psql = "SELECT * FROM users WHERE username = ?";
    	PreparedStatement stmt =  con.prepareStatement(psql);
    	stmt.setString(1,username);
    	ResultSet rs = stmt.executeQuery();
    	User user = null;
    	if(rs.next()) {
    		user = new User(
    				rs.getInt("id"),
    				rs.getString("username"),
    				rs.getString("email"),
    				rs.getString("password")
    		);
    	}
    	stmt.close();
    	return user;
    }
    
    public int createPortfolio(int userId, double amount) throws SQLException {
    	String psql = "INSERT INTO portfolio (user_id, cash_acc) VALUES (?,?)";
    	PreparedStatement stmt = con.prepareStatement(psql, Statement.RETURN_GENERATED_KEYS);
    	stmt.setInt(1, userId);
    	stmt.setDouble(2,amount);
    	stmt.executeUpdate();
    	
    	ResultSet rs = stmt.getGeneratedKeys();
    	int portfolioId = rs.next()? rs.getInt(1):-1;
    	stmt.close();
    	return portfolioId;
    }
    
    public Portfolio getPortfolioByUserId(int userId) throws SQLException{
    	String psql = "SELECT * FROM portfolio WHERE user_id = ?";
    	PreparedStatement stmt = con.prepareStatement(psql);
    	stmt.setInt(1, userId);
    	ResultSet rs = stmt.executeQuery();
    	Portfolio pf = null;
    	if(rs.next()) {
    		pf = new Portfolio(rs.getInt("id"), rs.getDouble("cash_acc"));
    		loadHoldings(pf);
    		loadTransactions(pf);
    	}
    	
    	stmt.close();
    	return pf;
    }
    
    public void updatePortfolio(int portfolioId, double amount) throws SQLException{
    	String psql = "UPDATE portfolio SET cash_acc = ? WHERE id =?";
    	PreparedStatement stmt = con.prepareStatement(psql);
    	stmt.setDouble(1,amount);
    	stmt.setInt(2, portfolioId);
    	stmt.executeUpdate();
    	stmt.close();
    }
    
    public void loadHoldings(Portfolio portfolio) throws SQLException{
    	String psql = "SELECT * from holdings WHERE portfolio_id = ?";
    	PreparedStatement stmt = con.prepareStatement(psql);
    	stmt.setInt(1, portfolio.getId());
    	ResultSet rs = stmt.executeQuery();
    	
    	while(rs.next()) {
    		portfolio.addHolding(
    				rs.getString("symbl"),
    				rs.getInt("qty"),
    				rs.getDouble("avg_price")
    		);
    	}
    	stmt.close();
    }
    
    public void saveHolding(int portfolioId, String symbol, int quantity, double price)throws SQLException{
    	String psql = "SELECT * FROM holdings where portfolio_id = ? AND symbl = ?";
    	PreparedStatement ps = con.prepareStatement(psql);
    	ps.setInt(1, portfolioId);
    	ps.setString(2,  symbol);
    	ResultSet rs = ps.executeQuery();
    	if(rs.next()) {
    		int existingQty = rs.getInt("qty");
    		double existingAvg = rs.getDouble("avg_price");
    		int newQty = existingQty + quantity;
    		double newPrice = ((existingQty * existingAvg)+ (newQty * price))/newQty;
    		psql = "UPDATE holdings SET qty=?, avg_price = ? WHERE portfolio_id = ? AND symbl = ?";
    		PreparedStatement updateStmt = con.prepareStatement(psql);
            updateStmt.setInt(1, newQty);
            updateStmt.setDouble(2, newPrice);
            updateStmt.setInt(3, portfolioId);
            updateStmt.setString(4, symbol);
            updateStmt.executeUpdate();
            updateStmt.close();		
    	}else {
    		psql = "INSERT INTO holdings(portfolio_id, symbl, qty, avg_price) VALUES (?,?,?,?)";
    		PreparedStatement insertStmt = con.prepareStatement(psql);
            insertStmt.setInt(1, portfolioId);
            insertStmt.setString(2, symbol);
            insertStmt.setInt(3, quantity);
            insertStmt.setDouble(4, price);
            insertStmt.executeUpdate();
            insertStmt.close();
    	}
    	ps.close();
    }
    
    public void updateHolding(int portfolioId, String symbol, int newQuantity) throws SQLException {
        if (newQuantity <= 0) {
            String deleteSql = "DELETE FROM holdings WHERE portfolio_id = ? AND symbl = ?";
            PreparedStatement pstmt = con.prepareStatement(deleteSql);
            pstmt.setInt(1, portfolioId);
            pstmt.setString(2, symbol);
            pstmt.executeUpdate();
            pstmt.close();
        } else {
            String updateSql = "UPDATE holdings SET qty = ? WHERE portfolio_id = ? AND symbl = ?";
            PreparedStatement pstmt = con.prepareStatement(updateSql);
            pstmt.setInt(1, newQuantity);
            pstmt.setInt(2, portfolioId);
            pstmt.setString(3, symbol);
            pstmt.executeUpdate();
            pstmt.close();
        }
    }
    
    public void saveTransactions(int portfolioId, Transaction transaction) throws SQLException{
    	String sql = "INSERT INTO transactions (portfolio_id, type, symbl, qty, price,timestamp) VALUES (?,?,?,?,?,?)";
    	PreparedStatement stmt = con.prepareStatement(sql);
    	stmt.setInt(1, portfolioId);
    	stmt.setString(2,  transaction.getType());
    	stmt.setString(3, transaction.getSymbol());
    	stmt.setInt(4, transaction.getQuantity());
    	stmt.setDouble(5,  transaction.getPrice());
    	stmt.setTimestamp(6, transaction.getTimestamp());
    	stmt.executeUpdate();
    	stmt.close();
    }
    
    public void loadTransactions(Portfolio portfolio) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE portfolio_id = ? ORDER BY timestamp DESC LIMIT 50";
        PreparedStatement pstmt = con.prepareStatement(sql);
        pstmt.setInt(1, portfolio.getId());
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            Transaction transaction = new Transaction(
                rs.getInt("id"),
                rs.getString("type"),
                rs.getString("symbl"),
                rs.getInt("qty"),
                rs.getDouble("price"),
                rs.getTimestamp("timestamp")
            );
            portfolio.addTransaction(transaction);
        }
        
        pstmt.close();
    }
    
    public void close() {
    	try {
    		if(con!=null && !con.isClosed()) {
    			con.close();
    		}
    	}catch(SQLException e) {
    		System.err.println("Error closing document: " + e.getMessage());
    	}
    }
}

