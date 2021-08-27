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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.shopapp.R;
import com.example.shopapp.models.Product;
import com.example.shopapp.services.MyService;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;
import java.util.concurrent.atomic.AtomicInteger;


public class HomeFragment extends Fragment{
  private ProgressBar progressBar;
  private LayoutInflater layoutInflater;
  private LinearLayout linearLayout;
  private FirebaseStorage firebaseStorage;
  private StorageReference storageReference;
  private final int PER_PAGE = 3;
  final long ONE_MEGABYTE = 1024 * 1024;
  private LinearLayout noResultLayout;
  private View noResultView;

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
  public void onStart() {
    super.onStart();

    progressBar = requireActivity().findViewById(R.id.progressBar);
    linearLayout = requireActivity().findViewById(R.id.container);

    this.loadData();
  }

  private void loadData(){
    linearLayout.removeAllViews();
    linearLayout.invalidate();

    AtomicInteger atomicInteger = new AtomicInteger(0);

    progressBar.setVisibility(ProgressBar.INVISIBLE);
    addNoResultView();

    MyService.newProductEvent.subscribe(v -> {
        if(atomicInteger.get() < PER_PAGE){
           addCardsToView(v);
           progressBar.setVisibility(ProgressBar.INVISIBLE);
           hideNoResultView();
        }

        atomicInteger.incrementAndGet();
    });
  }

  private void addCardsToView(Product product){
      View view = layoutInflater.inflate(R.layout.card, linearLayout, false);
      TextView title = view.findViewById(R.id.title);
      TextView short_text = view.findViewById(R.id.supporting_text);
      Button button = view.findViewById(R.id.btn);

      ImageView imageView = view.findViewById(R.id.imageView);
      StorageReference imageRef = storageReference.child(product.getImage());

      imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
          @Override
          public void onSuccess(byte[] bytes) {
              Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
              imageView.setImageBitmap(bitmap);
          }
      });

      title.setText(product.getTitle());
      short_text.setText(product.getDescription().substring(0, 20));
      button.setOnClickListener((view1) -> {
          Bundle bundle = new Bundle();
          bundle.putString("key", product.getId().toString());

          Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(R.id.nav_product, bundle);
      });

      linearLayout.addView(view);
  }

  private void addNoResultView(){
      noResultLayout = requireActivity().findViewById(R.id.home_content);
      noResultView = LayoutInflater.from(requireContext()).inflate(R.layout.empty_results, linearLayout, false);

      noResultLayout.addView(noResultView);
  }

  private void hideNoResultView(){
      noResultLayout.removeView(noResultView);
  }
}