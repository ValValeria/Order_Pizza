package com.example.shopapp.ui.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.shopapp.MainActivity;
import com.example.shopapp.R;
import com.example.shopapp.classes.UserAuth;
import com.example.shopapp.config.OrderStatus;
import com.example.shopapp.models.Order;
import com.example.shopapp.models.Product;
import com.example.shopapp.services.MyService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.atomic.AtomicBoolean;


public class ProductFragment extends Fragment {
    private Product product = new Product();
    private NavController navController;
    private LayoutInflater layoutInflater;
    private DatabaseReference dbReference;
    private String key;
    final long ONE_MEGABYTE = 1024 * 1024;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private AtomicBoolean atomicBoolean = new AtomicBoolean(false);
    private UserAuth userAuth;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private LinearLayout btnActionLayout;
    private Order order = new Order();
    private Button button;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        key = requireArguments().getString("key");

        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);

        layoutInflater = LayoutInflater.from(getContext());
        dbReference = firebaseDatabase.getReference(MyService.PRODUCT_KEY);

        dbReference.child(key).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    DataSnapshot snapshot = task.getResult();

                    if(!snapshot.exists()){
                        navController.navigate(R.id.nav_home);
                    } else {
                        product = snapshot.getValue(Product.class);

                        TextView title = getActivity().findViewById(R.id.title_product);
                        title.setText(product.getTitle());

                        TextView textView1 = getActivity().findViewById(R.id.descr);
                        textView1.setText(product.getDescription());

                        String[] paths = product.getImage().split("/");

                        ImageView imageView = getActivity().findViewById(R.id.imageView);
                        StorageReference imageRef = storageReference.child(paths[paths.length - 1]);

                        imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            imageView.setImageBitmap(bitmap);
                        });

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
            }
        });

        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                     String strNum = intent.getExtras().getString("number", GetOrdersNumberFragment.number);
                     int number = Integer.parseInt(strNum);
                     order.setCount(number);

                     Log.i(ProductFragment.class.getName(), "IntentFilter: " + GetOrdersNumberFragment.intentAction +
                            ". Number: " + number);
                } catch(NumberFormatException e){
                     e.printStackTrace();
                }

                buyProduct(order);
            }
        };

        requireActivity().registerReceiver(broadcastReceiver, new IntentFilter(GetOrdersNumberFragment.intentAction));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product, container, false);
        button = view.findViewById(R.id.order_a_product);
        button.setOnClickListener(ProductFragment.this::handleOrderClick);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity.userAuth.subscribe(v -> {
            atomicBoolean.set(v.isAuth());
            userAuth = v;

            if(userAuth.isAdmin()){
                button.setEnabled(false);

                btnActionLayout = ProductFragment.this.requireActivity().findViewById(R.id.btnActionLayout);

                View view1 = LayoutInflater.from(requireContext()).inflate(R.layout.delete_btn, btnActionLayout, false);

                if(btnActionLayout.getChildCount() == 1){
                    btnActionLayout.addView(view1);

                    view1.setOnClickListener(v1 -> {
                        dbReference.child(product.getId()).removeValue();
                        Toast.makeText(requireContext(), "The product is deleted", Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    public void handleOrderClick(View view){
        if(userAuth != null && userAuth.isAuth()){
            GetOrdersNumberFragment getOrdersNumberFragment = new GetOrdersNumberFragment();
            getOrdersNumberFragment.show(getChildFragmentManager(), "");
        } else{
            navController.navigate(R.id.nav_login);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void buyProduct(Order order){
        order.setStatus(OrderStatus.UNVERIFIED.toString());
        order.setEmail(userAuth.getUser().getEmail());

        Calendar calendar = new GregorianCalendar();
        Date date = calendar.getTime();
        order.setTime(date.toString());

        DatabaseReference orderReference = firebaseDatabase.getReference(MyService.ORDER_KEY);

        DatabaseReference orderRef = orderReference.push();
        order.setKey(orderRef.getKey());
        order.setDishKey(product.getId());
        orderRef.setValue(order);

        orderReference.child(order.getKey()).setValue(order);

        Log.i(ProductFragment.class.getName(),"The product key is " + product.getId());

        Toast.makeText(getContext(), "The product is added to the cart", Toast.LENGTH_LONG).show();
    }
}