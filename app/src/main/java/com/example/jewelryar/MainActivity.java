package com.example.jewelryar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    EditText etEmail, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        findViewById(R.id.btnLogin).setOnClickListener(v -> doLogin());

        findViewById(R.id.tvRegister).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        findViewById(R.id.tvForgot).setOnClickListener(v ->
                startActivity(new Intent(this, ForgotActivity.class)));
    }

    private void doLogin() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString();

        if (email.isEmpty() || pass.isEmpty()) {
            toast("Enter email and password");
            return;
        }

        ApiClient.get().login(email, pass).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> res) {
                if (res.body() != null) {
                    toast(res.body().message);
                    if (res.body().success) {
                        startActivity(new Intent(MainActivity.this, CatalogActivity.class));
                        finish();
                    }
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