package com.example.shopapp.ui.home;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.shopapp.R;
import com.example.shopapp.config.OrderStatus;

import org.jetbrains.annotations.NotNull;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class ChangeStatusDialog extends DialogFragment {
    public static PublishSubject<OrderStatus> orderStatusPublishSubject = PublishSubject.create();

    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(requireActivity())
                .setTitle("The status of order")
                .setItems(R.array.admin_order_options, (dialog, which) -> {
                    if(which == 0){ // order is ready
                        orderStatusPublishSubject.onNext(OrderStatus.READY);
                    }else{ // order is unready
                        orderStatusPublishSubject.onNext(OrderStatus.UNREADY);
                    }
                });

        return alertDialog.create();
    }
}
