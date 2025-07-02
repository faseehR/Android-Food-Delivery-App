package com.example.foodx;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class VendorAdapter extends RecyclerView.Adapter<VendorAdapter.VendorViewHolder> {

    private List<Vendor> vendorList;
    private Context context;

    public VendorAdapter(Context context, List<Vendor> vendorList) {
        this.context = context;
        this.vendorList = vendorList;
    }

    @NonNull
    @Override
    public VendorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.vendor_item, parent, false);
        return new VendorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VendorViewHolder holder, int position) {
        Vendor vendor = vendorList.get(position);
        holder.vendorName.setText(vendor.getName());
        holder.vendorCatchphrase.setText(vendor.getCatchphrase());

        // Decode and set Base64 image safely
        if (vendor.getImageBase64() != null && !vendor.getImageBase64().trim().isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(vendor.getImageBase64(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

                if (bitmap != null) {
                    holder.vendorImage.setImageBitmap(bitmap);
                } else {
                    Log.e("VendorAdapter", "Decoded bitmap is null. Check Base64 content.");
                    holder.vendorImage.setImageResource(R.drawable.icon); // fallback image
                }
            } catch (IllegalArgumentException e) {
                Log.e("VendorAdapter", "Base64 decode error: " + e.getMessage());
                holder.vendorImage.setImageResource(R.drawable.icon);
            }
        } else {
            Log.w("VendorAdapter", "Base64 image string is empty or null for vendor: " + vendor.getName());
            holder.vendorImage.setImageResource(R.drawable.icon); // fallback
        }


        // Handle click to go to UserItemActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, UserItemActivity.class);
            intent.putExtra("vendorId", vendor.getVendorId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return vendorList.size();
    }

    static class VendorViewHolder extends RecyclerView.ViewHolder {
        TextView vendorName, vendorCatchphrase;
        ImageView vendorImage;

        VendorViewHolder(@NonNull View itemView) {
            super(itemView);
            vendorName = itemView.findViewById(R.id.vendorName);
            vendorCatchphrase = itemView.findViewById(R.id.vendorCatchphrase);
            vendorImage = itemView.findViewById(R.id.vendorImage);
        }
    }
}
