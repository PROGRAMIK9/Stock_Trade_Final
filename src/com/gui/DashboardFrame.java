package com.gui;

import com.database.DatabaseManager;
import com.models.*;
import com.api.ApiClient;
import com.services.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Main dashboard GUI with portfolio and trading features
 */
public class DashboardFrame extends JFrame {
    private User currentUser;
    private AuthenticationService authService;
    private TrendPredictionService predictionService;
    private PortfolioManagementService portfolioService;
    private DatabaseManager dbManager;
    private ApiClient apiClient;

    private JLabel cashBalanceLabel;
    private JLabel portfolioValueLabel;
    private JTable stockTable;
    private JTable holdingsTable;
    private JTable transactionTable;
    private DefaultTableModel stockTableModel;
    private DefaultTableModel holdingsTableModel;
    private DefaultTableModel transactionTableModel;

    private Map<String, Stock> availableStocks;

    public DashboardFrame(User user, AuthenticationService authService) {
        this.currentUser = user;
        this.authService = authService;
        this.predictionService = new TrendPredictionService();
        this.dbManager = new DatabaseManager();
        this.portfolioService = new PortfolioManagementService(dbManager);
        this.availableStocks = new HashMap<>();
        this.apiClient = new ApiClient();
        try {
            // 1. Force load the portfolio from the DB
            Portfolio p = dbManager.getPortfolioByUserId(currentUser.getId());

            // 2. If it doesn't exist, create it!
            if (p == null) {
                System.out.println("No portfolio found. Creating one...");
                int portId = dbManager.createPortfolio(currentUser.getId(), 100000.00); // Give $100k
                p = dbManager.getPortfolioByUserId(currentUser.getId()); // Load it again
            }

            // 3. CRITICAL: Attach it to the User object in memory
            this.currentUser.setPortfolio(p);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage());
        }
        initializeUI();
        loadStockData();
        updatePortfolioDisplay();
        // startRealTimeUpdates();
    }

    private void initializeUI() {
        setTitle("Stock Trading Dashboard - " + currentUser.getUsername());
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Top panel with user info
        JPanel topPanel = createTopPanel();

        // Tabbed pane for different sections
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Market", createMarketPanel());
        tabbedPane.addTab("Portfolio", createPortfolioPanel());
        tabbedPane.addTab("Transactions", createTransactionsPanel());

        add(topPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(41, 128, 185));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getUsername());
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        infoPanel.setBackground(new Color(41, 128, 185));

        cashBalanceLabel = new JLabel();
        portfolioValueLabel = new JLabel();
        cashBalanceLabel.setForeground(Color.WHITE);
        portfolioValueLabel.setForeground(Color.WHITE);

        JButton refreshButton = new JButton("Refresh");
        JButton logoutButton = new JButton("Logout");

        refreshButton.addActionListener(e -> {
            // loadStockData();
            updatePortfolioDisplay();
        });

        logoutButton.addActionListener(e -> {
            authService.logout(currentUser);
            new LoginFrame(authService).setVisible(true);
            dispose();
        });

        infoPanel.add(cashBalanceLabel);
        infoPanel.add(new JLabel("  |  "));
        infoPanel.add(portfolioValueLabel);
        infoPanel.add(refreshButton);
        infoPanel.add(logoutButton);

        panel.add(welcomeLabel, BorderLayout.WEST);
        panel.add(infoPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createMarketPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Stock table
        String[] columns = { "Symbol", "Name", "Price", "Open", "High", "Low", "Prediction" };
        stockTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        stockTable = new JTable(stockTableModel);
        stockTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stockTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(stockTable);

        // Action panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton buyButton = new JButton("Buy Stock");
        JButton viewChartButton = new JButton("View Prediction Details");

        buyButton.setBackground(new Color(46, 204, 113));
        buyButton.setForeground(Color.WHITE);

        buyButton.addActionListener(e -> handleBuyStock());
        viewChartButton.addActionListener(e -> showPredictionDetails());

        actionPanel.add(buyButton);

        JButton chartButton = new JButton("Chart");
        chartButton.setBackground(new Color(52, 152, 219));
        chartButton.setForeground(Color.WHITE);
        chartButton.addActionListener(e -> showChart());
        actionPanel.add(chartButton);

        actionPanel.add(viewChartButton);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createPortfolioPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Holdings table
        String[] columns = { "Symbol", "Quantity", "Avg Price", "Current Value" };
        holdingsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        holdingsTable = new JTable(holdingsTableModel);
        JScrollPane scrollPane = new JScrollPane(holdingsTable);

        // Action panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton sellButton = new JButton("Sell Stock");
        sellButton.setBackground(new Color(231, 76, 60));
        sellButton.setForeground(Color.WHITE);

        sellButton.addActionListener(e -> handleSellStock());
        actionPanel.add(sellButton);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createTransactionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Transaction table
        String[] columns = { "Type", "Symbol", "Quantity", "Price", "Total", "Date" };
        transactionTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        transactionTable = new JTable(transactionTableModel);
        JScrollPane scrollPane = new JScrollPane(transactionTable);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadStockData() {
        // Load popular stocks using threads
        SwingWorker<Map<String, Stock>, Void> worker = new SwingWorker<>() {
            @Override
            protected Map<String, Stock> doInBackground() {
                List<String> symbols = Arrays.asList(
                        "NSE:RELIANCE-EQ");
                Map<String, Stock> stocks = apiClient.fetchMultipleStocks(symbols);
                for (Stock s : stocks.values()) {
                    try {
                        // Call the method we added to ApiClient earlier
                        List<Candle> history = apiClient.fetchStockHistory(s.getSymbol());

                        // Store it in the stock object
                        s.setHistoricalPrices(history);

                        // Optional: Update Open/High/Low from the latest history candle
                        // if the live quote didn't provide it
                        if (!history.isEmpty()) {
                            Candle latest = history.get(history.size() - 1);
                            // You might need setters in Stock.java for these
                            s.setOpenPrice(latest.getOpen());
                            s.setHighPrice(latest.getHigh());
                            s.setLowPrice(latest.getLow());
                        }

                    } catch (Exception e) {
                        System.err.println("Could not fetch history for " + s.getSymbol());
                    }
                }

                return stocks;
            }

            @Override
            protected void done() {
                try {
                    availableStocks = get();
                    updateStockTable();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(DashboardFrame.this,
                            "Error loading stock data: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void updateStockTable() {
        stockTableModel.setRowCount(0);

        for (Stock stock : availableStocks.values()) {
            // Calculate prediction
            String prediction = predictionService.predictTrend(stock.getHistoricalPrices());
            stock.setPrediction(prediction);

            stockTableModel.addRow(new Object[] {
                    stock.getSymbol(),
                    stock.getName(),
                    String.format("₹%.2f", stock.getCurrentPrice()),
                    String.format("₹%.2f", stock.getOpenPrice()),
                    String.format("₹%.2f", stock.getHighPrice()),
                    String.format("₹%.2f", stock.getLowPrice()),
                    prediction
            });
        }
    }

    private void updatePortfolioDisplay() {
        Portfolio portfolio = currentUser.getPortfolio();
        if (portfolio != null) {
            cashBalanceLabel.setText(String.format("Cash: ₹%.2f", portfolio.getCashBalance()));
            // Calculate total portfolio value
            Map<String, Double> currentPrices = new HashMap<>();
            for (String symbol : portfolio.getHoldings().keySet()) {
                Stock stock = availableStocks.get(symbol);
                if (stock != null) {
                    currentPrices.put(symbol, stock.getCurrentPrice());
                } else {
                    Portfolio.StockHolding holding = portfolio.getHoldings().get(symbol);
                    currentPrices.put(symbol, holding.getAveragePrice());
                }
            }

            double totalValue = portfolio.getTotalValue(currentPrices);
            portfolioValueLabel.setText(String.format("Total Value: ₹%.2f", totalValue));

            // Update holdings table
            holdingsTableModel.setRowCount(0);
            for (Portfolio.StockHolding holding : portfolio.getHoldings().values()) {
                double currentPrice = currentPrices.getOrDefault(holding.getSymbol(), holding.getAveragePrice());
                double currentValue = holding.getQuantity() * currentPrice;

                holdingsTableModel.addRow(new Object[] {
                        holding.getSymbol(),
                        holding.getQuantity(),
                        String.format("₹%.2f", holding.getAveragePrice()),
                        String.format("₹%.2f", currentValue)
                });
            }

            // Update transactions table
            transactionTableModel.setRowCount(0);
            for (Transaction transaction : portfolio.getTransactionHistory()) {
                transactionTableModel.addRow(new Object[] {
                        transaction.getType(),
                        transaction.getSymbol(),
                        transaction.getQuantity(),
                        String.format("₹%.2f", transaction.getPrice()),
                        String.format("₹%.2f", transaction.getTotalAmount()),
                        transaction.getTimestamp().toString()
                });
            }
        } else {
            cashBalanceLabel.setText("Cash:100000.0");
        }

    }

    private void handleBuyStock() {
        int selectedRow = stockTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a stock to buy",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String symbol = (String) stockTableModel.getValueAt(selectedRow, 0);
        Stock stock = availableStocks.get(symbol);

        String quantityStr = JOptionPane.showInputDialog(this,
                "Enter quantity to buy for " + symbol + " @ ₹" +
                        String.format("%.2f", stock.getCurrentPrice()) + ":",
                "Buy Stock",
                JOptionPane.QUESTION_MESSAGE);

        if (quantityStr != null && !quantityStr.isEmpty()) {
            try {
                int quantity = Integer.parseInt(quantityStr);
                if (quantity <= 0) {
                    throw new NumberFormatException();
                }

                boolean success = portfolioService.buyStock(currentUser.getPortfolio(), stock, quantity);

                if (success) {
                    updatePortfolioDisplay();
                    JOptionPane.showMessageDialog(this,
                            "Successfully bought " + quantity + " shares of " + symbol,
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to buy stock. Check your balance.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid quantity",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleSellStock() {
        int selectedRow = holdingsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a stock to sell",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String symbol = (String) holdingsTableModel.getValueAt(selectedRow, 0);
        int currentQuantity = (Integer) holdingsTableModel.getValueAt(selectedRow, 1);

        String quantityStr = JOptionPane.showInputDialog(this,
                "Enter quantity to sell (Max: " + currentQuantity + "):", "Sell Stock",
                JOptionPane.QUESTION_MESSAGE);

        if (quantityStr != null && !quantityStr.isEmpty()) {
            try {
                int quantity = Integer.parseInt(quantityStr);
                if (quantity <= 0 || quantity > currentQuantity) {
                    throw new NumberFormatException();
                }

                boolean success = portfolioService.sellStock(currentUser.getPortfolio(), symbol, quantity);

                if (success) {
                    updatePortfolioDisplay();
                    JOptionPane.showMessageDialog(this,
                            "Successfully sold " + quantity + " shares of " + symbol,
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to sell stock",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid quantity",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showPredictionDetails() {
        int selectedRow = stockTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a stock",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String symbol = (String) stockTableModel.getValueAt(selectedRow, 0);
        Stock stock = availableStocks.get(symbol);

        double confidence = predictionService.getConfidenceScore(stock.getHistoricalPrices());
        double predictedPrice = predictionService.predictNextPrice(stock);

        String message = String.format(
                "Stock: %s\n" +
                        "Current Price: ₹%.2f\n" +
                        "Predicted Trend: %s\n" +
                        "Predicted Next Price: ₹%.2f\n" +
                        "Confidence Score: %.1f%%\n\n" +
                        "Analysis based on %d days of historical data.",
                symbol,
                stock.getCurrentPrice(),
                stock.getPrediction(),
                predictedPrice,
                confidence * 100,
                stock.getHistoricalPrices().size());

        JOptionPane.showMessageDialog(this, message,
                "Prediction Details for " + symbol, JOptionPane.INFORMATION_MESSAGE);
    }

    private void showChart() {
        int selectedRow = stockTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a stock to view chart",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String symbol = (String) stockTableModel.getValueAt(selectedRow, 0);
        Stock stock = availableStocks.get(symbol);

        List<Double> history = stock.getHistoricalPrices();
        if (history == null || history.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No historical data available for " + symbol,
                    "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            new ChartFrame(symbol, history).setVisible(true);
        });
    }

    private void startRealTimeUpdates() {
        Thread updateThread = new Thread(() -> {
            while (true) {
                try {
                    // Update stock prices every 1 second
                    SwingUtilities.invokeLater(() -> loadStockData());
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        updateThread.setDaemon(true); // Ensure thread terminates when app closes
        updateThread.start();
    }
}
