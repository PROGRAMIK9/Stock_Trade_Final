package com.models;

/**
 * User class demonstrating inheritance from Person
 */
public class User extends Person {
    private String username;
    private String password;
    private Portfolio portfolio;
    
    public User(String username, String password, String email) {
        super(email);
        this.username = username;
        this.password = password;
        this.portfolio = new Portfolio(10000.0); // Starting with $10,000
    }
    
    public User(int id, String username, String email, String password) {
        super(id, email);
        this.username = username;
        this.password = password;
    }
    
    @Override
    public String getRole() {
        return "TRADER";
    }
    
    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Portfolio getPortfolio() { return portfolio; }
    public void setPortfolio(Portfolio portfolio) { this.portfolio = portfolio; }
    
    @Override
    public String toString() {
        return "User{username='" + username + "', name='" + name + "', email='" + email + "'}";
    }
}
