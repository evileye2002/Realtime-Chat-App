package com.evileye2002.real_timechatapp.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.evileye2002.real_timechatapp.databinding.ActivitySignUpBinding;
import com.evileye2002.real_timechatapp.utilities.Const;
import com.evileye2002.real_timechatapp.utilities.Funct;
import com.evileye2002.real_timechatapp.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {
    ActivitySignUpBinding binding;
    PreferenceManager preferenceManager;
    String encodedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListener();
    }

    void setListener(){
        binding.textSignIn.setOnClickListener(v -> onBackPressed());
        binding.btnSignUp.setOnClickListener(v -> {
            if(isValidUserDetails())
                signUp();
        });
        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    void signUp(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Const.KEY_NAME, binding.inputName.getText().toString());
        user.put(Const.KEY_EMAIL, binding.inputEmail.getText().toString());
        user.put(Const.KEY_PASSWORD, binding.inputPassword.getText().toString());
        user.put(Const.KEY_IMAGE, encodedImage);
        database.collection(Const.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    preferenceManager.putBoolean(Const.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(Const.KEY_USER_ID, documentReference.getId());
                    preferenceManager.putString(Const.KEY_NAME, binding.inputName.getText().toString());
                    preferenceManager.putString(Const.KEY_IMAGE, encodedImage);

                    /*Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);*/
                    onBackPressed();
                });
    }

    void loading(Boolean isLoading){
        if(isLoading){
            binding.btnSignUp.setVisibility(View.INVISIBLE);
            binding.processBar.setVisibility(View.VISIBLE);
        }
        else {
            binding.btnSignUp.setVisibility(View.VISIBLE);
            binding.processBar.setVisibility(View.INVISIBLE);
        }
    }

    Boolean isValidUserDetails(){
        if(encodedImage == null){
            Funct.showToast(getApplicationContext(),"Chưa chọn ảnh!");
            return false;
        }
        if(binding.inputName.getText().toString().trim().isEmpty()){
            Funct.showToast(getApplicationContext(),"Chưa nhập Họ và Tên!");
            return false;
        }
        if(binding.inputEmail.getText().toString().trim().isEmpty()){
            Funct.showToast(getApplicationContext(),"Chưa nhập Email!");
            return false;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()){
            Funct.showToast(getApplicationContext(),"Sai định dạng Email!");
            return false;
        }
        if(binding.inputPassword.getText().toString().trim().isEmpty()){
            Funct.showToast(getApplicationContext(),"Chưa nhập Mật khẩu!");
            return false;
        }
        if(!binding.inputPassword.getText().toString().trim().equals(binding.inputConfirmPassword.getText().toString().trim())){
            Funct.showToast(getApplicationContext(),"Mật khẩu xác nhận không chính xác!");
            binding.inputPassword.setText("");
            return false;
        }

        return true;
    }

    String encodeImage(Bitmap bitmap){
        int width = 150;
        int height = bitmap.getHeight() * width / bitmap.getWidth();

        Bitmap preview = Bitmap.createScaledBitmap(bitmap,width,height,false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        preview.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes,Base64.DEFAULT);
    }

    final ActivityResultLauncher<Intent> pickImage = registerForActivityResult( new ActivityResultContracts.StartActivityForResult(), result -> {
                if(result.getResultCode() == RESULT_OK){
                    Uri imageUri = result.getData().getData();

                    try {
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        binding.imageProfile.setImageBitmap(bitmap);
                        binding.iconCamera.setVisibility(View.GONE);
                        encodedImage = encodeImage(bitmap);
                    }
                    catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                }
            }
    );
}