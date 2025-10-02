package com.example.capstone_swastik;

public class ProductModel {
    String id;
    int img;
    String p_name,p_price;
    String description;
    public ProductModel(String id,int img ,String p_name,String p_price,String description){
        this.id = id ;
        this.img = img;
        this.p_name = p_name;
        this.p_price = p_price;
        this.description = description;
    }
}
