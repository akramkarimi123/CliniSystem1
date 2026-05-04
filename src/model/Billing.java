package model;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Billing {
    
    // Fields
    private int billId;
    private int patientId;
    private Patient patient; // For joined data
    private Integer appointmentId;
    private Appointment appointment; // For joined data
    private Integer patientTreatmentId;
    private double totalAmount;
    private double paidAmount;
    private double discount;
    private double tax;
    private String paymentStatus; // Pending, Partial, Paid, Refunded
    private String paymentMethod; // Cash, Credit Card, Debit Card, Insurance, Other
    private Date paymentDate;
    private String invoiceNumber;
    private String notes;
    private int createdBy;
    private Date createdAt;
    private Date updatedAt;
    
    // Constructors
    public Billing() {}
    
    public Billing(int patientId, double totalAmount, double paidAmount, 
                   double discount, double tax, String paymentStatus, 
                   String paymentMethod, Date paymentDate, String notes, int createdBy) {
        this.patientId = patientId;
        this.totalAmount = totalAmount;
        this.paidAmount = paidAmount;
        this.discount = discount;
        this.tax = tax;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
        this.paymentDate = paymentDate;
        this.notes = notes;
        this.createdBy = createdBy;
    }
    
    // Getters and Setters
    public int getBillId() {
        return billId;
    }
    
    public void setBillId(int billId) {
        this.billId = billId;
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
    
    public Integer getAppointmentId() {
        return appointmentId;
    }
    
    public void setAppointmentId(Integer appointmentId) {
        this.appointmentId = appointmentId;
    }
    
    public Appointment getAppointment() {
        return appointment;
    }
    
    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
        if (appointment != null) {
            this.appointmentId = appointment.getAppointmentId();
        }
    }
    
    public Integer getPatientTreatmentId() {
        return patientTreatmentId;
    }
    
    public void setPatientTreatmentId(Integer patientTreatmentId) {
        this.patientTreatmentId = patientTreatmentId;
    }
    
    public double getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public double getPaidAmount() {
        return paidAmount;
    }
    
    public void setPaidAmount(double paidAmount) {
        this.paidAmount = paidAmount;
    }
    
    public double getDiscount() {
        return discount;
    }
    
    public void setDiscount(double discount) {
        this.discount = discount;
    }
    
    public double getTax() {
        return tax;
    }
    
    public void setTax(double tax) {
        this.tax = tax;
    }
    
    public String getPaymentStatus() {
        return paymentStatus;
    }
    
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public Date getPaymentDate() {
        return paymentDate;
    }
    
    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }
    
    public String getInvoiceNumber() {
        return invoiceNumber;
    }
    
    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
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
    public double getBalanceDue() {
        return totalAmount - paidAmount;
    }
    
    public double getSubtotal() {
        return totalAmount - tax + discount;
    }
    
    public String getFormattedTotal() {
        return String.format("$%.2f", totalAmount);
    }
    
    public String getFormattedPaid() {
        return String.format("$%.2f", paidAmount);
    }
    
    public String getFormattedBalance() {
        return String.format("$%.2f", getBalanceDue());
    }
    
    public boolean isFullyPaid() {
        return getBalanceDue() <= 0 && "Paid".equals(paymentStatus);
    }
    
    public boolean isOverdue() {
        return getBalanceDue() > 0 && paymentDate != null && 
               paymentDate.toLocalDate().isBefore(java.time.LocalDate.now());
    }
    
    // Calculate total with tax and discount
    public static double calculateTotal(double amount, double discount, double tax) {
        double afterDiscount = amount - discount;
        return afterDiscount + (afterDiscount * tax / 100);
    }
    
    // Convert ResultSet to Billing Object
    public static Billing fromResultSet(ResultSet rs) throws SQLException {
        Billing billing = new Billing();
        billing.setBillId(rs.getInt("bill_id"));
        billing.setPatientId(rs.getInt("patient_id"));
        billing.setAppointmentId(rs.getObject("appointment_id") != null ? rs.getInt("appointment_id") : null);
        billing.setPatientTreatmentId(rs.getObject("patient_treatment_id") != null ? rs.getInt("patient_treatment_id") : null);
        billing.setTotalAmount(rs.getDouble("total_amount"));
        billing.setPaidAmount(rs.getDouble("paid_amount"));
        billing.setDiscount(rs.getDouble("discount"));
        billing.setTax(rs.getDouble("tax"));
        billing.setPaymentStatus(rs.getString("payment_status"));
        billing.setPaymentMethod(rs.getString("payment_method"));
        billing.setPaymentDate(rs.getDate("payment_date"));
        billing.setInvoiceNumber(rs.getString("invoice_number"));
        billing.setNotes(rs.getString("notes"));
        billing.setCreatedBy(rs.getInt("created_by"));
        billing.setCreatedAt(rs.getDate("created_at"));
        billing.setUpdatedAt(rs.getDate("updated_at"));
        
        // If patient data is joined
        try {
            if (rs.getString("first_name") != null) {
                Patient patient = new Patient();
                patient.setPatientId(rs.getInt("patient_id"));
                patient.setFirstName(rs.getString("first_name"));
                patient.setLastName(rs.getString("last_name"));
                billing.setPatient(patient);
            }
        } catch (SQLException e) {
            // Patient data not joined, ignore
        }
        
        return billing;
    }
    
    // Convert List of ResultSet to List of Billings
    public static List<Billing> fromResultSetList(ResultSet rs) throws SQLException {
        List<Billing> billings = new ArrayList<>();
        while (rs.next()) {
            billings.add(fromResultSet(rs));
        }
        return billings;
    }
    
    @Override
    public String toString() {
        return invoiceNumber + " - " + getFormattedTotal() + " (" + paymentStatus + ")";
    }
}