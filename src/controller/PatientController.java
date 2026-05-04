package controller;

import db.DatabaseConnection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Patient;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class PatientController {
    
    @FXML private TextField searchField;
    @FXML private TableView<Patient> patientTable;
    @FXML private TableColumn<Patient, Integer> colId;
    @FXML private TableColumn<Patient, String> colFirstName;
    @FXML private TableColumn<Patient, String> colLastName;
    @FXML private TableColumn<Patient, String> colPhone;
    @FXML private TableColumn<Patient, String> colEmail;
    @FXML private TableColumn<Patient, Integer> colAge;
    
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private DatePicker dobPicker;
    @FXML private ComboBox<String> genderCombo;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextArea addressArea;
    @FXML private TextArea medicalHistoryArea;
    @FXML private TextField emergencyContactField;
    
    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;
    @FXML private Label statusLabel;
    
    private ObservableList<Patient> patientList = FXCollections.observableArrayList();
    private Patient selectedPatient;
    private int currentUserId;
    private String currentUsername;
    private String currentRole;
    
    @FXML
    public void initialize() {
        // Initialize table columns
        colId.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colAge.setCellValueFactory(new PropertyValueFactory<>("age"));
        
        // Initialize gender combo box
        genderCombo.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));
        
        // FIX: safe loading after UI is ready
        Platform.runLater(() -> loadPatients());
        
        // Set table selection listener
        patientTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectPatient(newSelection);
            }
        });
        
        // Search listener
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            searchPatients(newText);
        });
    }
    
    public void setUserInfo(int userId, String username, String role) {
        this.currentUserId = userId;
        this.currentUsername = username;
        this.currentRole = role;
        
        // Disable delete for staff
        if ("staff".equalsIgnoreCase(role)) {
            deleteButton.setDisable(true);
            deleteButton.setTooltip(new Tooltip("Staff cannot delete patients"));
        }
    }
    
    private void loadPatients() {
        String query = "SELECT * FROM patients ORDER BY patient_id DESC";
        
        try (ResultSet rs = DatabaseConnection.executeQuery(query)) {
            
            patientList.clear();
            
            if (rs != null) {
                while (rs.next()) {
                    patientList.add(Patient.fromResultSet(rs));
                }
            }
            
            patientTable.setItems(patientList);
            statusLabel.setText("Loaded " + patientList.size() + " patients");
            
        } catch (SQLException e) {
            statusLabel.setText("Error loading patients");
            e.printStackTrace();
        }
    }
    
    private void searchPatients(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            loadPatients();
            return;
        }
        
        String query = "SELECT * FROM patients WHERE first_name LIKE ? OR last_name LIKE ? OR phone LIKE ? OR email LIKE ?";
        String searchPattern = "%" + keyword + "%";
        
        try (ResultSet rs = DatabaseConnection.executeQuery(query, searchPattern, searchPattern, searchPattern, searchPattern)) {
            
            patientList.clear();
            
            if (rs != null) {
                while (rs.next()) {
                    patientList.add(Patient.fromResultSet(rs));
                }
            }
            
            patientTable.setItems(patientList);
            statusLabel.setText("Found " + patientList.size() + " patients");
            
        } catch (SQLException e) {
            statusLabel.setText("Error searching patients");
            e.printStackTrace();
        }
    }
    
    private void selectPatient(Patient patient) {
        selectedPatient = patient;
        
        firstNameField.setText(patient.getFirstName());
        lastNameField.setText(patient.getLastName());
        
        if (patient.getDateOfBirth() != null) {
            dobPicker.setValue(patient.getDateOfBirth().toLocalDate());
        }
        
        genderCombo.setValue(patient.getGender());
        phoneField.setText(patient.getPhone());
        emailField.setText(patient.getEmail());
        addressArea.setText(patient.getAddress());
        medicalHistoryArea.setText(patient.getMedicalHistory());
        emergencyContactField.setText(patient.getEmergencyContact());
        
        addButton.setDisable(true);
        updateButton.setDisable(false);
        deleteButton.setDisable(false);
    }
    
    @FXML
    private void addPatient() {
        if (!validateInputs()) return;
        
        String query = "INSERT INTO patients (first_name, last_name, date_of_birth, gender, phone, email, address, medical_history, emergency_contact, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        Date dob = dobPicker.getValue() != null ? Date.valueOf(dobPicker.getValue()) : null;
        
        int result = DatabaseConnection.executeUpdate(query,
            firstNameField.getText().trim(),
            lastNameField.getText().trim(),
            dob,
            genderCombo.getValue(),
            phoneField.getText().trim(),
            emailField.getText().trim(),
            addressArea.getText(),
            medicalHistoryArea.getText(),
            emergencyContactField.getText(),
            currentUserId
        );
        
        if (result > 0) {
            statusLabel.setText("Patient added successfully!");
            clearForm();
            loadPatients();
        } else {
            statusLabel.setText("Error adding patient");
        }
    }
    
    @FXML
    private void updatePatient() {
        if (selectedPatient == null) {
            statusLabel.setText("Please select a patient to update");
            return;
        }
        
        if (!validateInputs()) return;
        
        String query = "UPDATE patients SET first_name=?, last_name=?, date_of_birth=?, gender=?, phone=?, email=?, address=?, medical_history=?, emergency_contact=? WHERE patient_id=?";
        
        Date dob = dobPicker.getValue() != null ? Date.valueOf(dobPicker.getValue()) : null;
        
        int result = DatabaseConnection.executeUpdate(query,
            firstNameField.getText().trim(),
            lastNameField.getText().trim(),
            dob,
            genderCombo.getValue(),
            phoneField.getText().trim(),
            emailField.getText().trim(),
            addressArea.getText(),
            medicalHistoryArea.getText(),
            emergencyContactField.getText(),
            selectedPatient.getPatientId()
        );
        
        if (result > 0) {
            statusLabel.setText("Patient updated successfully!");
            clearForm();
            loadPatients();
        } else {
            statusLabel.setText("Error updating patient");
        }
    }
    
    @FXML
    private void deletePatient() {
        if (selectedPatient == null) {
            statusLabel.setText("Please select a patient to delete");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Patient");
        alert.setContentText("Are you sure you want to delete " + selectedPatient.getFullName() + "? This will also delete related records.");
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            
            String query = "DELETE FROM patients WHERE patient_id=?";
            int result = DatabaseConnection.executeUpdate(query, selectedPatient.getPatientId());
            
            if (result > 0) {
                statusLabel.setText("Patient deleted successfully!");
                clearForm();
                loadPatients();
            } else {
                statusLabel.setText("Error deleting patient");
            }
        }
    }
    
    @FXML
    private void clearForm() {
        firstNameField.clear();
        lastNameField.clear();
        dobPicker.setValue(null);
        genderCombo.setValue(null);
        phoneField.clear();
        emailField.clear();
        addressArea.clear();
        medicalHistoryArea.clear();
        emergencyContactField.clear();
        
        selectedPatient = null;
        addButton.setDisable(false);
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
        
        patientTable.getSelectionModel().clearSelection();
        statusLabel.setText("Form cleared");
    }
    
    private boolean validateInputs() {
        if (firstNameField.getText().trim().isEmpty()) {
            statusLabel.setText("First name is required");
            return false;
        }
        if (lastNameField.getText().trim().isEmpty()) {
            statusLabel.setText("Last name is required");
            return false;
        }
        if (phoneField.getText().trim().isEmpty()) {
            statusLabel.setText("Phone number is required");
            return false;
        }
        return true;
    }
}