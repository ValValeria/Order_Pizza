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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AdminContentFragment extends Fragment {
    private boolean isUnreadyOrders = true;
    private Map<Order, Product> map = new LinkedHashMap<>();
    private DatabaseReference dbRef;
    private DatabaseReference dbProductRef;
    private LinearLayout linearLayout;
    private LinearLayout adminOrdersSpace;
    private MaterialCardView materialCardView;
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
        materialCardView = view.findViewById(R.id.no_admin_orders);

        Bundle args = getArguments();

        if(args != null){
            isUnreadyOrders = args.getBoolean(OrderStatus.UNREADY.toString());
            String txt;

            if(isUnreadyOrders){
               txt = "The list of unready orders";
            } else {
               txt = "The list of ready orders";
            }

            title.setText(txt);

            view.post(this::loadOrders);
            Log.i(AdminContentFragment.class.getName(), txt);
        }
    }

    private void loadOrders(){
        dbRef.get().addOnSuccessListener(dataSnapshot -> {
            for (DataSnapshot orderSnapshot: dataSnapshot.getChildren()){
                Order order = orderSnapshot.getValue(Order.class);

                dbProductRef
                        .child(order.getDishKey())
                        .get()
                        .addOnSuccessListener(v -> {
                            if(!v.exists()){
                                v.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.i(TAG, "The invalid order was deleted");
                                    }
                                });

                                Toast.makeText(requireContext(), "The product has been deleted", Toast.LENGTH_LONG).show();
                            } else {
                                Product product = v.getValue(Product.class);

                                Log.i(TAG, "Order status is: " + order.getStatus());
                                Log.i(TAG, "Do we need unready orders: " + isUnreadyOrders);

                                if(order.getStatus().equals(OrderStatus.UNREADY.toString()) && isUnreadyOrders){
                                    map.put(order, product);
                                } else if(order.getStatus().equals(OrderStatus.READY.toString()) && !isUnreadyOrders){
                                    map.put(order, product);
                                }

                                addOrdersToLayout();
                                map.clear();
                            }
                        });
            }
        });
    }

    private void addOrdersToLayout(){
        linearLayout.removeView(materialCardView);

        for (Map.Entry<Order, Product> mapEntry: map.entrySet()){
            Order order = mapEntry.getKey();
            Product product = mapEntry.getValue();

            View view = LayoutInflater.from(requireContext())
                    .inflate(R.layout.admin_order_item, linearLayout, false);

            TextView orderTitle = view.findViewById(R.id.admin_order_title);
            orderTitle.setText(product.getTitle());

            TextView orderInfo = view.findViewById(R.id.admin_order_info);
            orderInfo.setText(String.format("The total count of order is %d. The email is %s. Status: %s. Time " + order.getTime(),
                    order.getCount(), order.getEmail(), order.getStatus().toLowerCase()));

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

                    getView().post(() -> {
                        map.clear();
                        loadOrders();
                    });
                });
            });

            adminOrdersSpace.addView(view);
            adminOrdersSpace.invalidate();

            Log.i(TAG, "Order is loading. Number: " + order.getCount());
            Log.i(TAG, "The total count of orders in view is" + adminOrdersSpace.getChildCount());
        }
    }
}
