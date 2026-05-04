package controller;

import db.DatabaseConnection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Appointment;
import model.Patient;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;

public class AppointmentController {
    
    @FXML private DatePicker dateFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private TableView<Appointment> appointmentTable;
    @FXML private TableColumn<Appointment, Integer> colId;
    @FXML private TableColumn<Appointment, String> colPatientName;
    @FXML private TableColumn<Appointment, Date> colDate;
    @FXML private TableColumn<Appointment, Time> colTime;
    @FXML private TableColumn<Appointment, String> colStatus;
    @FXML private TableColumn<Appointment, String> colReason;
    
    @FXML private ComboBox<Patient> patientCombo;
    @FXML private DatePicker appointmentDatePicker;
    @FXML private ComboBox<String> hourCombo;
    @FXML private ComboBox<String> minuteCombo;
    @FXML private ComboBox<String> ampmCombo;
    @FXML private ComboBox<Integer> durationCombo;
    @FXML private TextArea reasonArea;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextArea notesArea;
    
    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button completeButton;
    @FXML private Button cancelButton;
    @FXML private Button clearButton;
    @FXML private Label statusLabel;
    
    private ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();
    private ObservableList<Patient> patientList = FXCollections.observableArrayList();
    private Appointment selectedAppointment;
    private int currentUserId;
    private String currentUsername;
    private String currentRole;
    
    @FXML
    public void initialize() {
        // Initialize table columns
        colId.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        colPatientName.setCellValueFactory(new PropertyValueFactory<>("patient"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("appointmentDate"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("appointmentTime"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
        
        // Initialize combo boxes
        statusFilter.setItems(FXCollections.observableArrayList("All", "Scheduled", "Completed", "Cancelled", "No-Show"));
        statusFilter.setValue("All");
        
        // Time picker combos
        hourCombo.setItems(FXCollections.observableArrayList("01","02","03","04","05","06","07","08","09","10","11","12"));
        minuteCombo.setItems(FXCollections.observableArrayList("00","15","30","45"));
        ampmCombo.setItems(FXCollections.observableArrayList("AM","PM"));
        durationCombo.setItems(FXCollections.observableArrayList(15,30,45,60,90,120));
        durationCombo.setValue(30);
        
        statusCombo.setItems(FXCollections.observableArrayList("Scheduled", "Completed", "Cancelled", "No-Show"));
        statusCombo.setValue("Scheduled");
        
        // Load data

        Platform.runLater(() -> {
            loadPatients();
            loadAppointments();
        });
        // loadPatients();
        // loadAppointments();
        
        // Set current date
        appointmentDatePicker.setValue(LocalDate.now());
        dateFilter.setValue(LocalDate.now());
        
        // Set default time to current time rounded up
        setDefaultTime();
        
        // Table selection listener
        appointmentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectAppointment(newSelection);
            }
        });
        
        // Filter listeners
        dateFilter.valueProperty().addListener((obs, oldDate, newDate) -> filterAppointments());
        statusFilter.valueProperty().addListener((obs, oldStatus, newStatus) -> filterAppointments());
    }
    
    public void setUserInfo(int userId, String username, String role) {
        this.currentUserId = userId;
        this.currentUsername = username;
        this.currentRole = role;
    }
    
    private void setDefaultTime() {
        LocalTime now = LocalTime.now();
        int hour = now.getHour();
        int minute = now.getMinute();
        
        // Round up to next 15 minutes
        if (minute > 45) {
            hour++;
            minute = 0;
        } else if (minute > 30) {
            minute = 45;
        } else if (minute > 15) {
            minute = 30;
        } else if (minute > 0) {
            minute = 15;
        }
        
        int displayHour = hour % 12;
        if (displayHour == 0) displayHour = 12;
        hourCombo.setValue(String.format("%02d", displayHour));
        minuteCombo.setValue(String.format("%02d", minute));
        ampmCombo.setValue(hour < 12 ? "AM" : "PM");
    }
    
    private void loadPatients() {
        String query = "SELECT * FROM patients ORDER BY first_name";
        try (ResultSet rs = DatabaseConnection.executeQuery(query)) {
            patientList.clear();
            while (rs != null && rs.next()) {
                patientList.add(Patient.fromResultSet(rs));
            }
            patientCombo.setItems(patientList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void loadAppointments() {
        String query = "SELECT a.*, p.first_name, p.last_name, p.phone FROM appointments a JOIN patients p ON a.patient_id = p.patient_id ORDER BY a.appointment_date DESC, a.appointment_time DESC";
        loadAppointmentsWithQuery(query);
    }
    
    private void filterAppointments() {
        LocalDate filterDate = dateFilter.getValue();
        String status = statusFilter.getValue();
        
        if (status.equals("All")) {
            String query = "SELECT a.*, p.first_name, p.last_name, p.phone FROM appointments a JOIN patients p ON a.patient_id = p.patient_id WHERE a.appointment_date = ? ORDER BY a.appointment_time";
            if (filterDate != null) {
                loadAppointmentsWithQuery(query, Date.valueOf(filterDate));
            }
        } else {
            String query = "SELECT a.*, p.first_name, p.last_name, p.phone FROM appointments a JOIN patients p ON a.patient_id = p.patient_id WHERE a.appointment_date = ? AND a.status = ? ORDER BY a.appointment_time";
            if (filterDate != null) {
                loadAppointmentsWithQuery(query, Date.valueOf(filterDate), status);
            }
        }
    }
    
    private void loadAppointmentsWithQuery(String query, Object... params) {
        try (ResultSet rs = DatabaseConnection.executeQuery(query, params)) {
            appointmentList.clear();
            while (rs != null && rs.next()) {
                Appointment apt = Appointment.fromResultSet(rs);
                // Load patient info
                Patient p = new Patient();
                p.setPatientId(rs.getInt("patient_id"));
                p.setFirstName(rs.getString("first_name"));
                p.setLastName(rs.getString("last_name"));
                p.setPhone(rs.getString("phone"));
                apt.setPatient(p);
                appointmentList.add(apt);
            }
            appointmentTable.setItems(appointmentList);
            statusLabel.setText("Showing " + appointmentList.size() + " appointments");
        } catch (SQLException e) {
            statusLabel.setText("Error loading appointments");
            e.printStackTrace();
        }
    }
    
    private void selectAppointment(Appointment appointment) {
        selectedAppointment = appointment;
        
        // Find patient in combo box
        for (Patient p : patientList) {
            if (p.getPatientId() == appointment.getPatientId()) {
                patientCombo.setValue(p);
                break;
            }
        }
        
        appointmentDatePicker.setValue(appointment.getAppointmentDate().toLocalDate());
        
        // Set time
        LocalTime time = appointment.getAppointmentTime().toLocalTime();
        int hour = time.getHour();
        int minute = time.getMinute();
        int displayHour = hour % 12;
        if (displayHour == 0) displayHour = 12;
        hourCombo.setValue(String.format("%02d", displayHour));
        minuteCombo.setValue(String.format("%02d", minute));
        ampmCombo.setValue(hour < 12 ? "AM" : "PM");
        
        durationCombo.setValue(appointment.getDurationMinutes());
        reasonArea.setText(appointment.getReason());
        statusCombo.setValue(appointment.getStatus());
        notesArea.setText(appointment.getNotes());
        
        addButton.setDisable(true);
        updateButton.setDisable(false);
        deleteButton.setDisable(false);
        completeButton.setDisable(false);
        cancelButton.setDisable(false);
    }
    
    private Time getSelectedTime() {
        int hour = Integer.parseInt(hourCombo.getValue());
        int minute = Integer.parseInt(minuteCombo.getValue());
        String ampm = ampmCombo.getValue();
        
        if (ampm.equals("PM") && hour != 12) {
            hour += 12;
        } else if (ampm.equals("AM") && hour == 12) {
            hour = 0;
        }
        
        return Time.valueOf(LocalTime.of(hour, minute));
    }
    
    @FXML
    private void addAppointment() {
        if (!validateInputs()) return;
        
        String query = "INSERT INTO appointments (patient_id, appointment_date, appointment_time, duration_minutes, reason, status, notes, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        Patient selectedPatient = patientCombo.getValue();
        Date aptDate = Date.valueOf(appointmentDatePicker.getValue());
        Time aptTime = getSelectedTime();
        int duration = durationCombo.getValue();
        String reason = reasonArea.getText();
        String status = statusCombo.getValue();
        String notes = notesArea.getText();
        
        int result = DatabaseConnection.executeUpdate(query, 
            selectedPatient.getPatientId(), aptDate, aptTime, duration, reason, status, notes, currentUserId);
        
        if (result > 0) {
            statusLabel.setText("Appointment added successfully!");
            clearForm();
            loadAppointments();
            filterAppointments();
        } else {
            statusLabel.setText("Error adding appointment");
        }
    }
    
    @FXML
    private void updateAppointment() {
        if (selectedAppointment == null) {
            statusLabel.setText("Please select an appointment to update");
            return;
        }
        
        if (!validateInputs()) return;
        
        String query = "UPDATE appointments SET patient_id=?, appointment_date=?, appointment_time=?, duration_minutes=?, reason=?, status=?, notes=? WHERE appointment_id=?";
        
        Patient selectedPatient = patientCombo.getValue();
        Date aptDate = Date.valueOf(appointmentDatePicker.getValue());
        Time aptTime = getSelectedTime();
        int duration = durationCombo.getValue();
        
        int result = DatabaseConnection.executeUpdate(query,
            selectedPatient.getPatientId(), aptDate, aptTime, duration, reasonArea.getText(), statusCombo.getValue(), notesArea.getText(), selectedAppointment.getAppointmentId());
        
        if (result > 0) {
            statusLabel.setText("Appointment updated successfully!");
            clearForm();
            loadAppointments();
            filterAppointments();
        } else {
            statusLabel.setText("Error updating appointment");
        }
    }
    
    @FXML
    private void deleteAppointment() {
        if (selectedAppointment == null) {
            statusLabel.setText("Please select an appointment to delete");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Appointment");
        alert.setContentText("Are you sure you want to delete this appointment?");
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            String query = "DELETE FROM appointments WHERE appointment_id=?";
            int result = DatabaseConnection.executeUpdate(query, selectedAppointment.getAppointmentId());
            
            if (result > 0) {
                statusLabel.setText("Appointment deleted successfully!");
                clearForm();
                loadAppointments();
                filterAppointments();
            } else {
                statusLabel.setText("Error deleting appointment");
            }
        }
    }
    
    @FXML
    private void completeAppointment() {
        if (selectedAppointment == null) {
            statusLabel.setText("Please select an appointment");
            return;
        }
        
        String query = "UPDATE appointments SET status='Completed' WHERE appointment_id=?";
        int result = DatabaseConnection.executeUpdate(query, selectedAppointment.getAppointmentId());
        
        if (result > 0) {
            statusLabel.setText("Appointment marked as completed!");
            clearForm();
            loadAppointments();
            filterAppointments();
        }
    }
    
    @FXML
    private void cancelAppointment() {
        if (selectedAppointment == null) {
            statusLabel.setText("Please select an appointment");
            return;
        }
        
        String query = "UPDATE appointments SET status='Cancelled' WHERE appointment_id=?";
        int result = DatabaseConnection.executeUpdate(query, selectedAppointment.getAppointmentId());
        
        if (result > 0) {
            statusLabel.setText("Appointment cancelled!");
            clearForm();
            loadAppointments();
            filterAppointments();
        }
    }
    
    @FXML
    private void clearForm() {
        patientCombo.setValue(null);
        appointmentDatePicker.setValue(LocalDate.now());
        setDefaultTime();
        durationCombo.setValue(30);
        reasonArea.clear();
        statusCombo.setValue("Scheduled");
        notesArea.clear();
        
        selectedAppointment = null;
        addButton.setDisable(false);
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
        completeButton.setDisable(true);
        cancelButton.setDisable(true);
        appointmentTable.getSelectionModel().clearSelection();
        statusLabel.setText("Form cleared");
    }
    
    private boolean validateInputs() {
        if (patientCombo.getValue() == null) {
            statusLabel.setText("Please select a patient");
            return false;
        }
        if (appointmentDatePicker.getValue() == null) {
            statusLabel.setText("Please select appointment date");
            return false;
        }
        return true;
    }
}