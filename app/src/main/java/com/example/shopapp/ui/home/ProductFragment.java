package com.example.shopapp.ui.home;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.shopapp.R;
import com.example.shopapp.models.Product;
import org.json.JSONArray;
import org.json.JSONException;


public class ProductFragment extends Fragment {
    private Product product = new Product();
    private NavController navController;
    private LayoutInflater layoutInflater;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String key = requireArguments().getString("key");
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        layoutInflater = LayoutInflater.from(getContext());

        /*getView().post(() -> {
            int number = Integer.parseInt(key);
            product = productDao.getById(number);

            if(product == null){
               navController.navigate(R.id.nav_home);
            } else {
                TextView textView1 = getActivity().findViewById(R.id.descr);
                textView1.setText(product.getDescription());

                Button button = getActivity().findViewById(R.id.textButton);
                button.setOnClickListener(this::buyProduct);

                try {
                    JSONArray ingredients = new JSONArray(product.getIngredients());
                    LinearLayout linearLayout = getActivity().findViewById(R.id.container);

                    for (int i = 0; i < ingredients.length(); i++) {
                        View view = layoutInflater.inflate(R.layout.ingredient, linearLayout, false);
                        TextView textView = view.findViewById(R.id.textIngredient);
                        textView.setText(ingredients.getString(i));

                        linearLayout.addView(textView);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product, container, false);
    }

    public void buyProduct(View view){
        Toast.makeText(getContext(), "The product is added to the cart", Toast.LENGTH_LONG).show();
    }
}