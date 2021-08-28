package com.example.shopapp.ui.home;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.shopapp.R;
import com.example.shopapp.models.Product;
import com.example.shopapp.services.MyService;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;



public class ProductFragment extends Fragment {
    private Product product = new Product();
    private NavController navController;
    private LayoutInflater layoutInflater;
    private DatabaseReference dbReference;
    private String key;
    final long ONE_MEGABYTE = 1024 * 1024;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        key = requireArguments().getString("key");
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        layoutInflater = LayoutInflater.from(getContext());
        dbReference = FirebaseDatabase.getInstance().getReference(MyService.PRODUCT_KEY);

        dbReference.child(key).addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NotNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    navController.navigate(R.id.nav_home);
                } else {
                    Product product = snapshot.getValue(Product.class);

                    TextView textView1 = getActivity().findViewById(R.id.descr);
                    textView1.setText(product.getDescription());

                    String[] paths = product.getImage().split("/");

                    ImageView imageView = getActivity().findViewById(R.id.imageView);
                    StorageReference imageRef = storageReference.child(paths[paths.length - 1]);

                    imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        imageView.setImageBitmap(bitmap);
                    });

                    Button button = getActivity().findViewById(R.id.textButton);
                    button.setOnClickListener(ProductFragment.this::buyProduct);

                    try {
                        JSONArray ingredients = new JSONArray(product.getIngredients());
                        LinearLayout linearLayout = getActivity().findViewById(R.id.container);

                        for (int i = 0; i < ingredients.length(); i++) {
                            View view = layoutInflater.inflate(R.layout.ingredient, linearLayout, false);
                            TextView textView = view.findViewById(R.id.textIngredient);
                            textView.setText(ingredients.getString(i));

                            linearLayout.addView(view);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
               navController.navigate(R.id.nav_home);
            }
        });
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