package com.example.shopapp.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.NonNull;

import com.example.shopapp.models.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import io.reactivex.rxjava3.subjects.PublishSubject;

public class MyService extends Service {
    private DatabaseReference dbProductRef;
    public final static String PRODUCT_KEY = "product";
    public static PublishSubject<Product> newProductEvent = PublishSubject.create();

    @Override
    public void onCreate() {
        super.onCreate();

        dbProductRef = FirebaseDatabase.getInstance().getReference(PRODUCT_KEY);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for ( DataSnapshot datasnapshot: snapshot.getChildren()) {
                    Product product = datasnapshot.getValue(Product.class);
                    product.setId(datasnapshot.getKey());

                    newProductEvent.onNext(product);

                    System.out.println("The product is loaded");
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        };

        dbProductRef.addValueEventListener(eventListener);

        return super.onStartCommand(intent, flags, startId);
    }
}