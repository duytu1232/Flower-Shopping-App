package com.example.flowerapp.Models;

public class Coupon {
    private int id;
    private String code, startDate, endDate, status;
    private double discountValue;

    public Coupon(int id, String code, double discountValue, String startDate, String endDate, String status) {
        this.id = id;
        this.code = code;
        this.discountValue = discountValue;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public int getId() { return id; }
    public String getCode() { return code; }
    public double getDiscountValue() { return discountValue; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getStatus() { return status; }
}