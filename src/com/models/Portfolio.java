package com.models;

import java.util.*;

/**
 * Portfolio class demonstrating Collections and Generics
 */
public class Portfolio {
    private int id;
    private double cashBalance;
    private Map<String, StockHolding> holdings; // Using Map with generics
    private List<Transaction> transactionHistory; // Using List with generics
    
    public Portfolio(double initialCash) {
        this.cashBalance = initialCash;
        this.holdings = new HashMap<>();
        this.transactionHistory = new ArrayList<>();
    }
    
    public Portfolio(int id, double cashBalance) {
        this.id = id;
        this.cashBalance = cashBalance;
        this.holdings = new HashMap<>();
        this.transactionHistory = new ArrayList<>();
    }
    
    public void addHolding(String symbol, int quantity, double price) {
        if (holdings.containsKey(symbol)) {
            StockHolding holding = holdings.get(symbol);
            holding.addQuantity(quantity, price);
        } else {
            holdings.put(symbol, new StockHolding(symbol, quantity, price));
        }
    }
    
    public boolean removeHolding(String symbol, int quantity) {
        if (!holdings.containsKey(symbol)) {
            return false;
        }
        StockHolding holding = holdings.get(symbol);
        if (holding.getQuantity() < quantity) {
            return false;
        }
        holding.removeQuantity(quantity);
        if (holding.getQuantity() == 0) {
            holdings.remove(symbol);
        }
        return true;
    }
    
    public double getTotalValue(Map<String, Double> currentPrices) {
        double stockValue = 0.0;
        for (StockHolding holding : holdings.values()) {
            Double price = currentPrices.get(holding.getSymbol());
            if (price != null) {
                stockValue += holding.getQuantity() * price;
            }
        }
        return cashBalance + stockValue;
    }
    
    public void addTransaction(Transaction transaction) {
        transactionHistory.add(transaction);
    }
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public double getCashBalance() { return cashBalance; }
    public void setCashBalance(double cashBalance) { this.cashBalance = cashBalance; }
    public Map<String, StockHolding> getHoldings() { return holdings; }
    public List<Transaction> getTransactionHistory() { return transactionHistory; }
    
    /**
     * Inner class representing a stock holding
     */
    public static class StockHolding {
        private String symbol;
        private int quantity;
        private double averagePrice;
        
        public StockHolding(String symbol, int quantity, double price) {
            this.symbol = symbol;
            this.quantity = quantity;
            this.averagePrice = price;
        }
        
        public void addQuantity(int qty, double price) {
            double totalValue = (quantity * averagePrice) + (qty * price);
            quantity += qty;
            averagePrice = totalValue / quantity;
        }
        
        public void removeQuantity(int qty) {
            quantity -= qty;
        }
        
        public String getSymbol() { return symbol; }
        public int getQuantity() { return quantity; }
        public double getAveragePrice() { return averagePrice; }
        
        @Override
        public String toString() {
            return symbol + ": " + quantity + " shares @ $" + String.format("%.2f", averagePrice);
        }
    }
}
