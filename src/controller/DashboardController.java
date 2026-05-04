package controller;

import db.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

// ✅ NEW IMPORTS (for charts)
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DashboardController {
    
    @FXML private Label welcomeLabel;
    @FXML private Label roleLabel;
    @FXML private Label todayAppointmentsLabel;
    @FXML private Label pendingPaymentsLabel;
    @FXML private Label totalPatientsLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private BorderPane mainContent;

    // ✅ NEW CHART FIELDS
    @FXML private BarChart<String, Number> appointmentsChart;
    @FXML private PieChart paymentChart;
    
    @FXML private MenuItem managePatientsMenuItem;
    @FXML private MenuItem manageAppointmentsMenuItem;
    @FXML private MenuItem manageTreatmentsMenuItem;
    @FXML private MenuItem manageBillingMenuItem;
    
    @FXML private Button patientsButton;
    @FXML private Button appointmentsButton;
    @FXML private Button treatmentsButton;
    @FXML private Button billingButton;
    
    private int currentUserId;
    private String currentUsername;
    private String currentRole;
    
    public void setUserInfo(int userId, String username, String role) {
        this.currentUserId = userId;
        this.currentUsername = username;
        this.currentRole = role;
        
        welcomeLabel.setText("Welcome, " + username + "!");
        roleLabel.setText("Role: " + role.toUpperCase());
        
        setPermissionsBasedOnRole();
        loadStatistics();

        // ✅ LOAD CHART DATA
        loadCharts();
    }
    
    private void setPermissionsBasedOnRole() {
        if ("staff".equalsIgnoreCase(currentRole)) {
            if (manageTreatmentsMenuItem != null) {
                manageTreatmentsMenuItem.setDisable(true);
            }
            if (treatmentsButton != null) {
                treatmentsButton.setDisable(true);
            }
        }
    }
    
    private void loadStatistics() {
        String todayAppointmentsQuery = "SELECT COUNT(*) as count FROM appointments WHERE appointment_date = CURDATE()";
        try (ResultSet rs = DatabaseConnection.executeQuery(todayAppointmentsQuery)) {
            if (rs != null && rs.next()) {
                todayAppointmentsLabel.setText(String.valueOf(rs.getInt("count")));
            }
        } catch (SQLException e) {
            todayAppointmentsLabel.setText("0");
            e.printStackTrace();
        }
        
        String pendingPaymentsQuery = "SELECT COUNT(*) as count FROM billing WHERE payment_status IN ('Pending', 'Partial')";
        try (ResultSet rs = DatabaseConnection.executeQuery(pendingPaymentsQuery)) {
            if (rs != null && rs.next()) {
                pendingPaymentsLabel.setText(String.valueOf(rs.getInt("count")));
            }
        } catch (SQLException e) {
            pendingPaymentsLabel.setText("0");
            e.printStackTrace();
        }
        
        String totalPatientsQuery = "SELECT COUNT(*) as count FROM patients";
        try (ResultSet rs = DatabaseConnection.executeQuery(totalPatientsQuery)) {
            if (rs != null && rs.next()) {
                totalPatientsLabel.setText(String.valueOf(rs.getInt("count")));
            }
        } catch (SQLException e) {
            totalPatientsLabel.setText("0");
            e.printStackTrace();
        }
        
        if ("admin".equalsIgnoreCase(currentRole)) {
            String revenueQuery = "SELECT SUM(paid_amount) as total FROM billing WHERE payment_status = 'Paid'";
            try (ResultSet rs = DatabaseConnection.executeQuery(revenueQuery)) {
                if (rs != null && rs.next()) {
                    double total = rs.getDouble("total");
                    totalRevenueLabel.setText(String.format("$%.2f", total));
                }
            } catch (SQLException e) {
                totalRevenueLabel.setText("$0.00");
                e.printStackTrace();
            }
        } else {
            totalRevenueLabel.setText("Restricted");
        }
    }

    // =========================
    // ✅ NEW METHOD: LOAD CHARTS
    // =========================
    private void loadCharts() {

        loadAppointmentsChart();
        loadPaymentChart();
    }

    // 📊 Bar Chart: Appointments per day (last 7 days)
    private void loadAppointmentsChart() {
        if (appointmentsChart == null) return;

        appointmentsChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Appointments");

        String query = "SELECT DATE(appointment_date) as day, COUNT(*) as count " +
                       "FROM appointments " +
                       "WHERE appointment_date >= CURDATE() - INTERVAL 7 DAY " +
                       "GROUP BY day ORDER BY day";

        try (ResultSet rs = DatabaseConnection.executeQuery(query)) {
            while (rs != null && rs.next()) {
                String day = rs.getString("day");
                int count = rs.getInt("count");

                series.getData().add(new XYChart.Data<>(day, count));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        appointmentsChart.getData().add(series);
    }

    // 🥧 Pie Chart: Payment status
    private void loadPaymentChart() {
        if (paymentChart == null) return;

        paymentChart.getData().clear();

        String query = "SELECT payment_status, COUNT(*) as count FROM billing GROUP BY payment_status";

        try (ResultSet rs = DatabaseConnection.executeQuery(query)) {
            while (rs != null && rs.next()) {
                String status = rs.getString("payment_status");
                int count = rs.getInt("count");

                paymentChart.getData().add(
                        new PieChart.Data(status, count)
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void showPatients() {
        loadModule("/view/patient_form.fxml", "Patient Management");
    }
    
    @FXML
    private void showAppointments() {
        loadModule("/view/appointment_form.fxml", "Appointment Management");
    }
    
    @FXML
    private void showTreatments() {
        if ("admin".equalsIgnoreCase(currentRole)) {
            loadModule("/view/treatment_form.fxml", "Treatment Management");
        } else {
            showAlert("Access Denied", "Only administrators can manage treatments.");
        }
    }
    
    @FXML
    private void showBilling() {
        loadModule("/view/billing.fxml", "Billing Management");
    }
    
    private void loadModule(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            Object controller = loader.getController();
            if (controller instanceof PatientController) {
                ((PatientController) controller).setUserInfo(currentUserId, currentUsername, currentRole);
            } else if (controller instanceof AppointmentController) {
                ((AppointmentController) controller).setUserInfo(currentUserId, currentUsername, currentRole);
            } else if (controller instanceof TreatmentController) {
                ((TreatmentController) controller).setUserInfo(currentUserId, currentUsername, currentRole);
            } else if (controller instanceof BillingController) {
                ((BillingController) controller).setUserInfo(currentUserId, currentUsername, currentRole);
            }
            
            mainContent.setCenter(root);
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not load module: " + title);
        }
    }
    
    @FXML
    private void logout() {
        try {
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.close();
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Parent root = loader.load();
            Stage loginStage = new Stage();
            loginStage.setTitle("Karimi Dental Clinic - Login");
            loginStage.setScene(new Scene(root));
            loginStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void exit() {
        System.exit(0);
    }
    
    @FXML
    private void refresh() {
        loadStatistics();

        // ✅ REFRESH CHARTS ALSO
        loadCharts();

        showAlert("Refreshed", "Dashboard statistics have been refreshed.");
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}