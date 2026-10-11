package com.example.jewelryar;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.content.Intent;

public class CatalogActivity extends AppCompatActivity {

    RecyclerView recycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        recycler = findViewById(R.id.recyclerProducts);
        recycler.setLayoutManager(new GridLayoutManager(this, 2));

        findViewById(R.id.tvLogout).setOnClickListener(v -> {
            getSharedPreferences("session", MODE_PRIVATE).edit().clear().apply();
            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });

        loadProducts();
    }

    private void loadProducts() {
        ApiClient.get().getProducts().enqueue(new Callback<ProductsResponse>() {
            @Override
            public void onResponse(Call<ProductsResponse> call, Response<ProductsResponse> res) {
                if (res.body() != null && res.body().success) {
                    recycler.setAdapter(new ProductAdapter(res.body().products, product -> {
                        Intent i = new Intent(CatalogActivity.this, TryOnActivity.class);
                        i.putExtra("product", product);
                        startActivity(i);
                    }));
                } else {
                    Toast.makeText(CatalogActivity.this, "Could not load products", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ProductsResponse> call, Throwable t) {
                Toast.makeText(CatalogActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}