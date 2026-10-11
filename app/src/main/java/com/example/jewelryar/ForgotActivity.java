package com.example.jewelryar;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotActivity extends AppCompatActivity {

    EditText etEmail, etPassword, etConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirm = findViewById(R.id.etConfirm);

        findViewById(R.id.btnReset).setOnClickListener(v -> doReset());
        findViewById(R.id.tvBackLogin).setOnClickListener(v -> finish());
    }

    private void doReset() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString();
        String confirm = etConfirm.getText().toString();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { toast("Enter a valid email"); return; }
        if (pass.length() < 6) { toast("Password must be at least 6 characters"); return; }
        if (!pass.equals(confirm)) { toast("Passwords do not match"); return; }

        ApiClient.get().resetPassword(email, pass).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> res) {
                if (res.body() != null) {
                    toast(res.body().message);
                    if (res.body().success) finish();
                } else {
                    toast("Server error: " + res.code());
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                toast("Network error: " + t.getMessage());
            }
        });
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}