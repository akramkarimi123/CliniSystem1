package controller;

import db.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button cancelButton;
    @FXML private Label statusLabel;
    
    private String loggedInUsername;
    private String loggedInRole;
    private int loggedInUserId;
    
    @FXML
    public void initialize() {
        // Set up enter key to trigger login
        usernameField.setOnAction(event -> login());
        passwordField.setOnAction(event -> login());
        
        // Clear status label
        statusLabel.setText("");
    }
    
    @FXML
    private void login() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        
        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter username and password");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        
        // Query database for user
        String query = "SELECT user_id, username, role, full_name FROM users WHERE username = ? AND password = ?";
        
        try (ResultSet rs = DatabaseConnection.executeQuery(query, username, password)) {
            if (rs != null && rs.next()) {
                // Login successful
                loggedInUserId = rs.getInt("user_id");
                loggedInUsername = rs.getString("username");
                loggedInRole = rs.getString("role");
                String fullName = rs.getString("full_name");
                
                statusLabel.setText("Login successful! Welcome " + fullName);
                statusLabel.setStyle("-fx-text-fill: green;");
                
                // Open dashboard
                openDashboard();
                
            } else {
                // Login failed
                statusLabel.setText("Invalid username or password");
                statusLabel.setStyle("-fx-text-fill: red;");
                passwordField.clear();
            }
        } catch (SQLException e) {
            statusLabel.setText("Database error. Please try again.");
            statusLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }
    
    @FXML
    private void cancel() {
        // Close the application
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    private void openDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/dashboard.fxml"));
            Parent root = loader.load();
            
            // Get dashboard controller and pass user info
            DashboardController dashboardController = loader.getController();
            dashboardController.setUserInfo(loggedInUserId, loggedInUsername, loggedInRole);
            
            Stage stage = new Stage();
            stage.setTitle("Dental Clinic Management System - Dashboard");
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
            
            // Close login window
            Stage loginStage = (Stage) loginButton.getScene().getWindow();
            loginStage.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading dashboard");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }
    
    // Getters for user info
    public int getLoggedInUserId() {
        return loggedInUserId;
    }
    
    public String getLoggedInUsername() {
        return loggedInUsername;
    }
    
    public String getLoggedInRole() {
        return loggedInRole;
    }
}