package com.example.shopapp.ui.home;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.shopapp.MainActivity;
import com.example.shopapp.R;
import com.example.shopapp.adapters.ErrorsAdapter;
import com.example.shopapp.classes.UserAuth;
import com.example.shopapp.validators.AuthValidator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import io.reactivex.rxjava3.subjects.PublishSubject;


public class LoginFragment extends Fragment {
    private NavController navController;
    private RecyclerView recyclerView;
    private List<String> errors = new ArrayList<>();
    private ErrorsAdapter errorAdapter;
    private final PublishSubject<String> source = PublishSubject.create();
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    public LoginFragment(){
        super(R.layout.fragment_login);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(getActivity().findViewById(R.id.nav_host_fragment));

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        Button button = getActivity().findViewById(R.id.login_btn);
        button.setOnClickListener(this::login);

        if(firebaseUser != null){
            Toast.makeText(getContext(), "You have already authenticated", Toast.LENGTH_LONG).show();

            view.postDelayed( () -> {
                navController.navigate(R.id.nav_home);
            },1000);
        } else {
            recyclerView = requireActivity().findViewById(R.id.list);
            errorAdapter = new ErrorsAdapter(getContext(), errors, source);
            recyclerView.setAdapter(errorAdapter);
        }
    }

    public void login(View view){
        TextInputEditText emailInput = requireActivity().findViewById(R.id.emailField);
        TextInputEditText passwordInput = requireActivity().findViewById(R.id.passwordField);

        String email = Objects.requireNonNull(emailInput.getText()).toString();
        String password = Objects.requireNonNull(passwordInput.getText()).toString();

        AuthValidator authValidator = new AuthValidator(email, password);
        authValidator.validate();

        if(authValidator.getErrors().size() == 0){
            firebaseAuth.signInWithEmailAndPassword(email,password)
                     .addOnCompleteListener(requireActivity(), task -> {
                         if (task.isSuccessful()) {
                             UserAuth userAuth = new UserAuth();
                             MainActivity.userAuth.onNext(userAuth);

                             Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(R.id.nav_home);
                         } else {
                             view.post(()->{
                                 Toast.makeText(getActivity(), "You are not in our database",
                                         Toast.LENGTH_SHORT).show();
                             });
                         }
                     });
        } else {
            this.errors.add("Please, check the validity of fields");
        }

        for (String error: this.errors) {
            this.source.onNext(error);
            this.recyclerView.invalidate();
        }

        for(String error: authValidator.getErrors()){
            Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
        }
    }
}