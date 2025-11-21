package com;

import com.database.DatabaseManager;
import com.gui.LoginFrame;
import com.services.AuthenticationService;
import javax.swing.*;

/**
 * Main entry point for Stock Trading Application
 * Demonstrates all required Java concepts:
 * - Threads (in StockAPIClient)
 * - Inheritance (Person -> User)
 * - RESTful APIs (StockAPIClient)
 * - Packages with import statements (throughout)
 * - Interfaces (AuthService, PortfolioService, StockPredictor)
 * - Collections (List, Map, Set throughout)
 * - Generics (List<Stock>, Map<String, Stock>, etc.)
 */
public class Main {
    public static void main(String[] args) {
        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set look and feel: " + e.getMessage());
        }
        
        // Initialize database
        DatabaseManager dbManager = new DatabaseManager();
        
        // Initialize authentication service
        AuthenticationService authService = new AuthenticationService(dbManager);
        
        // Launch login screen on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame(authService);
            loginFrame.setVisible(true);
        });
        
        System.out.println("Stock Trading Application Started");
        System.out.println("=================================");
        System.out.println("Features:");
        System.out.println("- User Authentication (Login/Register)");
        System.out.println("- Portfolio Management (Buy/Sell stocks)");
        System.out.println("- Stock Price Prediction (Trend Analysis)");
        System.out.println("- Real-time stock data fetching with threads");
        System.out.println("- SQLite database for persistence");
        System.out.println("- Comprehensive GUI with Swing");
    }
}

