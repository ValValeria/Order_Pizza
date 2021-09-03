package com.example.shopapp.ui.home;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shopapp.MainActivity;
import com.example.shopapp.R;
import com.example.shopapp.classes.UserAuth;
import com.example.shopapp.config.OrderStatus;
import com.example.shopapp.models.Order;
import com.example.shopapp.models.Product;
import com.example.shopapp.services.MyService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class OrderList extends Fragment {
    private ListView listView;
    private DatabaseReference databaseReference;
    private UserAuth userAuth;
    private NavController navController;
    private List<Order> orderList = new ArrayList<>();
    private List<String> orderListId = new ArrayList<>();
    private MaterialCardView noResults;
    private LinearLayout results;
    private Button submitButton;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        databaseReference = FirebaseDatabase.getInstance().getReference(MyService.ORDER_KEY);

        MainActivity.userAuth.subscribe(v -> {
            userAuth = v;

            if(!userAuth.isAuth()){
                navController.navigate(R.id.nav_login);
            }

            Log.i(OrderList.class.getName(), userAuth.isAuth() ? "Current user is auth" : "Current user is not auth");
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_list, container, false);
        noResults = view.findViewById(R.id.no_orders);
        results = view.findViewById(R.id.space_for_orders);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadData();
    }

    @Override
    public void onStart() {
        super.onStart();

        Button visitOrderPage = requireActivity().findViewById(R.id.visitOrderPage);
        visitOrderPage.setOnClickListener(v -> {
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(R.id.nav_home);
        });

        submitButton = requireActivity().findViewById(R.id.submit_orders);
        submitButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Submitting", Toast.LENGTH_SHORT).show();

            databaseReference.get()
                    .addOnSuccessListener(dataSnapshot1 -> {
                        for (DataSnapshot dataSnapshot: dataSnapshot1.getChildren()){
                            Order order = dataSnapshot.getValue(Order.class);
                            order.setKey(dataSnapshot.getKey());

                            if(order.getStatus().equals(OrderStatus.UNVERIFIED.toString())){
                                order.setStatus(OrderStatus.UNREADY.toString());
                            }

                            databaseReference.child(order.getKey()).removeValue()
                                    .addOnSuccessListener(v2 -> {
                                        databaseReference.child(order.getKey()).setValue(order);
                                    });
                        }

                        Toast.makeText(getContext(), "The order is submitted", Toast.LENGTH_LONG).show();
                        navController.navigate(R.id.nav_home);
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull @NotNull Exception e) {
                            e.printStackTrace();
                        }
                    })
            ;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void addOrder(Order order){
       DatabaseReference databaseProductReference = FirebaseDatabase.getInstance().getReference(MyService.PRODUCT_KEY);

       Log.i(OrderList.class.getName(), "The id of product is " + order.getDishKey());

       submitButton.setVisibility(View.VISIBLE);

       databaseProductReference.child(order.getDishKey()).get()
               .addOnSuccessListener(dataSnapshot -> {
                   Product product = dataSnapshot.getValue(Product.class);

                   LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                   View view = layoutInflater.inflate(R.layout.order_item, results, false);

                   TextView textView = view.findViewById(R.id.order_title);
                   textView.setText(product.getTitle());

                   TextView textView1 = view.findViewById(R.id.info);
                   textView1.setText(String.format("The total count of order is %d. Status: %s. Time " + order.getTime(),
                           order.getCount(), order.getStatus().toLowerCase()));

                   Button button = view.findViewById(R.id.delete_order);

                   if(order.getStatus().equals(OrderStatus.UNVERIFIED.toString())){
                       button.setClickable(true);
                       button.setOnClickListener(v1 -> {
                           databaseProductReference.child(dataSnapshot.getKey()).removeValue();

                           results.removeView(view);
                           results.invalidate();
                       });
                   } else {
                       button.setClickable(false);
                       button.setVisibility(View.INVISIBLE);
                   }

                   results.addView(view);
                   results.invalidate();

                   Log.i(OrderList.class.getName(), "Order is added to the view");
               })
               .addOnFailureListener(Throwable::printStackTrace)
       ;
    }

    private void loadData(){
        databaseReference.get()
                .addOnSuccessListener(v -> {
                    for (DataSnapshot dataSnapshot: v.getChildren()){
                        Order order = dataSnapshot.getValue(Order.class);
                        order.setKey(dataSnapshot.getKey());

                        if(!orderListId.contains(order.getKey())){
                            if(userAuth!= null && order.getEmail().equalsIgnoreCase(userAuth.getUser().getEmail())){
                                orderList.add(order);
                                orderListId.add(order.getKey());
                            }
                        }
                    }

                    if(orderList.size() > 0){
                        LinearLayout linearLayout = requireActivity().findViewById(R.id.main_container);
                        linearLayout.removeView(noResults);
                        linearLayout.invalidate();
                    } else {
                        Toast.makeText(getContext(), "No orders", Toast.LENGTH_LONG).show();
                    }

                    Collections.sort(orderList, (o1, o2) -> {
                        String unverified = OrderStatus.UNVERIFIED.toString();

                        if(o1.getStatus().equals(unverified) && !o2.getStatus().equals(unverified)){
                            return 1;
                        } else if (!o1.getStatus().equals(unverified) && o2.getStatus().equals(unverified)){
                            return -1;
                        }

                        return 0;
                    });

                    for (Order order: orderList){
                        addOrder(order);
                    }
                }).addOnFailureListener(Throwable::printStackTrace) ;
    }
}