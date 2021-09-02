package com.example.shopapp.ui.home;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.shopapp.MainActivity;
import com.example.shopapp.R;
import com.example.shopapp.models.Product;
import com.example.shopapp.services.MyService;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;


public class SearchFragment extends Fragment {
    private LinearLayout linearLayout;
    private ArrayList<Product> products = new ArrayList<>();
    private DatabaseReference databaseReference;

    public SearchFragment(){
        super(R.layout.fragment_search);
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        databaseReference = FirebaseDatabase.getInstance().getReference(MyService.PRODUCT_KEY);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        linearLayout = view.findViewById(R.id.results_container_search);

        MainActivity.search.subscribe(v -> {
            Log.i(SearchFragment.class.getName(), "The search result is " + v.size());

            SearchFragment.this.requireView().post(() -> {
                SearchFragment.this.products.addAll(v);
                SearchFragment.this.addViews();
            });
        });
    }

    private void addViews(){
        linearLayout.removeAllViews();

        for(Product product: products){
            View view = requireActivity().getLayoutInflater().inflate(R.layout.search_result,
                    linearLayout, false);

            Button button = view.findViewById(R.id.read_more_btn);
            button.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("key", product.getId());

                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(R.id.nav_product, bundle);
            });

            TextView textView = view.findViewById(R.id.title);
            textView.setText(product.getTitle());

            Log.i(SearchFragment.class.getName(), "The search result: " + product.getTitle());

            linearLayout.addView(view);
        }

        linearLayout.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();

        products.clear();
    }
}