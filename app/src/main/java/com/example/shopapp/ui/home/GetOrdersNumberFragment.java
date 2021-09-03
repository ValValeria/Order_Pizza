package com.example.shopapp.ui.home;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.shopapp.R;
import com.google.android.material.textfield.TextInputEditText;

import org.jetbrains.annotations.NotNull;

import io.reactivex.rxjava3.subjects.PublishSubject;

public class GetOrdersNumberFragment extends DialogFragment {
    public static PublishSubject<Boolean> publishSubject = PublishSubject.create();
    public static String intentAction = "com.example.shopapp.GET_NUMBER_OF_ORDER";
    public static String number = "0";
    private String TAG = GetOrdersNumberFragment.class.getName();

    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.number_of_dishes, null);
        TextInputEditText textInputEditText = view.findViewById(R.id.numbersField);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(requireActivity())
                .setTitle("The number of dishes")
                .setView(view)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       String string = textInputEditText.getText().toString();
                       number = string;

                       Intent intent = new Intent(intentAction);
                       intent.putExtra("number", string);

                       requireActivity().sendBroadcast(intent);

                       Log.i(TAG, "The number of order is " + number);
                    }
                })
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       publishSubject.onNext(false);
                    }
                })
                ;

        return alertDialog.create();
    }
}
