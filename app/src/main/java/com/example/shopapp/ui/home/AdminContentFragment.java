package com.example.shopapp.ui.home;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.shopapp.R;
import com.example.shopapp.config.OrderStatus;
import com.example.shopapp.models.Order;
import com.example.shopapp.models.Product;
import com.example.shopapp.services.MyService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AdminContentFragment extends Fragment {
    private boolean isUnreadyOrders = true;
    private List<Order> orders = new ArrayList<>();
    private DatabaseReference dbRef;
    private DatabaseReference dbProductRef;
    private LinearLayout linearLayout;
    private LinearLayout adminOrdersSpace;
    private final String TAG = AdminContentFragment.class.getName();

    public AdminContentFragment() {
        super(R.layout.admin_content);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView title = view.findViewById(R.id.admin_title);
        linearLayout = view.findViewById(R.id.admin_container);
        dbRef = FirebaseDatabase.getInstance().getReference(MyService.ORDER_KEY);
        dbProductRef = FirebaseDatabase.getInstance().getReference(MyService.PRODUCT_KEY);
        adminOrdersSpace = view.findViewById(R.id.space_for_available_orders);

        Bundle args = getArguments();

        if(args != null){
            isUnreadyOrders = args.getBoolean(OrderStatus.UNREADY.toString());

            if(isUnreadyOrders){
                title.setText("The list of unready orders");
            } else {
                title.setText("The list of ready orders");
            }
        }

        view.post(this::loadOrders);
    }

    private void loadOrders(){
        dbRef.get().addOnSuccessListener(dataSnapshot -> {
            for (DataSnapshot orderSnapshot: dataSnapshot.getChildren()){
                Order order = orderSnapshot.getValue(Order.class);
                DataSnapshot dataSnapshot1 =  dbProductRef.child(order.getDishKey())
                        .get().getResult();

                if(!dataSnapshot1.exists()){
                    Toast.makeText(requireContext(), "The product has been deleted", Toast.LENGTH_LONG).show();
                } else {
                    if(order.getStatus().equalsIgnoreCase(OrderStatus.UNREADY.toString())){
                        if(isUnreadyOrders) orders.add(order);
                    } else if(order.getStatus().equalsIgnoreCase(OrderStatus.READY.toString())){
                        if(!isUnreadyOrders) orders.add(order);
                    }
                }
            }

            addOrdersToLayout();
        });
    }

    private void addOrdersToLayout(){
        Log.i(TAG, "Orders is loading");

        for (Order order: orders){
            DataSnapshot dataSnapshot1 =  dbProductRef.child(order.getDishKey())
                    .get().getResult();

            Product product = dataSnapshot1.getValue(Product.class);
            View view = LayoutInflater.from(requireContext())
                    .inflate(R.layout.admin_order_item, linearLayout, false);

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

            adminOrdersSpace.addView(view);
            adminOrdersSpace.invalidate();
        }
    }
}
