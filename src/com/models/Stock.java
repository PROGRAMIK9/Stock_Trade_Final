package com.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Stock model with historical price data
 */
public class Stock {
    private String symbol;
    private String name;
    private double currentPrice;
    private double openPrice;
    private double highPrice;
    private double lowPrice;
    private long volume;
    private List<Double> historicalPrices;
    private String prediction; // UP, DOWN, or NEUTRAL
    
    public Stock(String symbol, String name, double currentPrice) {
        this.symbol = symbol;
        this.name = name;
        this.currentPrice = currentPrice;
        this.historicalPrices = new ArrayList<>();
    }
    
    public Stock(String symbol, double currentPrice) {
        this(symbol, symbol, currentPrice);
    }
    
    // Getters and setters
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }
    public double getOpenPrice() { return openPrice; }
    public void setOpenPrice(double openPrice) { this.openPrice = openPrice; }
    public double getHighPrice() { return highPrice; }
    public void setHighPrice(double highPrice) { this.highPrice = highPrice; }
    public double getLowPrice() { return lowPrice; }
    public void setLowPrice(double lowPrice) { this.lowPrice = lowPrice; }
    public long getVolume() { return volume; }
    public void setVolume(long volume) { this.volume = volume; }
    public List<Double> getHistoricalPrices() { return historicalPrices; }
    public void setHistoricalPrices(List<Double> prices) { this.historicalPrices = prices; }
    public void addHistoricalPrice(double price) { this.historicalPrices.add(price); }
    public String getPrediction() { return prediction; }
    public void setPrediction(String prediction) { this.prediction = prediction; }
    
    @Override
    public String toString() {
        return symbol + " - $" + String.format("%.2f", currentPrice) + 
               (prediction != null ? " [" + prediction + "]" : "");
    }
}
