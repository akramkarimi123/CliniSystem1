package controller;

import db.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Billing;
import model.Patient;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class BillingController {
    
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private TableView<Billing> billingTable;
    @FXML private TableColumn<Billing, Integer> colId;
    @FXML private TableColumn<Billing, String> colInvoiceNo;
    @FXML private TableColumn<Billing, String> colPatientName;
    @FXML private TableColumn<Billing, Double> colTotal;
    @FXML private TableColumn<Billing, Double> colPaid;
    @FXML private TableColumn<Billing, Double> colBalance;
    @FXML private TableColumn<Billing, String> colStatus;
    @FXML private TableColumn<Billing, Date> colDate;
    
    @FXML private ComboBox<Patient> patientCombo;
    @FXML private TextField totalAmountField;
    @FXML private TextField paidAmountField;
    @FXML private TextField discountField;
    @FXML private TextField taxField;
    @FXML private ComboBox<String> paymentStatusCombo;
    @FXML private ComboBox<String> paymentMethodCombo;
    @FXML private DatePicker paymentDatePicker;
    @FXML private TextArea notesArea;
    @FXML private Label balanceLabel;
    
    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button printButton;
    @FXML private Button clearButton;
    @FXML private Label statusLabel;
    
    private ObservableList<Billing> billingList = FXCollections.observableArrayList();
    private ObservableList<Patient> patientList = FXCollections.observableArrayList();
    private Billing selectedBilling;
    private int currentUserId;
    private String currentUsername;
    private String currentRole;
    
    @FXML
    public void initialize() {
        // Initialize table columns
        colId.setCellValueFactory(new PropertyValueFactory<>("billId"));
        colInvoiceNo.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));
        colPatientName.setCellValueFactory(new PropertyValueFactory<>("patient"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colPaid.setCellValueFactory(new PropertyValueFactory<>("paidAmount"));
        colBalance.setCellValueFactory(new PropertyValueFactory<>("balanceDue"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        
        // Custom cell factories for currency formatting
        colTotal.setCellFactory(column -> new TableCell<Billing, Double>() {
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
        
        colPaid.setCellFactory(column -> new TableCell<Billing, Double>() {
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
        
        colBalance.setCellFactory(column -> new TableCell<Billing, Double>() {
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
        
        // Initialize combo boxes
        statusFilter.setItems(FXCollections.observableArrayList("All", "Pending", "Partial", "Paid", "Refunded"));
        statusFilter.setValue("All");
        
        paymentStatusCombo.setItems(FXCollections.observableArrayList("Pending", "Partial", "Paid", "Refunded"));
        paymentStatusCombo.setValue("Pending");
        
        paymentMethodCombo.setItems(FXCollections.observableArrayList("Cash", "Credit Card", "Debit Card", "Insurance", "Other"));
        
        // Set default values
        paymentDatePicker.setValue(LocalDate.now());
        discountField.setText("0");
        taxField.setText("0");
        
        // Add listeners for real-time balance calculation
        totalAmountField.textProperty().addListener((obs, oldVal, newVal) -> calculateBalance());
        paidAmountField.textProperty().addListener((obs, oldVal, newVal) -> calculateBalance());
        discountField.textProperty().addListener((obs, oldVal, newVal) -> calculateBalance());
        taxField.textProperty().addListener((obs, oldVal, newVal) -> calculateBalance());
        
        // Load data
        loadPatients();
        loadBillings();
        
        // Table selection listener
        billingTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectBilling(newSelection);
            }
        });
        
        // Filter listener
        statusFilter.valueProperty().addListener((obs, oldStatus, newStatus) -> filterBillings());
        
        // Search listener
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            searchBillings(newText);
        });
    }
    
    public void setUserInfo(int userId, String username, String role) {
        this.currentUserId = userId;
        this.currentUsername = username;
        this.currentRole = role;
    }
    
    private void calculateBalance() {
        try {
            double total = totalAmountField.getText().isEmpty() ? 0 : Double.parseDouble(totalAmountField.getText());
            double paid = paidAmountField.getText().isEmpty() ? 0 : Double.parseDouble(paidAmountField.getText());
            double discount = discountField.getText().isEmpty() ? 0 : Double.parseDouble(discountField.getText());
            double tax = taxField.getText().isEmpty() ? 0 : Double.parseDouble(taxField.getText());
            
            double afterDiscount = total - discount;
            double totalWithTax = afterDiscount + (afterDiscount * tax / 100);
            double balance = totalWithTax - paid;
            
            balanceLabel.setText(String.format("Balance Due: $%.2f", balance));
            
            // Auto-update payment status based on balance
            if (balance <= 0) {
                paymentStatusCombo.setValue("Paid");
            } else if (paid > 0) {
                paymentStatusCombo.setValue("Partial");
            } else {
                paymentStatusCombo.setValue("Pending");
            }
            
        } catch (NumberFormatException e) {
            balanceLabel.setText("Balance Due: $0.00");
        }
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
    
    private void loadBillings() {
        String query = "SELECT b.*, p.first_name, p.last_name FROM billing b JOIN patients p ON b.patient_id = p.patient_id ORDER BY b.bill_id DESC";
        loadBillingsWithQuery(query);
    }
    
    private void filterBillings() {
        String status = statusFilter.getValue();
        if (status.equals("All")) {
            loadBillings();
        } else {
            String query = "SELECT b.*, p.first_name, p.last_name FROM billing b JOIN patients p ON b.patient_id = p.patient_id WHERE b.payment_status = ? ORDER BY b.bill_id DESC";
            loadBillingsWithQuery(query, status);
        }
    }
    
    private void searchBillings(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            loadBillings();
            return;
        }
        
        String query = "SELECT b.*, p.first_name, p.last_name FROM billing b JOIN patients p ON b.patient_id = p.patient_id WHERE b.invoice_number LIKE ? OR p.first_name LIKE ? OR p.last_name LIKE ? ORDER BY b.bill_id DESC";
        String searchPattern = "%" + keyword + "%";
        
        loadBillingsWithQuery(query, searchPattern, searchPattern, searchPattern);
    }
    
    // private void loadBillingsWithQuery(String query, Object... params) {
    //     try (ResultSet rs = DatabaseConnection.executeQuery(query, params)) {
    //         billingList.clear();
    //         while (rs != null && rs.next()) {
    //             Billing bill = Billing.fromResultSet(rs);
    //             // Load patient info
    //             Patient p = new Patient();
    //             p.setPatientId(rs.getInt("patient_id"));
    //             p.setFirstName(rs.getString("first_name"));
    //             p.setLastName(rs.getString("last_name"));
    //             bill.setPatient(p);
    //             billingList.add(bill);
    //         }
    //         billingTable.setItems(billingList);
    //         statusLabel.setText("Showing " + billingList.size() + " invoices");
    //     } catch (SQLException e) {
    //         statusLabel.setText("Error loading billings");
    //         e.printStackTrace();
    //     }
    // }

    private void loadBillingsWithQuery(String query, Object... params) {
        try (ResultSet rs = DatabaseConnection.executeQuery(query, params)) {
            billingList.clear();
            int count = 0;
            while (rs != null && rs.next()) {
                count++;
                Billing bill = Billing.fromResultSet(rs);
                // Load patient info
                Patient p = new Patient();
                p.setPatientId(rs.getInt("patient_id"));
                p.setFirstName(rs.getString("first_name"));
                p.setLastName(rs.getString("last_name"));
                bill.setPatient(p);
                billingList.add(bill);
            }
            billingTable.setItems(billingList);
            statusLabel.setText("Showing " + billingList.size() + " invoices");
            System.out.println("Loaded " + billingList.size() + " billing records"); // DEBUG LINE
        } catch (SQLException e) {
            statusLabel.setText("Error loading billings");
            e.printStackTrace();
        }
        }
    
    private void selectBilling(Billing billing) {
        selectedBilling = billing;
        
        // Find patient in combo box
        for (Patient p : patientList) {
            if (p.getPatientId() == billing.getPatientId()) {
                patientCombo.setValue(p);
                break;
            }
        }
        
        totalAmountField.setText(String.valueOf(billing.getTotalAmount()));
        paidAmountField.setText(String.valueOf(billing.getPaidAmount()));
        discountField.setText(String.valueOf(billing.getDiscount()));
        taxField.setText(String.valueOf(billing.getTax()));
        paymentStatusCombo.setValue(billing.getPaymentStatus());
        paymentMethodCombo.setValue(billing.getPaymentMethod());
        if (billing.getPaymentDate() != null) {
            paymentDatePicker.setValue(billing.getPaymentDate().toLocalDate());
        }
        notesArea.setText(billing.getNotes());
        
        addButton.setDisable(true);
        updateButton.setDisable(false);
        deleteButton.setDisable(false);
        printButton.setDisable(false);
    }
    
    @FXML
    private void addBilling() {
        if (!validateInputs()) return;
        
        String query = "INSERT INTO billing (patient_id, total_amount, paid_amount, discount, tax, payment_status, payment_method, payment_date, notes, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        Patient selectedPatient = patientCombo.getValue();
        double total = Double.parseDouble(totalAmountField.getText());
        double paid = paidAmountField.getText().isEmpty() ? 0 : Double.parseDouble(paidAmountField.getText());
        double discount = discountField.getText().isEmpty() ? 0 : Double.parseDouble(discountField.getText());
        double tax = taxField.getText().isEmpty() ? 0 : Double.parseDouble(taxField.getText());
        String status = paymentStatusCombo.getValue();
        String method = paymentMethodCombo.getValue();
        Date paymentDate = paymentDatePicker.getValue() != null ? Date.valueOf(paymentDatePicker.getValue()) : null;
        String notes = notesArea.getText();
        
        int result = DatabaseConnection.executeUpdate(query,
            selectedPatient.getPatientId(), total, paid, discount, tax, status, method, paymentDate, notes, currentUserId);
        
        if (result > 0) {
            statusLabel.setText("Invoice created successfully!");
            clearForm();
            loadBillings();
        } else {
            statusLabel.setText("Error creating invoice");
        }
    }
    
    @FXML
    private void updateBilling() {
        if (selectedBilling == null) {
            statusLabel.setText("Please select an invoice to update");
            return;
        }
        
        if (!validateInputs()) return;
        
        String query = "UPDATE billing SET patient_id=?, total_amount=?, paid_amount=?, discount=?, tax=?, payment_status=?, payment_method=?, payment_date=?, notes=? WHERE bill_id=?";
        
        Patient selectedPatient = patientCombo.getValue();
        double total = Double.parseDouble(totalAmountField.getText());
        double paid = paidAmountField.getText().isEmpty() ? 0 : Double.parseDouble(paidAmountField.getText());
        double discount = discountField.getText().isEmpty() ? 0 : Double.parseDouble(discountField.getText());
        double tax = taxField.getText().isEmpty() ? 0 : Double.parseDouble(taxField.getText());
        Date paymentDate = paymentDatePicker.getValue() != null ? Date.valueOf(paymentDatePicker.getValue()) : null;
        
        int result = DatabaseConnection.executeUpdate(query,
            selectedPatient.getPatientId(), total, paid, discount, tax, paymentStatusCombo.getValue(), paymentMethodCombo.getValue(), paymentDate, notesArea.getText(), selectedBilling.getBillId());
        
        if (result > 0) {
            statusLabel.setText("Invoice updated successfully!");
            clearForm();
            loadBillings();
        } else {
            statusLabel.setText("Error updating invoice");
        }
    }
    
    @FXML
    private void deleteBilling() {
        if (selectedBilling == null) {
            statusLabel.setText("Please select an invoice to delete");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Invoice");
        alert.setContentText("Are you sure you want to delete invoice " + selectedBilling.getInvoiceNumber() + "?");
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            String query = "DELETE FROM billing WHERE bill_id=?";
            int result = DatabaseConnection.executeUpdate(query, selectedBilling.getBillId());
            
            if (result > 0) {
                statusLabel.setText("Invoice deleted successfully!");
                clearForm();
                loadBillings();
            } else {
                statusLabel.setText("Error deleting invoice");
            }
        }
    }
    
    @FXML
    private void printInvoice() {
        if (selectedBilling == null) {
            statusLabel.setText("Please select an invoice to print");
            return;
        }
        
        // Create a simple print dialog
        String invoiceDetails = 
            "====================================\n" +
            "     KARIMI DENTAL CLINIC\n" +
            "====================================\n" +
            "Invoice No: " + selectedBilling.getInvoiceNumber() + "\n" +
            "Date: " + selectedBilling.getCreatedAt() + "\n" +
            "------------------------------------\n" +
            "Patient: " + (selectedBilling.getPatient() != null ? selectedBilling.getPatient().getFullName() : "N/A") + "\n" +
            "------------------------------------\n" +
            "Total Amount: $" + String.format("%.2f", selectedBilling.getTotalAmount()) + "\n" +
            "Discount: $" + String.format("%.2f", selectedBilling.getDiscount()) + "\n" +
            "Tax: $" + String.format("%.2f", selectedBilling.getTax()) + "\n" +
            "Paid Amount: $" + String.format("%.2f", selectedBilling.getPaidAmount()) + "\n" +
            "Balance Due: $" + String.format("%.2f", selectedBilling.getBalanceDue()) + "\n" +
            "------------------------------------\n" +
            "Status: " + selectedBilling.getPaymentStatus() + "\n" +
            "Payment Method: " + (selectedBilling.getPaymentMethod() != null ? selectedBilling.getPaymentMethod() : "N/A") + "\n" +
            "====================================\n";
        
        Alert printAlert = new Alert(Alert.AlertType.INFORMATION);
        printAlert.setTitle("Invoice Details");
        printAlert.setHeaderText("Invoice #" + selectedBilling.getInvoiceNumber());
        printAlert.setContentText(invoiceDetails);
        printAlert.showAndWait();
        
        statusLabel.setText("Invoice printed (simulated)");
    }
    
    @FXML
    private void clearForm() {
        patientCombo.setValue(null);
        totalAmountField.clear();
        paidAmountField.clear();
        discountField.setText("0");
        taxField.setText("0");
        paymentStatusCombo.setValue("Pending");
        paymentMethodCombo.setValue(null);
        paymentDatePicker.setValue(LocalDate.now());
        notesArea.clear();
        balanceLabel.setText("Balance Due: $0.00");
        
        selectedBilling = null;
        addButton.setDisable(false);
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
        printButton.setDisable(true);
        billingTable.getSelectionModel().clearSelection();
        statusLabel.setText("Form cleared");
    }
    
    private boolean validateInputs() {
        if (patientCombo.getValue() == null) {
            statusLabel.setText("Please select a patient");
            return false;
        }
        if (totalAmountField.getText().isEmpty()) {
            statusLabel.setText("Total amount is required");
            return false;
        }
        try {
            double total = Double.parseDouble(totalAmountField.getText());
            if (total <= 0) {
                statusLabel.setText("Total amount must be greater than 0");
                return false;
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid total amount");
            return false;
        }
        if (paymentMethodCombo.getValue() == null) {
            statusLabel.setText("Please select payment method");
            return false;
        }
        return true;
    }
}