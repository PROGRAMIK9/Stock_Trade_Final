package com.gui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Custom panel to draw a line chart of stock prices.
 */
public class LineChartPanel extends JPanel {
    private List<Double> dataPoints;
    private String title;

    public LineChartPanel(List<Double> dataPoints, String title) {
        this.dataPoints = dataPoints != null ? dataPoints : new ArrayList<>();
        this.title = title;
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(Color.GRAY));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int padding = 40;
        int labelPadding = 20;

        // Draw title
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g2.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        g2.drawString(title, (width - titleWidth) / 2, padding / 2);

        if (dataPoints.isEmpty()) {
            String msg = "No Data Available";
            int msgWidth = fm.stringWidth(msg);
            g2.drawString(msg, (width - msgWidth) / 2, height / 2);
            return;
        }

        double min = Collections.min(dataPoints);
        double max = Collections.max(dataPoints);
        double range = max - min;

        // Prevent division by zero if all prices are same
        if (range == 0)
            range = 1;

        // Draw Axes
        g2.drawLine(padding + labelPadding, height - padding - labelPadding, padding + labelPadding, padding); // Y Axis
        g2.drawLine(padding + labelPadding, height - padding - labelPadding, width - padding,
                height - padding - labelPadding); // X Axis

        // Scales
        double xScale = (double) (width - 2 * padding - labelPadding) / (dataPoints.size() - 1);
        double yScale = (double) (height - 2 * padding - labelPadding) / range;

        // Transform data points to coordinates
        List<Point> graphPoints = new ArrayList<>();
        for (int i = 0; i < dataPoints.size(); i++) {
            int x1 = (int) (i * xScale + padding + labelPadding);
            int y1 = (int) ((max - dataPoints.get(i)) * yScale + padding);
            graphPoints.add(new Point(x1, y1));
        }

        // Draw lines
        g2.setColor(new Color(41, 128, 185));
        g2.setStroke(new BasicStroke(2f));
        for (int i = 0; i < graphPoints.size() - 1; i++) {
            int x1 = graphPoints.get(i).x;
            int y1 = graphPoints.get(i).y;
            int x2 = graphPoints.get(i + 1).x;
            int y2 = graphPoints.get(i + 1).y;
            g2.drawLine(x1, y1, x2, y2);
        }

        // Draw points
        g2.setColor(new Color(231, 76, 60));
        for (Point p : graphPoints) {
            int r = 4;
            g2.fillOval(p.x - r, p.y - r, 2 * r, 2 * r);
        }

        // Draw Min/Max labels
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.drawString(String.format("Max: %.2f", max), padding + 5, padding);
        g2.drawString(String.format("Min: %.2f", min), padding + 5, height - padding);
    }
}
