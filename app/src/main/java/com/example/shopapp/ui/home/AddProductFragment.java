package com.example.shopapp.ui.home;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.example.shopapp.R;
import com.example.shopapp.models.Product;
import com.example.shopapp.services.MyService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import io.reactivex.rxjava3.core.Observable;


public class AddProductFragment extends Fragment {
    private DatabaseReference db;
    private ChipGroup chipGroup;
    private TextInputEditText ingredients;
    private List<String> ingredientsList = new ArrayList<>();
    private ActivityResultLauncher<String> mGetContent;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private final Product product = new Product();

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseDatabase.getInstance().getReference(MyService.PRODUCT_KEY);
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                     try{
                         int randomNum = ThreadLocalRandom.current().nextInt(0, 1000);
                         String filename = randomNum + "image.png";
                         InputStream stream = requireActivity().getContentResolver().openInputStream(uri);
                         StorageReference imageRef = storageReference.child(filename);

                         byte[] bytes = new byte[stream.available()];
                         stream.read(bytes);

                         UploadTask uploadTask = imageRef.putBytes(bytes);
                         uploadTask.continueWithTask(task -> {
                             if (!task.isSuccessful()) {
                                 throw task.getException();
                             }

                             return imageRef.getDownloadUrl();
                         }).addOnCompleteListener(task -> {
                             if (task.isSuccessful()) {
                                 Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                                 Uri downloadUri = task.getResult();
                                 ImageView imageView = AddProductFragment.this.getView().findViewById(R.id.imageView);

                                 if(imageView != null){
                                     imageView.setImageBitmap(bitmap);
                                 }

                                 product.setImage(downloadUri.getPath());
                                 Toast.makeText(requireContext(), "Succeeded to download the image", Toast.LENGTH_LONG).show();
                             } else {
                                 task.getException().printStackTrace();
                                 Toast.makeText(requireContext(), "Failed to download the image", Toast.LENGTH_LONG).show();
                             }
                         });
                     }catch (Throwable e){
                         Toast.makeText(getContext(), "File is not found", Toast.LENGTH_LONG).show();
                     }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, 
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_product, container, false);

        Button button = view.findViewById(R.id.click);
        button.setOnClickListener(this::handleSubmit);

        Button uploadBtn = view.findViewById(R.id.uploadFile);
        uploadBtn.setOnClickListener(this::selectImage);

        ingredients = view.findViewById(R.id.ingredientsInput);
        ingredients.setOnKeyListener((v, keyCode, event) -> {
            if(event.isCtrlPressed()){
                String ingredient = ingredients.getText().toString();
                ingredientsList.add(ingredient);

                addChip(ingredient);

                return true;
            }

            return false;
        });

        chipGroup = view.findViewById(R.id.chipgroup);

        return view;
    }

    private void handleSubmit(View view){
        try{
            TextInputEditText title = getView().findViewById(R.id.titleInput);
            product.setTitle(title.getText().toString());

            TextInputEditText descr = getView().findViewById(R.id.descriptionInput);
            product.setDescription(descr.getText().toString());

            TextInputEditText price = getView().findViewById(R.id.priceInput);
            product.setPrice(Integer.parseInt(price.getText().toString()));

            TextInputEditText weight = getView().findViewById(R.id.weightInput);
            product.setWeight(Integer.parseInt(weight.getText().toString()));

            JSONArray jsonArray = new JSONArray(ingredientsList);
            String ingredientsJson = jsonArray.toString();
            product.setIngredients(ingredientsJson);

            DatabaseReference refProduct = db.push();
            product.setId(refProduct.getKey());

            refProduct.setValue(product);

            Toast.makeText(getContext(), "The dish is added to database", Toast.LENGTH_LONG).show();

            getView().post(() -> {
                NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                Bundle bundle = new Bundle();
                bundle.putString("key", product.getId());

                navController.navigate(R.id.nav_product, bundle);
            });
        } catch (Throwable e){
            e.printStackTrace();
        }
    }

    public void selectImage(View view){
       mGetContent.launch("image/**");
    }

    public void addChip(String chipString){
        Chip chip = (Chip) LayoutInflater.from(requireContext()).inflate(R.layout.chip, chipGroup, false);
        chip.setText(chipString);

        chipGroup.addView(chip, ingredientsList.size());
    }
}