package com.example.shopapp.ui.home;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.shopapp.R;
import com.example.shopapp.models.Product;
import com.example.shopapp.services.MyService;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.core.Observable;


public class AddProductFragment extends Fragment {
    private DatabaseReference db;
    private ChipGroup chipGroup;
    private TextInputEditText ingredients;
    private List<String> ingredientsList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseDatabase.getInstance().getReference(MyService.PRODUCT_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, 
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_product, container, false);

        Button button = view.findViewById(R.id.click);
        button.setOnClickListener(this::handleSubmit);

        ingredients = view.findViewById(R.id.ingredientsInput);
        ingredients.setOnKeyListener((v, keyCode, event) -> {
            switch (keyCode){
                case KeyEvent.KEYCODE_ENTER:
                    String ingredient = ingredients.getText().toString();
                    ingredientsList.add(ingredient);
                    break;
                default:
                    break;
            }

            return true;
        });

        chipGroup = view.findViewById(R.id.chipgroup);

        return view;
    }

    private void handleSubmit(View view){
        try{
            Product product = new Product();

            TextInputEditText title = getView().findViewById(R.id.titleInput);
            product.setTitle(title.getText().toString());

            TextInputEditText descr = getView().findViewById(R.id.descriptionInput);
            product.setTitle(descr.getText().toString());

            TextInputEditText price = getView().findViewById(R.id.priceInput);
            product.setPrice(Integer.parseInt(price.getText().toString()));

            TextInputEditText weight = getView().findViewById(R.id.weightInput);
            product.setWeight(Integer.parseInt(weight.getText().toString()));

            product.setId(db.push().getKey());

            JSONArray jsonArray = new JSONArray(ingredientsList);
            String ingredientsJson = jsonArray.toString();
            product.setIngredients(ingredientsJson);

            db.push().setValue(product);
        } catch (Throwable e){
            e.printStackTrace();
        }
    }
}