package com.example.shopapp.ui.home;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.shopapp.R;
import com.example.shopapp.models.Product;
import com.example.shopapp.services.MyService;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class HomeFragment extends Fragment{
    private ProgressBar progressBar;
    private LayoutInflater layoutInflater;
    private LinearLayout linearLayout;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private final int PER_PAGE = 50;
    final long ONE_MEGABYTE = 1024 * 1024;
    private LinearLayout homeContent;
    private View noResultView;
    LinkedList<String> linkedList = new LinkedList();
    private AtomicBoolean isPaused = new AtomicBoolean(false);

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layoutInflater = LayoutInflater.from(getContext());

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        return layoutInflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progressBar);
        linearLayout = view.findViewById(R.id.container);
        homeContent = view.findViewById(R.id.home_content);
    }

    @Override
    public void onStart() {
        super.onStart();

        this.loadData();
    }

    @Override
    public void onResume() {
        super.onResume();

        if(isPaused.get()){
            loadData();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        isPaused.set(true);
    }

    private void loadData(){
        linearLayout.removeAllViews();
        linearLayout.invalidate();
        linkedList.clear();

        AtomicInteger atomicInteger = new AtomicInteger(0);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference(MyService.PRODUCT_KEY);

        databaseReference.get().addOnSuccessListener(dataSnapshot -> {
            noResultView = LayoutInflater.from(requireContext()).inflate(R.layout.empty_results, linearLayout, false);

            if(!dataSnapshot.hasChildren()){
                addNoResultView();
            } else{
                homeContent.removeView(noResultView);
            }

            homeContent.removeView(progressBar);

            for ( DataSnapshot datasnapshot: dataSnapshot.getChildren()) {
                Product product = datasnapshot.getValue(Product.class);

                Log.e(HomeFragment.class.getName(), "The id of product fragment " + product.getId());

                if(atomicInteger.get() < PER_PAGE && !linkedList.contains(product.getId())){
                    addCardsToView(product);
                    linkedList.add(product.getId());
                }
            }

            homeContent.invalidate();
        });
    }

    private void addCardsToView(Product product){
        View view = layoutInflater.inflate(R.layout.card, linearLayout, false);
        TextView title = view.findViewById(R.id.title);
        TextView short_text = view.findViewById(R.id.supporting_text);
        Button button = view.findViewById(R.id.btn);

        String[] paths = product.getImage().split("/");

        ImageView imageView = view.findViewById(R.id.imageView);
        StorageReference imageRef = storageReference.child(paths[paths.length - 1]);

        imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            imageView.setImageBitmap(bitmap);
        });

        title.setText(product.getTitle());

        String txt;

        if(product.getDescription().length() > 20){
            txt = product.getDescription().substring(0, 20);
        } else {
            txt = product.getDescription();
        }

        short_text.setText(txt);

        button.setOnClickListener((view1) -> {
            Bundle bundle = new Bundle();
            bundle.putString("key", product.getId().toString());

            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(R.id.nav_product, bundle);
        });

        linearLayout.addView(view);
    }

    private void addNoResultView(){
        homeContent.addView(this.noResultView);
    }
}