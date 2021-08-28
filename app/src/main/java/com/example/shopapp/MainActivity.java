package com.example.shopapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Menu;
import android.widget.SearchView;

import com.example.shopapp.classes.UserAuth;
import com.example.shopapp.services.MyService;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import io.reactivex.rxjava3.subjects.PublishSubject;


public class MainActivity extends AppCompatActivity {
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    public static PublishSubject<UserAuth> userAuth = PublishSubject.create();
    static final int REQUEST_IMAGE_GET = 1;
    private UserAuth userAuthObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));
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
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_hint));
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