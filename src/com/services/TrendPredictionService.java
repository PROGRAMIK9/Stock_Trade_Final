package com.services;

import com.interfaces.StockPredictor;
import com.models.Stock;
import java.util.List;

/**
 * Stock prediction service implementing interface
 * Uses simple moving average and trend analysis
 */
public class TrendPredictionService implements StockPredictor {
    
    private static final int SHORT_TERM_PERIOD = 5;
    private static final int LONG_TERM_PERIOD = 10;
    
    @Override
    public String predictTrend(List<Double> historicalPrices) {
        if (historicalPrices == null || historicalPrices.size() < LONG_TERM_PERIOD) {
            return "NEUTRAL";
        }
        
        double shortMA = calculateMovingAverage(historicalPrices, SHORT_TERM_PERIOD);
        double longMA = calculateMovingAverage(historicalPrices, LONG_TERM_PERIOD);
        
        // Golden cross / Death cross strategy
        if (shortMA > longMA * 1.02) {
            return "UP";
        } else if (shortMA < longMA * 0.98) {
            return "DOWN";
        }
        
        return "NEUTRAL";
    }
    
    @Override
    public double predictNextPrice(Stock stock) {
        List<Double> prices = stock.getHistoricalPrices();
        if (prices == null || prices.isEmpty()) {
            return stock.getCurrentPrice();
        }
        
        double shortMA = calculateMovingAverage(prices, SHORT_TERM_PERIOD);
        double momentum = calculateMomentum(prices);
        
        // Simple prediction based on trend continuation
        return stock.getCurrentPrice() * (1 + momentum);
    }
    
    @Override
    public double getConfidenceScore(List<Double> prices) {
        if (prices == null || prices.size() < 10) {
            return 0.5; // Low confidence
        }
        
        // Calculate confidence based on trend consistency
        int consecutiveMoves = 0;
        boolean isUptrend = prices.get(prices.size() - 1) > prices.get(prices.size() - 2);
        
        for (int i = prices.size() - 1; i > 0 && i > prices.size() - 10; i--) {
            boolean currentMove = prices.get(i) > prices.get(i - 1);
            if (currentMove == isUptrend) {
                consecutiveMoves++;
            } else {
                break;
            }
        }
        
        // Confidence increases with consistent trend
        return Math.min(0.5 + (consecutiveMoves * 0.08), 0.95);
    }
    
    /**
     * Calculate simple moving average
     */
    private double calculateMovingAverage(List<Double> prices, int period) {
        if (prices.size() < period) {
            period = prices.size();
        }
        
        double sum = 0.0;
        for (int i = prices.size() - period; i < prices.size(); i++) {
            sum += prices.get(i);
        }
        
        return sum / period;
    }
    
    /**
     * Calculate momentum (rate of change)
     */
    private double calculateMomentum(List<Double> prices) {
        if (prices.size() < 2) {
            return 0.0;
        }
        
        int period = Math.min(5, prices.size());
        double oldPrice = prices.get(prices.size() - period);
        double newPrice = prices.get(prices.size() - 1);
        
        return (newPrice - oldPrice) / oldPrice;
    }
    
    /**
     * Calculate Relative Strength Index (RSI)
     */
    public double calculateRSI(List<Double> prices, int period) {
        if (prices.size() < period + 1) {
            return 50.0; // Neutral
        }
        
        double gains = 0.0;
        double losses = 0.0;
        
        for (int i = prices.size() - period; i < prices.size(); i++) {
            double change = prices.get(i) - prices.get(i - 1);
            if (change > 0) {
                gains += change;
            } else {
                losses -= change;
            }
        }
        
        if (losses == 0) {
            return 100.0;
        }
        
        double avgGain = gains / period;
        double avgLoss = losses / period;
        double rs = avgGain / avgLoss;
        
        return 100 - (100 / (1 + rs));
    }
}
