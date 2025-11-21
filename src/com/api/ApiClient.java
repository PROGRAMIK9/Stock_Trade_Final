package com.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import com.models.Stock;
import org.json.JSONArray;
import org.json.JSONObject;

public class ApiClient {

    // This points to your running Spring Boot app
    private static final String BASE_URL = "http://localhost:8080";
    
    // The modern Java HTTP Client
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * Calls your Spring endpoint: /fyers/quote?symbol=...
     */
    public static String getLiveQuote(String symbol) {
        String endpoint = "/fyers/quote?symbol=" + symbol;
        return sendGetRequest(endpoint);
    }

    /**
     * Calls your Spring endpoint: /fyers/history?symbol=...
     */
    public static String getHistory(String symbol) {
        String endpoint = "/fyers/history?symbol=" + symbol;
        return sendGetRequest(endpoint);
    }

    /**
     * Calls your Spring endpoint: /portfolio/holdings
     */
    public static String getMyHoldings() {
        return sendGetRequest("/portfolio/holdings");
    }

    // --- Helper Method to send the actual Request ---
    private static String sendGetRequest(String endpoint) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body(); // Returns the JSON String
            } else {
                System.err.println("API Error: " + response.statusCode());
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public Map<String, Stock> fetchMultipleStocks(List<String> symbolList) {
        Map<String, Stock> stockMap = new HashMap<>();
        
        // 1. Convert List to Comma-Separated String
        // Example: "NSE:TCS-EQ,NSE:INFY-EQ"
        String joinedSymbols = String.join(",", symbolList);
        
        // 2. Call the existing method to get raw JSON string
        String jsonResponse = getLiveQuote(joinedSymbols);

        if (jsonResponse == null || jsonResponse.isEmpty()) {
            return stockMap; // Return empty map on failure
        }

        try {
            // 3. Parse the Fyers V3 JSON Response
            JSONObject root = new JSONObject(jsonResponse);
            
            // Fyers returns data in a "d" array
            JSONArray dataArray = root.getJSONArray("d");

            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject item = dataArray.getJSONObject(i);
                
                // Extract "n" (symbol name) and "v" (value object)
                String symbol = item.getString("n");
                JSONObject values = item.getJSONObject("v");
                
                // Extract "lp" (Last Traded Price) and "chp" (Change Percent)
                double price = values.optDouble("lp", 0.0);
                double change = values.optDouble("chp", 0.0);

                // Create Stock object and add to map
                Stock stock = new Stock(symbol, price);
                stockMap.put(symbol, stock);
            }
        } catch (Exception e) {
            System.err.println("Error parsing stock data: " + e.getMessage());
            e.printStackTrace();
        }

        return stockMap;
    }
    
}