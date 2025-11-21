package com.interfaces;

import com.models.User;

/**
 * Interface demonstrating abstraction for authentication operations
 */
public interface AuthService {
    User login(String username, String password);
    boolean register(String username, String password, String email);
    void logout(User user);
    boolean isAuthenticated(User user);
}
