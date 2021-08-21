package com.example.shopapp.ui.home;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.shopapp.R;
import com.example.shopapp.models.Product;
import java.util.ArrayList;
import java.util.List;

public class MenuFragment extends Fragment {
    private final List<Product> productList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void loadProducts(){
        
    }
}