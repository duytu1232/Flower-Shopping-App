package com.example.flowerapp.User.Fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerapp.Adapters.FavoriteAdapter;
import com.example.flowerapp.Models.Product;
import com.example.flowerapp.R;

import java.util.ArrayList;
import java.util.List;

public class FragmentFavorite extends Fragment {
    private RecyclerView recyclerView;
    private FavoriteAdapter adapter;
    private List<Product> favoriteList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        recyclerView = view.findViewById(R.id.recycler_favorite_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        favoriteList = new ArrayList<>();
        // Thêm dữ liệu mẫu (hoặc lấy từ SQLite/Firestore/local storage)
        favoriteList.add(new Product("Rose", "$10.00", R.drawable.rose));
        favoriteList.add(new Product("Lily", "$15.00", R.drawable.lily));

        adapter = new FavoriteAdapter(favoriteList);
        recyclerView.setAdapter(adapter);

        return view;
    }
}