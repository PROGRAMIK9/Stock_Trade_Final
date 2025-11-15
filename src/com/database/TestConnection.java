package com.database;

import java.sql.Connection;

public class TestConnection {

    public static void main(String[] args) {
        
        System.out.println("--- Starting database connection test ---");

        // 1. Create an instance of your DatabaseManager
        // This will run the code inside its constructor
        DatabaseManager dbManager = new DatabaseManager();

        // 2. Get the connection object from the manager
        Connection conn = dbManager.getConnection(); // Uses the getter method from my last example

        // 3. Check if the connection is valid
        if (conn != null) {
            System.out.println("Test PASSED: Connection successful!");
            
            // Good practice: close the connection when you're done
            try {
                conn.close();
                System.out.println("Connection closed.");
            } catch (Exception e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }

        } else {
            System.err.println("Test FAILED: Connection is null.");
            System.err.println("Check the console for errors from the DatabaseManager constructor.");
        }

        System.out.println("--- Test finished ---");
    }
}