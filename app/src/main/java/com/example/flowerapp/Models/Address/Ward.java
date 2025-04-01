package com.example.flowerapp.Models.Address;

import com.google.gson.annotations.SerializedName;

public class Ward {
    @SerializedName("name")
    private String name;

    @SerializedName("code")
    private int code;

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return name;
    }
}
