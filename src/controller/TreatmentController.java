package controller;

import db.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Treatment;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TreatmentController {
    
    @FXML private TextField searchField;
    @FXML private TableView<Treatment> treatmentTable;
    @FXML private TableColumn<Treatment, Integer> colId;
    @FXML private TableColumn<Treatment, String> colName;
    @FXML private TableColumn<Treatment, String> colDescription;
    @FXML private TableColumn<Treatment, Double> colCost;
    @FXML private TableColumn<Treatment, Integer> colDuration;
    
    @FXML private TextField nameField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField costField;
    @FXML private ComboBox<Integer> durationCombo;
    
    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;
    @FXML private Label statusLabel;
    
    private ObservableList<Treatment> treatmentList = FXCollections.observableArrayList();
    private Treatment selectedTreatment;
    private int currentUserId;
    private String currentUsername;
    private String currentRole;
    
    @FXML
    public void initialize() {
        // Initialize table columns
        colId.setCellValueFactory(new PropertyValueFactory<>("treatmentId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("treatmentName"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colCost.setCellValueFactory(new PropertyValueFactory<>("cost"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("durationMinutes"));
        
        // Custom cell factory for cost formatting
        colCost.setCellFactory(column -> new TableCell<Treatment, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item));
                }
            }
        });
        
        // Initialize duration combo box
        durationCombo.setItems(FXCollections.observableArrayList(15, 30, 45, 60, 90, 120, 180));
        durationCombo.setValue(30);
        
        // Load treatments
        loadTreatments();
        
        // Only allow numeric input in cost field
        costField.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.matches("\\d*(\\.\\d{0,2})?")) {
                costField.setText(oldText);
            }
        });
        
        // Table selection listener
        treatmentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectTreatment(newSelection);
            }
        });
        
        // Search listener
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            searchTreatments(newText);
        });
    }
    
    public void setUserInfo(int userId, String username, String role) {
        this.currentUserId = userId;
        this.currentUsername = username;
        this.currentRole = role;
        
        // Only admin can manage treatments
        if (!"admin".equalsIgnoreCase(role)) {
            addButton.setDisable(true);
            updateButton.setDisable(true);
            deleteButton.setDisable(true);
            nameField.setDisable(true);
            descriptionArea.setDisable(true);
            costField.setDisable(true);
            durationCombo.setDisable(true);
            statusLabel.setText("View Only Mode - Admin access required for modifications");
        }
    }
    
    private void loadTreatments() {
        String query = "SELECT * FROM treatments ORDER BY treatment_id";
        try (ResultSet rs = DatabaseConnection.executeQuery(query)) {
            treatmentList.clear();
            while (rs != null && rs.next()) {
                treatmentList.add(Treatment.fromResultSet(rs));
            }
            treatmentTable.setItems(treatmentList);
            statusLabel.setText("Loaded " + treatmentList.size() + " treatments");
        } catch (SQLException e) {
            statusLabel.setText("Error loading treatments");
            e.printStackTrace();
        }
    }
    
    private void searchTreatments(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            loadTreatments();
            return;
        }
        
        String query = "SELECT * FROM treatments WHERE treatment_name LIKE ? OR description LIKE ?";
        String searchPattern = "%" + keyword + "%";
        
        try (ResultSet rs = DatabaseConnection.executeQuery(query, searchPattern, searchPattern)) {
            treatmentList.clear();
            while (rs != null && rs.next()) {
                treatmentList.add(Treatment.fromResultSet(rs));
            }
            treatmentTable.setItems(treatmentList);
            statusLabel.setText("Found " + treatmentList.size() + " treatments");
        } catch (SQLException e) {
            statusLabel.setText("Error searching treatments");
            e.printStackTrace();
        }
    }
    
    private void selectTreatment(Treatment treatment) {
        selectedTreatment = treatment;
        nameField.setText(treatment.getTreatmentName());
        descriptionArea.setText(treatment.getDescription());
        costField.setText(String.valueOf(treatment.getCost()));
        durationCombo.setValue(treatment.getDurationMinutes());
        
        addButton.setDisable(true);
        updateButton.setDisable(false);
        deleteButton.setDisable(false);
    }
    
    @FXML
    private void addTreatment() {
        if (!validateInputs()) return;
        
        String query = "INSERT INTO treatments (treatment_name, description, cost, duration_minutes) VALUES (?, ?, ?, ?)";
        
        double cost = Double.parseDouble(costField.getText());
        
        int result = DatabaseConnection.executeUpdate(query,
            nameField.getText().trim(),
            descriptionArea.getText(),
            cost,
            durationCombo.getValue()
        );
        
        if (result > 0) {
            statusLabel.setText("Treatment added successfully!");
            clearForm();
            loadTreatments();
        } else {
            statusLabel.setText("Error adding treatment");
        }
    }
    
    @FXML
    private void updateTreatment() {
        if (selectedTreatment == null) {
            statusLabel.setText("Please select a treatment to update");
            return;
        }
        
        if (!validateInputs()) return;
        
        String query = "UPDATE treatments SET treatment_name=?, description=?, cost=?, duration_minutes=? WHERE treatment_id=?";
        
        double cost = Double.parseDouble(costField.getText());
        
        int result = DatabaseConnection.executeUpdate(query,
            nameField.getText().trim(),
            descriptionArea.getText(),
            cost,
            durationCombo.getValue(),
            selectedTreatment.getTreatmentId()
        );
        
        if (result > 0) {
            statusLabel.setText("Treatment updated successfully!");
            clearForm();
            loadTreatments();
        } else {
            statusLabel.setText("Error updating treatment");
        }
    }
    
    @FXML
    private void deleteTreatment() {
        if (selectedTreatment == null) {
            statusLabel.setText("Please select a treatment to delete");
            return;
        }
        
        // Check if treatment is being used
        String checkQuery = "SELECT COUNT(*) as count FROM patient_treatments WHERE treatment_id = ?";
        try (ResultSet rs = DatabaseConnection.executeQuery(checkQuery, selectedTreatment.getTreatmentId())) {
            if (rs != null && rs.next() && rs.getInt("count") > 0) {
                statusLabel.setText("Cannot delete: Treatment is being used by patients");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Treatment");
        alert.setContentText("Are you sure you want to delete " + selectedTreatment.getTreatmentName() + "?");
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            String query = "DELETE FROM treatments WHERE treatment_id=?";
            int result = DatabaseConnection.executeUpdate(query, selectedTreatment.getTreatmentId());
            
            if (result > 0) {
                statusLabel.setText("Treatment deleted successfully!");
                clearForm();
                loadTreatments();
            } else {
                statusLabel.setText("Error deleting treatment");
            }
        }
    }
    
    @FXML
    private void clearForm() {
        nameField.clear();
        descriptionArea.clear();
        costField.clear();
        durationCombo.setValue(30);
        
        selectedTreatment = null;
        addButton.setDisable(false);
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
        treatmentTable.getSelectionModel().clearSelection();
        statusLabel.setText("Form cleared");
    }
    
    private boolean validateInputs() {
        if (nameField.getText().trim().isEmpty()) {
            statusLabel.setText("Treatment name is required");
            return false;
        }
        if (costField.getText().trim().isEmpty()) {
            statusLabel.setText("Cost is required");
            return false;
        }
        try {
            double cost = Double.parseDouble(costField.getText());
            if (cost < 0) {
                statusLabel.setText("Cost cannot be negative");
                return false;
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid cost format");
            return false;
        }
        return true;
    }
}