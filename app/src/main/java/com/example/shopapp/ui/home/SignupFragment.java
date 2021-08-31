package com.example.shopapp.ui.home;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.shopapp.MainActivity;
import com.example.shopapp.R;
import com.example.shopapp.classes.UserAuth;
import com.example.shopapp.validators.AuthValidator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;
import java.util.Objects;


public class SignupFragment extends Fragment {
    private NavController navController;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button button = requireActivity().findViewById(R.id.textButton);
        button.setOnClickListener(this::signUp);

        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();

        if(firebaseUser != null){
            Toast.makeText(getContext(), "You have already authenticated", Toast.LENGTH_LONG).show();

            view.postDelayed( () -> {
                navController.navigate(R.id.nav_home);
            },1000);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_signup, container, false);
    }

    public void signUp(View view){
        TextInputEditText emailInput = requireActivity().findViewById(R.id.emailField);
        TextInputEditText passwordInput = requireActivity().findViewById(R.id.passwordField);

        String email = Objects.requireNonNull(emailInput.getText()).toString();
        String password = Objects.requireNonNull(passwordInput.getText()).toString();

        AuthValidator authValidator = new AuthValidator(email, password);
        authValidator.validate();

        if(authValidator.getErrors().size() == 0){
            mAuth.createUserWithEmailAndPassword(email, password).
                    addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            view.post(()->{
                                UserAuth userAuth = new UserAuth();
                                MainActivity.userAuth.onNext(userAuth);

                                Toast.makeText(getActivity(), "Authentication is successful.",
                                        Toast.LENGTH_SHORT).show();

                                goHome();
                            });
                        } else {
                            view.post(()->{
                                Toast.makeText(getActivity(), "Authentication failed. Please change the email",
                                        Toast.LENGTH_SHORT).show();
                            });

                            Log.e(SignupFragment.class.getName(), task.getException().getMessage());
                        }
                    });
        } else {
            StringBuilder stringBuilder = new StringBuilder("Check the validity of fields.");

            for (int i = 0; i < authValidator.getErrors().size(); i++) {
                String error = authValidator.getErrors().get(i);

                stringBuilder = stringBuilder.append(error);
            }

            Toast.makeText(getContext(), stringBuilder.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void goHome(){
        NavController navController = Navigation.findNavController(getActivity(),R.id.nav_host_fragment);
        navController.navigate(R.id.nav_home);
    }
}