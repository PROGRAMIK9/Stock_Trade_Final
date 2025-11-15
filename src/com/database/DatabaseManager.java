package com.database;

import java.sql.*;


public class DatabaseManager {
private static final String DB_URL = "jdbc:postgresql://localhost:5432/stock";
    
    // 2. Add your username and password
    private static final String DB_USER = "postgres"; // Often "postgres"
    private static final String DB_PASSWORD = "openaudit@123";

    private Connection con;

    public DatabaseManager() {
        try {
            // 3. Load the PostgreSQL driver
            Class.forName("org.postgresql.Driver");
            
            // 4. Connect using the URL, user, and password
            con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            
            System.out.println("Connection successful!");

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
}

