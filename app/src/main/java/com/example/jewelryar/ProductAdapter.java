package com.example.jewelryar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.VH> {

    public interface OnProductClick {
        void onClick(Product product);
    }

    private final List<Product> items;
    private final OnProductClick listener;

    public ProductAdapter(List<Product> items, OnProductClick listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Product p = items.get(position);
        h.tvName.setText(p.name);
        h.tvCategory.setText(p.category.toUpperCase());
        h.tvPrice.setText(String.format("₹%,.0f", p.price));

        Glide.with(h.itemView.getContext())
                .load(p.image_url)
                .into(h.img);

        h.itemView.setOnClickListener(v -> listener.onClick(p));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvName, tvCategory, tvPrice;

        VH(View v) {
            super(v);
            img = v.findViewById(R.id.imgProduct);
            tvName = v.findViewById(R.id.tvName);
            tvCategory = v.findViewById(R.id.tvCategory);
            tvPrice = v.findViewById(R.id.tvPrice);
        }
    }
}