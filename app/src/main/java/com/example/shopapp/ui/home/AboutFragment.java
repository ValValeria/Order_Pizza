package com.example.shopapp.ui.home;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.example.shopapp.R;


public class AboutFragment extends Fragment {

    public AboutFragment(){
        super(R.layout.fragment_about);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        Button button = requireActivity().findViewById(R.id.contact_btn);
        button.setOnClickListener(this::call);

        Button button2 = requireActivity().findViewById(R.id.contact_btn_email);
        button2.setOnClickListener(this::composeEmail);
    }

    public void call(View view){
        int hasCallPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE);

        if(hasCallPermission != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(requireContext(), "The app doesn't has call permission", Toast.LENGTH_LONG).show();
        } else {
            Uri number = Uri.parse("tel:5551234");
            Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
            startActivity(callIntent);
        }
    }

    public void composeEmail(View view) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"inpizza@inpizza.com"});

        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}