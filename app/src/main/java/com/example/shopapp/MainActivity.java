package com.example.shopapp;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.shopapp.classes.UserAuth;
import com.example.shopapp.models.Order;
import com.example.shopapp.models.Product;
import com.example.shopapp.services.MyService;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.fragment.NavHostFragment;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.ReplaySubject;


public class MainActivity extends AppCompatActivity {
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    public static BehaviorSubject<UserAuth> userAuth = BehaviorSubject.create();
    private UserAuth userAuthObj;
    private FragmentContainerView fragmentContainerView;
    private AtomicBoolean isNotificationsSetUp = new AtomicBoolean(false);
    private static final int NOTIFY_ID = 101;
    private static String CHANNEL_ID = "Order channel";
    public static String NOTIFICATION_KEY = "NOTIFICATION_KEY";
    public static String SEARCH_QUERY = "SEARCH_QUERY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));

        fragmentContainerView = findViewById(R.id.nav_host_fragment);

        startService(new Intent(this, MyService.class));
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

        AtomicInteger fragmentId = new AtomicInteger(-1);

        searchView.setOnCloseListener(() -> {
            if(fragmentId.get() == -1){
                navController.navigate(fragmentId.get());
            } else {
                navController.navigate(R.id.nav_home);
            }

            return true;
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                fragmentId.set(navController.getCurrentDestination().getId());

                Bundle bundle = new Bundle();
                bundle.putString(SEARCH_QUERY, query);
                navController.navigate(R.id.nav_search, bundle);

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
                    break;
                case R.id.nav_add_product:
                    navController.navigate(R.id.nav_add_product_page);
                    break;
                case R.id.nav_orders:
                    navController.navigate(R.id.nav_order);
                    break;
                case R.id.nav_logout:
                    navController.navigate(R.id.nav_home);
                    FirebaseAuth.getInstance().signOut();
                    userAuthObj = new UserAuth();
                    userAuth.onNext(userAuthObj);
                    break;
                case R.id.nav_admin:
                    navController.navigate(R.id.nav_admin);
                    break;
            }

            drawerLayout.close();

            return true;
        });

        userAuthObj = new UserAuth();
        userAuth.onNext(userAuthObj);

        userAuth.subscribe(v -> {
            userAuthObj = v;

            Menu menu = navigationView.getMenu();

            if(!userAuthObj.isAdmin()){
                menu.findItem(R.id.nav_add_product).setVisible(false);
                menu.findItem(R.id.nav_admin).setVisible(false);
            } else {
                menu.findItem(R.id.nav_add_product).setVisible( true);
                menu.findItem(R.id.nav_admin).setVisible(true);
            }

            if(userAuthObj.isAuth()){
                menu.findItem(R.id.nav_login).setVisible(false);
                menu.findItem(R.id.nav_signup).setVisible(false);
                menu.findItem(R.id.nav_logout).setVisible(true);
            } else {
                menu.findItem(R.id.nav_login).setVisible(true);
                menu.findItem(R.id.nav_signup).setVisible(true);
                menu.findItem(R.id.nav_logout).setVisible(false);
            }

            if(userAuthObj.isAdmin() || !userAuthObj.isAuth()){
                menu.findItem(R.id.nav_orders).setVisible(false);
            } else {
                menu.findItem(R.id.nav_orders).setVisible(true);
            }

            if(userAuthObj.isAdmin() && !isNotificationsSetUp.get()){
                setUpNotifications();
            }
        }, this::handleError);
    }

    private void handleError(Throwable e){
        e.printStackTrace();
    }

    private void setUpNotifications(){
        isNotificationsSetUp.set(true);

        DatabaseReference dbOrderRef = FirebaseDatabase.getInstance().getReference(MyService.ORDER_KEY);

        dbOrderRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                if(snapshot.exists()){
                    Intent notificationIntent = new Intent(MainActivity.this, MainActivity.class);

                    PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this,
                            0, notificationIntent,
                            PendingIntent.FLAG_CANCEL_CURRENT);

                    String longContent = "New order has been added";

                    NotificationCompat.Builder builder =
                            new NotificationCompat.Builder(MainActivity.this, CHANNEL_ID)
                                    .setContentTitle("New order")
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                    .setStyle(new NotificationCompat.BigTextStyle().bigText(longContent))
                                    .addAction(R.drawable.ic_baseline_open_in_new_24, "Visit", pendingIntent);

                    NotificationManagerCompat notificationManager =
                            NotificationManagerCompat.from(MainActivity.this);
                    notificationManager.notify(NOTIFY_ID, builder.build());
                }
            }

            @Override
            public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
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