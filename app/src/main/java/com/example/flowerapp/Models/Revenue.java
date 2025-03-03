package com.example.flowerapp.Models;

public class Revenue {
    private String paymentMethod;
    private float amount;
    private String paymentDate;

    public Revenue(String paymentMethod, float amount, String paymentDate) {
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.paymentDate = paymentDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public float getAmount() {
        return amount;
    }

    public String getPaymentDate() {
        return paymentDate;
    }
}
