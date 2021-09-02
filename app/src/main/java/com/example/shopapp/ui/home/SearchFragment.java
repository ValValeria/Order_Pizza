package com.example.shopapp.ui.home;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.shopapp.MainActivity;
import com.example.shopapp.R;
import com.example.shopapp.adapters.SearchAdapter;
import com.example.shopapp.models.Product;
import com.example.shopapp.services.MyService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class SearchFragment extends Fragment {
    private ListView listView;
    private ArrayAdapter<Product> arrayAdapter;
    private ArrayList<Product> products = new ArrayList<>();
    private DatabaseReference databaseReference;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        databaseReference = FirebaseDatabase.getInstance().getReference(MyService.PRODUCT_KEY);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listView = view.findViewById(R.id.results_search);

        arrayAdapter = new SearchAdapter(requireContext(), R.layout.search_result, products, requireActivity());
        listView.setAdapter(arrayAdapter);
        listView.invalidate();

        MainActivity.search.subscribe(v -> {
            Log.i(SearchFragment.class.getName(), "The search result is " + v.getTitle());

            SearchFragment.this.products.add(v);
            arrayAdapter.add(v);
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        products.clear();
        arrayAdapter.clear();
    }
}