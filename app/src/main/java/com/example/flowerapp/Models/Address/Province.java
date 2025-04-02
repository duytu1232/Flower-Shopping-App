package com.example.flowerapp.Models.Address;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Province {
    @SerializedName("name")
    private String name;

    @SerializedName("code")
    private int code;

    @SerializedName("districts")
    private List<com.example.flowerapp.Models.Address.District> districts;

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }

    public List<com.example.flowerapp.Models.Address.District> getDistricts() {
        return districts;
    }

    @Override
    public String toString() {
        return name;
    }
}
