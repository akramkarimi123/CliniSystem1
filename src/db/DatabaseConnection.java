package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    
    // Database configuration
    private static final String URL = "jdbc:mysql://localhost:3306/dental_clinic";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";
    
    private static Connection connection = null;
    
    public DatabaseConnection() {
        // Empty constructor
    }
    
    /**
     * Get database connection
     */
    public static Connection getConnection() {
        try {
            // FIX: also check if connection is closed (IMPORTANT)
            if (connection == null || connection.isClosed()) {
                
                Class.forName("com.mysql.cj.jdbc.Driver");
                
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                
                System.out.println("Database connected successfully!");
            }
            
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Failed to connect to database!");
            e.printStackTrace();
        }
        
        return connection;
    }
    
    /**
     * Close connection
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Error closing connection!");
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Execute UPDATE / INSERT / DELETE
     */
    public static int executeUpdate(String query, Object... params) {
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            
            return pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error executing update: " + query);
            e.printStackTrace();
            return -1;
        }
    }
    
    /**
     * Execute INSERT and return generated ID
     */
    public static int executeInsert(String query, Object... params) {
        try (PreparedStatement pstmt = getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
            
            return -1;
            
        } catch (SQLException e) {
            System.err.println("Error executing insert: " + query);
            e.printStackTrace();
            return -1;
        }
    }
    
    /**
     * Execute SELECT query
     */
    public static ResultSet executeQuery(String query, Object... params) {
        try {
            PreparedStatement pstmt = getConnection().prepareStatement(query);
            
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            
            return pstmt.executeQuery();
            
        } catch (SQLException e) {
            System.err.println("Error executing query: " + query);
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Test connection
     */
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("✓ Database connection test: SUCCESS");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("✗ Database connection test: FAILED");
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Check connection status
     */
    public static boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}