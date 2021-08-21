package com.example.shopapp.ui.home;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.example.shopapp.R;
import com.example.shopapp.validators.AuthValidator;
import com.google.android.material.textfield.TextInputEditText;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;


public class SignupFragment extends Fragment {
    private NavController navController;

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button button = requireActivity().findViewById(R.id.textButton);
        button.setOnClickListener(this::signUp);

        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
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

        } else {
            Toast.makeText(getContext(), "Check the validity of fields", Toast.LENGTH_LONG).show();
        }
    }
}