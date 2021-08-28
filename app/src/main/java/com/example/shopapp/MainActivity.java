package com.example.shopapp;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;

import com.example.shopapp.classes.UserAuth;
import com.example.shopapp.models.Product;
import com.example.shopapp.services.MyService;
import com.example.shopapp.ui.home.SearchFragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.fragment.app.FragmentContainerView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.subjects.PublishSubject;


public class MainActivity extends AppCompatActivity {
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    public static PublishSubject<UserAuth> userAuth = PublishSubject.create();
    public static PublishSubject<Product> search = PublishSubject.create();
    private UserAuth userAuthObj;
    private FragmentContainerView fragmentContainerView;
    private LinearLayout linearLayout;
    private ListView searchResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));

        fragmentContainerView = findViewById(R.id.nav_host_fragment);

        startService(new Intent(this, MyService.class));

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if(firebaseUser != null){
            userAuthObj = new UserAuth(firebaseUser.getEmail(), "");
        } else {
            userAuthObj = new UserAuth();
        }

        userAuth.onNext(userAuthObj);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        setupSearchView(searchMenuItem);
        return true;
    }

    private void setupSearchView(MenuItem searchMenuItem) {
        NavController navController = Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment);

        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_hint));

        searchView.setOnCloseListener(() -> {
            navController.navigate(R.id.nav_home);

            return false;
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(MyService.PRODUCT_KEY);

                databaseReference.get().addOnSuccessListener(dataSnapshot -> {
                    navController.navigate(R.id.nav_search);

                    for ( DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                        Product product = dataSnapshot1.getValue(Product.class);
                        product.setId(dataSnapshot1.getKey());

                        if(product.getTitle().contains(query) || product.getDescription().contains(query)){
                            search.onNext(product);
                        }
                    }
                });


                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        NavController navController = Navigation.findNavController(findViewById(R.id.nav_host_fragment));
        navigationView = findViewById(R.id.nav_view);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView.setNavigationItemSelectedListener(v -> {
            switch(v.getItemId()){
                case R.id.nav_home:
                    navController.navigate(R.id.nav_home);
                    break;
                case R.id.nav_login:
                    navController.navigate(R.id.nav_login);
                    break;
                case R.id.nav_signup:
                    navController.navigate(R.id.nav_signup);
                    break;
                case R.id.nav_about_us:
                    navController.navigate(R.id.nav_about);
                case R.id.nav_add_product:
                    navController.navigate(R.id.nav_add_product_page);
            }

            drawerLayout.close();

            return true;
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_menu:
                drawerLayout.open();
                break;
            case R.id.action_search:
                return false;
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}