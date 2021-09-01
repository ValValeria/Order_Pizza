package com.example.shopapp.classes;

import com.example.shopapp.config.Roles;
import com.example.shopapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import io.reactivex.rxjava3.subjects.PublishSubject;

public class UserAuth {
    private FirebaseAuth firebaseAuth;
    private User user = new User();
    private boolean isAuth = false;
    private boolean isAdmin = false;
    public static final PublishSubject<User> userAuth = PublishSubject.create();

    public UserAuth(){
        firebaseAuth = FirebaseAuth.getInstance();

        checkUser();
    }

    public void checkUser(){
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if(firebaseUser != null){
            UserAuth.this.isAuth = true;
            UserAuth.this.user.setEmail(firebaseUser.getEmail());
            UserAuth.this.user.setPassword("");

            String adminEmail = "adminadmin@gmc.com";

            if(UserAuth.this.user.getEmail().equalsIgnoreCase(adminEmail)){
                UserAuth.this.isAdmin = true;
                UserAuth.this.user.setRole(Roles.ADMIN);
            } else {
                UserAuth.this.user.setRole(Roles.USER);
            }
        }
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
