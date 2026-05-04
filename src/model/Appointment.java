package model;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Appointment {
    
    // Fields
    private int appointmentId;
    private int patientId;
    private Patient patient; // For joined data
    private Date appointmentDate;
    private Time appointmentTime;
    private int durationMinutes;
    private String reason;
    private String status; // Scheduled, Completed, Cancelled, No-Show
    private String notes;
    private int createdBy;
    private Date createdAt;
    private Date updatedAt;
    
    // Constructors
    public Appointment() {}
    
    public Appointment(int patientId, Date appointmentDate, Time appointmentTime, 
                       int durationMinutes, String reason, String status, 
                       String notes, int createdBy) {
        this.patientId = patientId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.durationMinutes = durationMinutes;
        this.reason = reason;
        this.status = status;
        this.notes = notes;
        this.createdBy = createdBy;
    }
    
    // Getters and Setters
    public int getAppointmentId() {
        return appointmentId;
    }
    
    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }
    
    public int getPatientId() {
        return patientId;
    }
    
    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }
    
    public Patient getPatient() {
        return patient;
    }
    
    public void setPatient(Patient patient) {
        this.patient = patient;
        if (patient != null) {
            this.patientId = patient.getPatientId();
        }
    }
    
    public Date getAppointmentDate() {
        return appointmentDate;
    }
    
    public void setAppointmentDate(Date appointmentDate) {
        this.appointmentDate = appointmentDate;
    }
    
    public Time getAppointmentTime() {
        return appointmentTime;
    }
    
    public void setAppointmentTime(Time appointmentTime) {
        this.appointmentTime = appointmentTime;
    }
    
    public int getDurationMinutes() {
        return durationMinutes;
    }
    
    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public int getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Helper Methods
    public String getFormattedTime() {
        if (appointmentTime == null) return "";
        return appointmentTime.toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"));
    }
    
    public Time getEndTime() {
        if (appointmentTime == null) return null;
        LocalTime start = appointmentTime.toLocalTime();
        LocalTime end = start.plusMinutes(durationMinutes);
        return Time.valueOf(end);
    }
    
    public boolean isToday() {
        if (appointmentDate == null) return false;
        return appointmentDate.toLocalDate().equals(java.time.LocalDate.now());
    }
    
    // Convert ResultSet to Appointment Object
    public static Appointment fromResultSet(ResultSet rs) throws SQLException {
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(rs.getInt("appointment_id"));
        appointment.setPatientId(rs.getInt("patient_id"));
        appointment.setAppointmentDate(rs.getDate("appointment_date"));
        appointment.setAppointmentTime(rs.getTime("appointment_time"));
        appointment.setDurationMinutes(rs.getInt("duration_minutes"));
        appointment.setReason(rs.getString("reason"));
        appointment.setStatus(rs.getString("status"));
        appointment.setNotes(rs.getString("notes"));
        appointment.setCreatedBy(rs.getInt("created_by"));
        appointment.setCreatedAt(rs.getDate("created_at"));
        appointment.setUpdatedAt(rs.getDate("updated_at"));
        
        // If patient data is joined
        try {
            if (rs.getString("first_name") != null) {
                Patient patient = new Patient();
                patient.setPatientId(rs.getInt("patient_id"));
                patient.setFirstName(rs.getString("first_name"));
                patient.setLastName(rs.getString("last_name"));
                patient.setPhone(rs.getString("phone"));
                appointment.setPatient(patient);
            }
        } catch (SQLException e) {
            // Patient data not joined, ignore
        }
        
        return appointment;
    }
    
    // Convert List of ResultSet to List of Appointments
    public static List<Appointment> fromResultSetList(ResultSet rs) throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        while (rs.next()) {
            appointments.add(fromResultSet(rs));
        }
        return appointments;
    }
    
    @Override
    public String toString() {
        return appointmentDate + " " + getFormattedTime() + " - " + 
               (patient != null ? patient.getFullName() : "Patient ID: " + patientId);
    }
}