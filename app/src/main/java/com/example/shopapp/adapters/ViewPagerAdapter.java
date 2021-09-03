package com.example.shopapp.adapters;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.shopapp.config.OrderStatus;
import com.example.shopapp.ui.home.AdminContentFragment;

import org.jetbrains.annotations.NotNull;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull @NotNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @NotNull
    @Override
    public Fragment createFragment(int position) {
        Log.i(ViewPagerAdapter.class.getName(), "Creating a fragment");

        Bundle bundle = new Bundle();
        AdminContentFragment adminContentFragment = new AdminContentFragment();
        adminContentFragment.setArguments(bundle);

        if(position == 0){
           bundle.putBoolean(OrderStatus.UNREADY.toString(), true);
        } else {
           bundle.putBoolean(OrderStatus.UNREADY.toString(), false);
        }

        return adminContentFragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
