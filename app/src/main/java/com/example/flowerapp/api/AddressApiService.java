package com.example.flowerapp.api;

import com.example.flowerapp.Models.Address.District;
import com.example.flowerapp.Models.Address.Province;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AddressApiService {
    @GET("p/")
    Call<List<Province>> getProvinces();

    @GET("p/{province_code}")
    Call<Province> getDistricts(@Path("province_code") int provinceCode, @Query("depth") int depth);

    @GET("d/{district_code}")
    Call<District> getWards(@Path("district_code") int districtCode, @Query("depth") int depth);
}