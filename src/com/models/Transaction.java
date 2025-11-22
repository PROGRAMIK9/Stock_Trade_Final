package com.models;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Transaction model representing buy/sell operations
 */
public class Transaction {
    private int id;
    private String type; // BUY or SELL
    private String symbol;
    private int quantity;
    private double price;
    private Timestamp timestamp;
    
    public Transaction(String type, String symbol, int quantity, double price) {
        this.type = type;
        this.symbol = symbol;
        this.quantity = quantity;
        this.price = price;
        this.timestamp = Timestamp.valueOf(LocalDateTime.now());
    }
    
    public Transaction(int id, String type, String symbol, int quantity, double price, Timestamp timestamp) {
        this.id = id;
        this.type = type;
        this.symbol = symbol;
        this.quantity = quantity;
        this.price = price;
        this.timestamp = timestamp;
    }
    
    public double getTotalAmount() {
        return quantity * price;
    }
    
    // Getters
    public int getId() { return id; }
    public String getType() { return type; }
    public String getSymbol() { return symbol; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public Timestamp getTimestamp() { return timestamp; }
    
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return type + " " + quantity + " " + symbol + " @ $" + 
               String.format("%.2f", price) + " (" + timestamp+")";
    }
}
