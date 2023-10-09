package com.evileye2002.real_timechatapp.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;

import com.evileye2002.real_timechatapp.R;
import com.evileye2002.real_timechatapp.databinding.ActivitySignUpBinding;
import com.evileye2002.real_timechatapp.utilities.PreferenceManager;
import com.evileye2002.real_timechatapp.utilities._const;
import com.evileye2002.real_timechatapp.utilities._firestore;
import com.evileye2002.real_timechatapp.utilities._funct;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {
    ActivitySignUpBinding binding;
    PreferenceManager manager;
    String encodedImage;
    Boolean isExistEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        setListener();
    }

    void init() {
        manager = new PreferenceManager(getApplicationContext());
    }

    void setListener() {
        binding.textSignIn.setOnClickListener(v -> onBackPressed());
        binding.btnSignUp.setOnClickListener(v -> {
            if (isValidUserDetails()) {
                signUp();
            }
        });
        binding.layoutImage.setOnClickListener(v -> {
            requestPermission();
        });

        _funct.onTextChange(binding.inputName, binding.layoutName, this::onTextChange);
        _funct.onTextChange(binding.inputPassword, binding.layoutPassword, this::onTextChange);
        _funct.onTextChange(binding.inputConfirmPassword, binding.layoutConfirmPassword, this::onTextChange);
        _funct.onTextChange(binding.inputEmail, binding.layoutEmail, (s, textInputLayout) -> {
            if (s.toString().isEmpty())
                return;

            if (textInputLayout.getError() != null)
                textInputLayout.setError(null);

            if (!Patterns.EMAIL_ADDRESS.matcher(s.toString()).matches()) {
                return;
            }

            //Check Email Exist
            _const.firestore
                    .collection(_const.COLLECTION_USERS)
                    .whereEqualTo(_const.EMAIL, s.toString().trim().toLowerCase())
                    .get()
                    .addOnCompleteListener(task -> {
                        boolean isExist = task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0;
                        if (isExist)
                            isExistEmail = true;
                        else
                            isExistEmail = false;
                    });

        });

    }

    void onTextChange(CharSequence s, TextInputLayout textInputLayout) {
        if (s.toString().isEmpty())
            return;
        if (textInputLayout.getError() != null)
            textInputLayout.setError(null);
    }

    void requestPermission() {
        boolean isHasPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (isHasPermission) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        } else
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
    }

    void signUp() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(_const.NAME, binding.inputName.getText().toString());
        user.put(_const.EMAIL, binding.inputEmail.getText().toString());
        user.put(_const.PASSWORD, binding.inputPassword.getText().toString());
        user.put(_const.IMAGE, encodedImage);

        _firestore.allUsers().add(user).addOnSuccessListener(documentReference -> {
            loading(false);
            onBackPressed();
        });
    }

    void loading(Boolean isLoading) {
        if (isLoading) {
            binding.btnSignUp.setVisibility(View.INVISIBLE);
            binding.processBar.setVisibility(View.VISIBLE);
        } else {
            binding.btnSignUp.setVisibility(View.VISIBLE);
            binding.processBar.setVisibility(View.INVISIBLE);
        }
    }

    void addEmptyError() {
        String required = getResources().getString(R.string.required_field);
        if (binding.inputName.getText().toString().trim().isEmpty()) {
            binding.layoutName.setError(required);
        }
        if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            binding.layoutEmail.setError(required);
        }
        if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            binding.layoutPassword.setError(required);
        }
        if (binding.inputConfirmPassword.getText().toString().trim().isEmpty()) {
            binding.layoutConfirmPassword.setError(required);
        }
    }

    Boolean isValidUserDetails() {
        addEmptyError();
        if (encodedImage == null) {
            _funct.showToast(getApplicationContext(), "Chưa chọn ảnh!");
            return false;
        }
        if (binding.inputName.getText().toString().trim().isEmpty())
            return false;
        if (binding.inputEmail.getText().toString().trim().isEmpty())
            return false;
        if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            binding.layoutEmail.setError("*Sai định dạng Email");
            return false;
        }
        if (isExistEmail) {
            binding.layoutEmail.setError("*Email đã được sử dụng");
            return false;
        }
        if (binding.inputPassword.getText().toString().trim().isEmpty())
            return false;
        if (!binding.inputPassword.getText().toString().trim().equals(binding.inputConfirmPassword.getText().toString().trim())) {
            binding.layoutConfirmPassword.setError("*Mật khẩu xác nhận không chính xác");
            return false;
        }

        return true;
    }

    String encodeImage(Bitmap bitmap) {
        int width = 150;
        int height = bitmap.getHeight() * width / bitmap.getWidth();

        Bitmap preview = Bitmap.createScaledBitmap(bitmap, width, height, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        preview.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Uri imageUri = result.getData().getData();
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        binding.imageProfile.setImageBitmap(bitmap);
                        binding.iconCamera.setVisibility(View.GONE);
                        encodedImage = encodeImage(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
    );
}