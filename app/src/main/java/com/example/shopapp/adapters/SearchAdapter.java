package com.example.shopapp.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

import com.example.shopapp.R;
import com.example.shopapp.models.Product;

import java.util.List;

public class SearchAdapter extends ArrayAdapter<Product> {
    private final List<Product> objects;
    private LayoutInflater layoutInflater;
    private int resource;
    private Context context;
    private Activity activity;

    public SearchAdapter(@NonNull Context context, int resource, @NonNull List<Product> objects, Activity activity) {
        super(context, resource, objects);

        this.resource = resource;
        this.objects = objects;
        this.layoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Product product = this.objects.get(position);

        @SuppressLint("ViewHolder") View view = layoutInflater.inflate(this.resource, parent, false);
        Button button = view.findViewById(R.id.read_more_btn);
        button.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("key", product.getId());

            Navigation.findNavController(activity, R.id.nav_host_fragment).navigate(R.id.nav_product, bundle);
        });

        TextView textView = view.findViewById(R.id.title);
        textView.setText(product.getTitle());

        return view;
    }
}
