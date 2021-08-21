package com.example.shopapp.validators;

import java.util.ArrayList;
import java.util.List;

public class AuthValidator {
    private String email;
    private String password;
    private List<String> errors = new ArrayList<>();

    public AuthValidator(String email, String password){
        this.email = email;
        this.password = password;
    }

    public boolean validate(){
        if(!(email.length() > 10)){
            this.errors.add("Invalid size of email. Min length of email is 10");
        } else if(!(email.length() < 20)){
            this.errors.add("Invalid size of email. Max length of email is 20");
        } else if(!(password.length() > 10)){
            this.errors.add("Invalid size of password. Min length of password is 10");
        } else if(!(password.length() < 20)){
            this.errors.add("Invalid size of password. Max length of email is 20");
        }

        return errors.size() == 0;
    }

    public List<String> getErrors(){
        return this.errors;
    }
}
