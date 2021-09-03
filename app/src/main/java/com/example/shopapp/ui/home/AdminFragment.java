package com.example.shopapp.ui.home;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.shopapp.MainActivity;
import com.example.shopapp.R;
import com.example.shopapp.adapters.ViewPagerAdapter;
import com.example.shopapp.classes.UserAuth;
import com.example.shopapp.config.OrderStatus;
import com.example.shopapp.models.Order;
import com.example.shopapp.models.Product;
import com.example.shopapp.services.MyService;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
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
    private LayoutInflater layoutInflater;
    private LinearLayout linearLayout;
    private UserAuth userAuth = new UserAuth();
    private TabLayout tabLayout;
    private String TAG = AdminFragment.class.getName();
    private ViewPager2 viewPager;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        layoutInflater = LayoutInflater.from(requireContext());

        MainActivity.userAuth.subscribe(userAuth -> {
            AdminFragment.this.userAuth = userAuth;

            if(!userAuth.isAdmin()){
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(R.id.nav_home);
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
        viewPager = view.findViewById(R.id.pager);
        viewPager.setAdapter(new ViewPagerAdapter(requireActivity()));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
          if(position == 0){
              tab.setText(getResources().getString(R.string.unready_orders));
          } else {
              tab.setText(getResources().getString(R.string.ready_orders));
          }

          viewPager.setCurrentItem(position, true);
        }).attach();
    }
}