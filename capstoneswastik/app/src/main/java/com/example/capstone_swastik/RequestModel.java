package com.example.capstone_swastik;

public class RequestModel {
    private String productName;
    private String areaInAcres;
    private String growthMonths;   // ⭐ NEW FIELD
    private String location;
    private String supplierName;
    private String status;
    private String visitDate;

    public RequestModel() {} // Required by Firestore

    public RequestModel(String productName, String areaInAcres, String growthMonths,
                        String location, String supplierName, String status, String visitDate) {
        this.productName = productName;
        this.areaInAcres = areaInAcres;
        this.growthMonths = growthMonths;
        this.location = location;
        this.supplierName = supplierName;
        this.status = status;
        this.visitDate = visitDate;
    }

    public String getProductName() { return productName; }
    public String getAreaInAcres() { return areaInAcres; }
    public String getGrowthMonths() { return growthMonths; }
    public String getLocation() { return location; }
    public String getSupplierName() { return supplierName; }
    public String getStatus() { return status; }
    public String getVisitDate() { return visitDate; }
}
