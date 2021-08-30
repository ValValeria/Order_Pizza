package com.example.shopapp.ui.home;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.shopapp.MainActivity;
import com.example.shopapp.R;
import com.example.shopapp.classes.UserAuth;
import com.example.shopapp.models.Order;
import com.example.shopapp.services.MyService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;


public class OrderList extends Fragment {
    private ListView listView;
    private DatabaseReference databaseReference;
    private UserAuth userAuth;
    private NavController navController;
    private List<Order> orderList = new ArrayList<>();

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

        databaseReference.get().addOnSuccessListener(v -> {
            for (DataSnapshot dataSnapshot: v.getChildren()){
                Order order = dataSnapshot.getValue(Order.class);

                if(userAuth!= null && order.getEmail().equals(userAuth.getUser().getEmail())){
                    orderList.add(order);
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_list, container, false);
    }
}