package com.example.flowerapp.Models.Address;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class District {
    @SerializedName("name")
    private String name;

    @SerializedName("code")
    private int code;

    @SerializedName("wards")
    private List<Ward> wards;

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }

    public List<Ward> getWards() {
        return wards;
    }

    @Override
    public String toString() {
        return name;
    }
}