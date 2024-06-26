package com.evileye2002.real_timechatapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;

import com.evileye2002.real_timechatapp.R;
import com.evileye2002.real_timechatapp.databinding.ActivitySignInBinding;
import com.evileye2002.real_timechatapp.utilities.PreferenceManager;
import com.evileye2002.real_timechatapp.utilities._const;
import com.evileye2002.real_timechatapp.utilities._firestore;
import com.evileye2002.real_timechatapp.utilities._funct;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;

public class SignInActivity extends AppCompatActivity {
    ActivitySignInBinding binding;
    PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        preferenceManager = new PreferenceManager(getApplicationContext());
        if (preferenceManager.getBoolean(_const.IS_SIGNED_IN)) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListener();
    }

    void setListener() {
        binding.textSignUp.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));
        binding.btnSignIn.setOnClickListener(v -> {
            if (isValid())
                signIn();
        });

        _funct.onTextChange(binding.inputEmail, binding.layoutEmail, this::onTextChange);
        _funct.onTextChange(binding.inputPassword, binding.layoutPassword, this::onTextChange);
    }

    void onTextChange(CharSequence s, TextInputLayout textInputLayout) {
        if (s.toString().isEmpty())
            return;
        if (textInputLayout.getError() != null)
            textInputLayout.setError(null);
    }

    void loading(Boolean isLoading) {
        if (isLoading) {
            binding.btnSignIn.setVisibility(View.INVISIBLE);
            binding.processBar.setVisibility(View.VISIBLE);
        } else {
            binding.btnSignIn.setVisibility(View.VISIBLE);
            binding.processBar.setVisibility(View.INVISIBLE);
        }
    }

    void signIn() {
        loading(true);
        _firestore.allUsers()
                .whereEqualTo(_const.EMAIL, binding.inputEmail.getText().toString().trim().toLowerCase())
                .whereEqualTo(_const.PASSWORD, binding.inputPassword.getText().toString().trim())
                .get()
                .addOnCompleteListener(task -> {
                    boolean isCorrect = task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() == 1;
                    if (isCorrect) {
                        if(binding.layoutPassword.getError() != null)
                            binding.layoutPassword.setError(null);

                        DocumentSnapshot currentUser = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(_const.IS_SIGNED_IN, true);
                        preferenceManager.putString(_const.ID, currentUser.getId());
                        preferenceManager.putString(_const.NAME, currentUser.getString(_const.NAME));
                        preferenceManager.putString(_const.IMAGE, currentUser.getString(_const.IMAGE));

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        loading(false);
                        binding.layoutPassword.setError("*Email hoặc mật khẩu không chính xác");
                    }
                });
    }

    void addEmptyError() {
        String required = getResources().getString(R.string.required_field);
        if (binding.inputEmail.getText().toString().trim().isEmpty())
            binding.layoutEmail.setError(required);
        if (binding.inputPassword.getText().toString().trim().isEmpty())
            binding.layoutPassword.setError(required);
    }

    Boolean isValid() {
        addEmptyError();
        if (binding.inputEmail.getText().toString().trim().isEmpty())
            return false;
        if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            binding.layoutEmail.setError("*Sai định dạng Email");
            return false;
        }
        if (binding.inputPassword.getText().toString().trim().isEmpty())
            return false;

        return true;
    }
}