package com.example.capstone_swastik;

public class ProductModel {
    String id;
    int img;
    String rawName;       // original name
    String rawPrice;      // original price
    String displayName;   // formatted for UI
    String displayPrice;  // formatted for UI
    String description;

    public ProductModel(String id, int img, String rawName, String rawPrice, String description) {
        this.id = id;
        this.img = img;
        this.rawName = rawName;
        this.rawPrice = rawPrice;
        this.description = description;

        // UI formatting
        this.displayName = rawName ;
        this.displayPrice = "₹ " + rawPrice + " per kg";
    }
}
