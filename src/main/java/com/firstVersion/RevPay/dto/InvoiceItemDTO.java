package com.firstVersion.RevPay.dto;

import java.math.BigDecimal;

public class InvoiceItemDTO {
    private String description;
    private Integer quantity;
    private BigDecimal unitPrice;

    // Default Constructor
    public InvoiceItemDTO() {
    }

    // All-args Constructor
    public InvoiceItemDTO(String description, Integer quantity, BigDecimal unitPrice) {
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // Getters and Setters
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
}