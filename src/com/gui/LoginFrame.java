package com.gui;

import com.models.User;
import com.services.AuthenticationService;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Login screen using Swing GUI
 */
public class LoginFrame extends JFrame {
    private AuthenticationService authService;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;

    public LoginFrame(AuthenticationService authService) {
        this.authService = authService;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Stock Trading App - Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(41, 128, 185));
        JLabel titleLabel = new JLabel("Stock Trading Application");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        usernameField = new JTextField(20);
        formPanel.add(usernameField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        formPanel.add(passwordField, gbc);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");

        loginButton.setBackground(new Color(46, 204, 113));
        loginButton.setForeground(Color.BLACK);
        loginButton.setFocusPainted(false);

        registerButton.setBackground(new Color(52, 152, 219));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        // Add event listeners
        loginButton.addActionListener(e -> handleLogin());
        registerButton.addActionListener(e -> handleRegister());

        // Enter key on password field
        passwordField.addActionListener(e -> handleLogin());

        // Add panels to frame
        add(titlePanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user = authService.login(username, password);

        if (user != null) {
            // Open main dashboard
            SwingUtilities.invokeLater(() -> {
                new DashboardFrame(user, authService).setVisible(true);
                dispose();
            });
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRegister() {
        JTextField regUsernameField = new JTextField(20);
        JPasswordField regPasswordField = new JPasswordField(20);
        JPasswordField confirmPasswordField = new JPasswordField(20);
        JTextField emailField = new JTextField(100);

        JPanel registerPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        registerPanel.add(new JLabel("Username:"));
        registerPanel.add(regUsernameField);
        registerPanel.add(new JLabel("Password:"));
        registerPanel.add(regPasswordField);
        registerPanel.add(new JLabel("Confirm Password:"));
        registerPanel.add(confirmPasswordField);
        registerPanel.add(new JLabel("Email:"));
        registerPanel.add(emailField);

        int result = JOptionPane.showConfirmDialog(this, registerPanel,
                "Register New Account",
                JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String username = regUsernameField.getText().trim();
            String password = new String(regPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            String email = emailField.getText().trim();

            if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean success = authService.register(username, password, email);

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Registration successful! You can now login.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Registration failed. Username may already exist or password is too weak (min 8 chars, 1 upper, 1 special).",
                        "Error", JOptionPane.ERROR_MESSAGE);
                System.out.println(JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
