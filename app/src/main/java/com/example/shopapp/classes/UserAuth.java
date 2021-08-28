package com.example.shopapp.classes;
import android.content.res.Resources;
import com.example.shopapp.R;
import com.example.shopapp.config.Roles;
import com.example.shopapp.models.User;
import com.google.firebase.auth.FirebaseAuth;

import io.reactivex.rxjava3.subjects.PublishSubject;

public class UserAuth {
    private FirebaseAuth firebaseAuth;
    private User user = new User();
    private boolean isAuth = false;
    private boolean isAdmin = false;
    public static final PublishSubject<User> userAuth = PublishSubject.create();

    public UserAuth(String email, String password){
        firebaseAuth = FirebaseAuth.getInstance();

        this.login(email, password);
    }

    public UserAuth(){}

    public void login(String email, String password){
        this.firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    UserAuth.this.isAuth = true;
                    UserAuth.this.user.setEmail(email);
                    UserAuth.this.user.setPassword(email);

                    String adminEmail = Resources.getSystem().getString(R.string.admin_email);
                    String adminPassword = Resources.getSystem().getString(R.string.admin_password);;

                    if(UserAuth.this.user.getEmail().equalsIgnoreCase(adminEmail)
                            && UserAuth.this.user.getPassword().equals(adminPassword)){
                        UserAuth.this.isAdmin = true;
                        UserAuth.this.user.setRole(Roles.ADMIN);
                    } else {
                        UserAuth.this.user.setRole(Roles.USER);
                    }
                });
    }

    public boolean isAuth() {
        return isAuth;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public User getUser(){
        return this.user;
    }
}
