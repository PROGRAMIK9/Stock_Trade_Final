package com.services;

import com.interfaces.PortfolioService;
import com.models.*;
import com.database.DatabaseManager;
import java.sql.SQLException;
import java.util.*;

/**
 * Portfolio management service implementing PortfolioService interface
 */
public class PortfolioManagementService implements PortfolioService {
    private DatabaseManager dbManager;
    
    public PortfolioManagementService(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }
    
    @Override
    public boolean buyStock(Portfolio portfolio, Stock stock, int quantity) {
        double totalCost = stock.getCurrentPrice() * quantity;
        
        if (portfolio.getCashBalance() < totalCost) {
            System.err.println("Insufficient funds");
            return false;
        }
        
        try {
            // Update portfolio
            portfolio.setCashBalance(portfolio.getCashBalance() - totalCost);
            portfolio.addHolding(stock.getSymbol(), quantity, stock.getCurrentPrice());
            
            // Create transaction
            Transaction transaction = new Transaction("BUY", stock.getSymbol(), quantity, stock.getCurrentPrice());
            portfolio.addTransaction(transaction);
            
            // Save to database
            dbManager.updatePortfolio(portfolio.getId(), portfolio.getCashBalance());
            dbManager.saveHolding(portfolio.getId(), stock.getSymbol(), quantity, stock.getCurrentPrice());
            dbManager.saveTransactions(portfolio.getId(), transaction);
            
            return true;
        } catch (SQLException e) {
            System.err.println("Error buying stock: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean sellStock(Portfolio portfolio, String symbol, int quantity) {
        Portfolio.StockHolding holding = portfolio.getHoldings().get(symbol);
        
        if (holding == null || holding.getQuantity() < quantity) {
            System.err.println("Insufficient shares");
            return false;
        }
        
        try {
            // For selling, we need current price - assuming we have it stored or fetched
            double currentPrice = holding.getAveragePrice(); // In real app, fetch current price
            double totalRevenue = currentPrice * quantity;
            
            // Update portfolio
            portfolio.setCashBalance(portfolio.getCashBalance() + totalRevenue);
            portfolio.removeHolding(symbol, quantity);
            
            // Create transaction
            Transaction transaction = new Transaction("SELL", symbol, quantity, currentPrice);
            portfolio.addTransaction(transaction);
            
            // Save to database
            dbManager.updatePortfolio(portfolio.getId(), portfolio.getCashBalance());
            
            Portfolio.StockHolding updatedHolding = portfolio.getHoldings().get(symbol);
            int remainingQty = updatedHolding != null ? updatedHolding.getQuantity() : 0;
            dbManager.updateHolding(portfolio.getId(), symbol, remainingQty);
            dbManager.saveTransactions(portfolio.getId(), transaction);
            
            return true;
        } catch (SQLException e) {
            System.err.println("Error selling stock: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public double getPortfolioValue(Portfolio portfolio) {
        // This would need current stock prices - simplified for demo
        double totalValue = portfolio.getCashBalance();
        
        for (Portfolio.StockHolding holding : portfolio.getHoldings().values()) {
            totalValue += holding.getQuantity() * holding.getAveragePrice();
        }
        
        return totalValue;
    }
    
    @Override
    public List<Stock> getHoldings(Portfolio portfolio) {
        List<Stock> stocks = new ArrayList<>();
        
        for (Portfolio.StockHolding holding : portfolio.getHoldings().values()) {
            Stock stock = new Stock(holding.getSymbol(), holding.getAveragePrice());
            stocks.add(stock);
        }
        
        return stocks;
    }
}
