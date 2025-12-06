package com.example.capstone_swastik;

public class Procurement {
    private String supplierId, supplierName, date, status, paymentDate, userUID, billNumber; // added billNumber
    private boolean notify;
    private int quantity, pricePerUnit, totalAmount;
    private long timestamp; // added timestamp

    public Procurement() {}

    public String getSupplierId() { return supplierId; }
    public String getSupplierName() { return supplierName; }
    public String getDate() { return date; }
    public String getStatus() { return status; }
    public String getPaymentDate() { return paymentDate; }
    public boolean isNotify() { return notify; }
    public int getQuantity() { return quantity; }
    public int getPricePerUnit() { return pricePerUnit; }
    public int getTotalAmount() { return totalAmount; }
    public long getTimestamp() { return timestamp; }
    public String getUserUID() { return userUID; }
    public String getBillNumber() { return billNumber; } // getter for billNumber

    public void setBillNumber(String billNumber) { this.billNumber = billNumber; } // optional setter
}
