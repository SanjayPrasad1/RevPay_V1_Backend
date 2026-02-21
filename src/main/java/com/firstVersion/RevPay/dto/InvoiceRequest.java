package com.firstVersion.RevPay.dto;

import java.time.LocalDate;
import java.util.List;

public class InvoiceRequest {
    private String customerEmail;
    private String customerName;
    private LocalDate dueDate;
    private List<InvoiceItemDTO> items; // This uses the class we just created

    public InvoiceRequest() {}

    // Getters and Setters
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public List<InvoiceItemDTO> getItems() { return items; }
    public void setItems(List<InvoiceItemDTO> items) { this.items = items; }
}