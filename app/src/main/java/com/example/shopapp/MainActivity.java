package com.example.shopapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Menu;

import com.example.shopapp.services.MyService;
import com.google.android.material.navigation.NavigationView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import io.reactivex.rxjava3.subjects.PublishSubject;


public class MainActivity extends AppCompatActivity {
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    public static PublishSubject<Void> chooseFile = PublishSubject.create();
    static final int REQUEST_IMAGE_GET = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));
        startService(new Intent(this, MyService.class));
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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