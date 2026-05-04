package model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Treatment {
    
    // Fields
    private int treatmentId;
    private String treatmentName;
    private String description;
    private double cost;
    private int durationMinutes;
    private java.sql.Timestamp createdAt;
    private java.sql.Timestamp updatedAt;
    
    // Constructors
    public Treatment() {}
    
    public Treatment(String treatmentName, String description, double cost, int durationMinutes) {
        this.treatmentName = treatmentName;
        this.description = description;
        this.cost = cost;
        this.durationMinutes = durationMinutes;
    }
    
    // Getters and Setters
    public int getTreatmentId() {
        return treatmentId;
    }
    
    public void setTreatmentId(int treatmentId) {
        this.treatmentId = treatmentId;
    }
    
    public String getTreatmentName() {
        return treatmentName;
    }
    
    public void setTreatmentName(String treatmentName) {
        this.treatmentName = treatmentName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public double getCost() {
        return cost;
    }
    
    public void setCost(double cost) {
        this.cost = cost;
    }
    
    public int getDurationMinutes() {
        return durationMinutes;
    }
    
    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
    
    public java.sql.Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(java.sql.Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public java.sql.Timestamp getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(java.sql.Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Helper Methods
    public String getFormattedCost() {
        return String.format("$%.2f", cost);
    }
    
    public String getFormattedDuration() {
        if (durationMinutes < 60) {
            return durationMinutes + " min";
        } else {
            int hours = durationMinutes / 60;
            int minutes = durationMinutes % 60;
            if (minutes == 0) {
                return hours + " hour" + (hours > 1 ? "s" : "");
            } else {
                return hours + "h " + minutes + "m";
            }
        }
    }
    
    // Convert ResultSet to Treatment Object
    public static Treatment fromResultSet(ResultSet rs) throws SQLException {
        Treatment treatment = new Treatment();
        treatment.setTreatmentId(rs.getInt("treatment_id"));
        treatment.setTreatmentName(rs.getString("treatment_name"));
        treatment.setDescription(rs.getString("description"));
        treatment.setCost(rs.getDouble("cost"));
        treatment.setDurationMinutes(rs.getInt("duration_minutes"));
        treatment.setCreatedAt(rs.getTimestamp("created_at"));
        treatment.setUpdatedAt(rs.getTimestamp("updated_at"));
        return treatment;
    }
    
    // Convert List of ResultSet to List of Treatments
    public static List<Treatment> fromResultSetList(ResultSet rs) throws SQLException {
        List<Treatment> treatments = new ArrayList<>();
        while (rs.next()) {
            treatments.add(fromResultSet(rs));
        }
        return treatments;
    }
    
    @Override
    public String toString() {
        return treatmentName + " - " + getFormattedCost();
    }
}
