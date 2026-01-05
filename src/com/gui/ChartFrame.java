package com.gui;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ChartFrame extends JFrame {

    public ChartFrame(String stockName, List<Double> history) {
        setTitle("Price History - " + stockName);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Main container
        setLayout(new BorderLayout());

        // Chart Panel
        LineChartPanel chartPanel = new LineChartPanel(history, "Closing Price History: " + stockName);
        add(chartPanel, BorderLayout.CENTER);

        // Close Button
        JPanel buttonPanel = new JPanel();
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}
