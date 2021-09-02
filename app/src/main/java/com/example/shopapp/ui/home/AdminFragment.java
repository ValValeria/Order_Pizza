package com.example.shopapp.ui.home;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;


public class AdminFragment extends Fragment {
    private DatabaseReference dbRef;
    private DatabaseReference dbProductRef;
    private LayoutInflater layoutInflater;
    private LinearLayout linearLayout;
    private UserAuth userAuth = new UserAuth();
    private TabLayout tabLayout;
    private List<Order> readyOrders = new ArrayList<>();
    private List<Order> unreadyOrders = new ArrayList<>();
    private AtomicBoolean showUnreadyOrders = new AtomicBoolean(true);
    private String TAG = AdminFragment.class.getName();

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbRef = FirebaseDatabase.getInstance().getReference(MyService.ORDER_KEY);
        dbProductRef = FirebaseDatabase.getInstance().getReference(MyService.PRODUCT_KEY);
        layoutInflater = LayoutInflater.from(requireContext());

        MainActivity.userAuth.subscribe(new Consumer<UserAuth>() {
            @Override
            public void accept(UserAuth userAuth) throws Throwable {
                AdminFragment.this.userAuth = userAuth;

                if(!userAuth.isAdmin()){
                    Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(R.id.nav_home);
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin2, container, false);
    }


    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        linearLayout = view.findViewById(R.id.admin_container);
        tabLayout = view.findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getId() == R.id.showUnready){
                    showUnreadyOrders.set(true);
                } else {
                    showUnreadyOrders.set(false);
                }

                Log.i(TAG, "Tab was selected");
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Log.i(TAG, "Tab was unselected");
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Log.i(TAG, "Tab was reselected");
            }
        });

        view.post(this::loadData);
    }

    private void loadData(){
        dbRef.get().addOnSuccessListener(dataSnapshot -> {
            for (DataSnapshot orderSnapshot: dataSnapshot.getChildren()){
                Order order = orderSnapshot.getValue(Order.class);
                DataSnapshot dataSnapshot1 =  dbProductRef.child(order.getDishKey())
                        .get().getResult();

                if(!dataSnapshot1.exists()){
                    Toast.makeText(requireContext(), "The product has been deleted", Toast.LENGTH_LONG).show();
                } else {
                    if(order.getStatus().equalsIgnoreCase(OrderStatus.UNREADY.toString())){
                        unreadyOrders.add(order);
                    } else if(order.getStatus().equalsIgnoreCase(OrderStatus.READY.toString())){
                        readyOrders.add(order);
                    }
                }
            }

            addOrdersToLayout();
        });

        dbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                addOrdersToLayout();

                Log.i(TAG, "Order has been added");
            }

            @Override
            public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                addOrdersToLayout();

                Log.i(TAG, "Order has been updated");
            }

            @Override
            public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {
                Log.i(TAG, "Order has been deleted");
            }

            @Override
            public void onChildMoved(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void addOrdersToLayout(){
        List<Order> orderList = new ArrayList<>();

        if(showUnreadyOrders.get()){
            orderList.addAll(unreadyOrders);
        } else {
            orderList.addAll(readyOrders);
        }

        linearLayout.removeAllViews();
        linearLayout.invalidate();

        for (Order order: orderList){
            DataSnapshot dataSnapshot1 =  dbProductRef.child(order.getDishKey())
                    .get().getResult();

            Product product = dataSnapshot1.getValue(Product.class);
            View view = layoutInflater.inflate(R.layout.admin_order_item, linearLayout, false);

            TextView orderTitle = view.findViewById(R.id.admin_order_title);
            orderTitle.setText(product.getTitle());

            TextView orderInfo = view.findViewById(R.id.admin_order_info);
            orderInfo.setText(String.format("The total count of order is %d. Status: %s. Time " + order.getTime(),
                    order.getCount(), order.getStatus().toLowerCase()));

            Button button = view.findViewById(R.id.admin_change_order_status);
            button.setOnClickListener(v -> {
                ChangeStatusDialog dialog = new ChangeStatusDialog();
                dialog.show(getChildFragmentManager(), "");

                ChangeStatusDialog.orderStatusPublishSubject.subscribe(status -> {
                    if(status == OrderStatus.READY){
                        order.setStatus(OrderStatus.READY.toString());
                    }else{
                        order.setStatus(OrderStatus.UNREADY.toString());
                    }

                    dbRef.child(order.getKey()).setValue(order);

                    Toast.makeText(requireContext(), "The order is changed", Toast.LENGTH_LONG).show();
                });
            });

            linearLayout.addView(view);
            linearLayout.invalidate();
        }
    }
}