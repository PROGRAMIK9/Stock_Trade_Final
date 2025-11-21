package com.interfaces;

import com.models.Portfolio;
import com.models.Stock;
import java.util.List;

/**
 * Interface for portfolio management operations
 */
public interface PortfolioService {
    boolean buyStock(Portfolio portfolio, Stock stock, int quantity);
    boolean sellStock(Portfolio portfolio, String symbol, int quantity);
    double getPortfolioValue(Portfolio portfolio);
    List<Stock> getHoldings(Portfolio portfolio);
}

