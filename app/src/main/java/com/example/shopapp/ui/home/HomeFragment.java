package com.example.shopapp.ui.home;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.shopapp.R;
import com.example.shopapp.models.Product;
import com.example.shopapp.services.MyService;
import org.jetbrains.annotations.NotNull;
import java.util.concurrent.atomic.AtomicInteger;


public class HomeFragment extends Fragment{
  private ProgressBar progressBar;
  private LayoutInflater layoutInflater;
  private LinearLayout linearLayout;
  private final int PER_PAGE = 3;

  @Override
  public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    layoutInflater = LayoutInflater.from(getContext());

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
    
    MyService.newProductEvent.subscribe(v -> {
        if(atomicInteger.get() < PER_PAGE){
           addCardsToView(v);
           progressBar.setVisibility(ProgressBar.INVISIBLE);
        }

        atomicInteger.incrementAndGet();
    });
  }

  private void addCardsToView(Product product){
      View view = layoutInflater.inflate(R.layout.card, linearLayout, false);
      TextView title = view.findViewById(R.id.title);
      TextView short_text = view.findViewById(R.id.supporting_text);
      Button button = view.findViewById(R.id.btn);

      title.setText(product.getTitle());
      short_text.setText(product.getDescription().substring(0, 20));
      button.setOnClickListener((view1) -> {
          Bundle bundle = new Bundle();
          bundle.putString("key", product.getId().toString());

          Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(R.id.nav_product, bundle);
      });

      linearLayout.addView(view);
  }
}