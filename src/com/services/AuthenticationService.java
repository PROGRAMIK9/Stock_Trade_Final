package com.services;

import com.interfaces.AuthService;
import com.models.User;
import com.database.DatabaseManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * Authentication service implementing AuthService interface
 */
public class AuthenticationService implements AuthService {
    private DatabaseManager dbManager;
    private Set<String> authenticatedUsers; // Using Set from Collections

    public AuthenticationService(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.authenticatedUsers = new HashSet<>();
    }

    @Override
    public User login(String username, String password) {
        try {
            User user = dbManager.getUserByUsername(username);

            if (user != null && user.getPassword().equals(password)) {
                authenticatedUsers.add(username);

                // Load user's portfolio
                user.setPortfolio(dbManager.getPortfolioByUserId(user.getId()));

                return user;
            }
        } catch (SQLException e) {
            System.err.println("Login error: " + e.getMessage());
        }

        return null;
    }

    @Override
    public boolean register(String username, String password, String email) {
        if (!isValidPassword(password)) {
            System.err.println("Password does not meet security requirements.");
            return false;
        }

        try {
            // Check if user already exists
            User existing = dbManager.getUserByUsername(username);
            if (existing != null) {
                return false;
            }

            // Create new user
            User newUser = new User(username, password, email);
            int userId = dbManager.createUser(newUser);

            return userId > 0;
        } catch (SQLException e) {
            System.err.println("Registration error: " + e.getMessage());
            return false;
        }
    }

    private boolean isValidPassword(String password) {
        // Constraints: At least 8 chars, 1 uppercase, 1 special char
        if (password == null || password.length() < 8)
            return false;
        boolean hasUpper = false;
        boolean hasSpecial = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c))
                hasUpper = true;
            if (!Character.isLetterOrDigit(c))
                hasSpecial = true;
        }
        return hasUpper && hasSpecial;
    }

    @Override
    public void logout(User user) {
        if (user != null) {
            authenticatedUsers.remove(user.getUsername());
        }
    }

    @Override
    public boolean isAuthenticated(User user) {
        return user != null && authenticatedUsers.contains(user.getUsername());
    }
}
