package com.example.foodx;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserItemAdapter extends RecyclerView.Adapter<UserItemAdapter.ItemViewHolder> {

    private Context context;
    private List<Item> itemList;
    private OnAddToCartClickListener listener;
    private boolean orderExists = false; // 🔧 NEW FLAG

    public interface OnAddToCartClickListener {
        void onAddToCart(Item item);
    }

    public UserItemAdapter(Context context, List<Item> itemList, OnAddToCartClickListener listener) {
        this.context = context;
        this.itemList = itemList;
        this.listener = listener;
    }

    public void updateData(List<Item> newList) {
        this.itemList = newList;
        notifyDataSetChanged();
    }
    // 🔧 Setter to update order status from UserItemActivity
    public void setOrderExists(boolean orderExists) {
        this.orderExists = orderExists;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_row, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = itemList.get(position);

        holder.name.setText(item.getName());
        holder.size.setText(item.getSize());
        holder.price.setText("$" + item.getPrice());

        // Decode Base64 image
        try {
            if (item.getImage() != null && !item.getImage().trim().isEmpty()) {
                byte[] imageBytes = Base64.decode(item.getImage(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                if (bitmap != null) {
                    holder.image.setImageBitmap(bitmap);
                } else {
                    holder.image.setImageResource(R.drawable.ic_launcher_foreground);
                }
            } else {
                holder.image.setImageResource(R.drawable.ic_launcher_foreground);
            }
        } catch (Exception e) {
            holder.image.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // 🔧 Disable Add to Cart if order is already active
        if (orderExists) {
            holder.btnCart.setEnabled(false);
            holder.btnCart.setAlpha(0.5f); // make it visually disabled
        } else {
            holder.btnCart.setEnabled(true);
            holder.btnCart.setAlpha(1f);
        }

        holder.btnCart.setOnClickListener(v -> {
            if (!orderExists) {
                listener.onAddToCart(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView name, size, price;
        ImageView image;
        Button btnCart;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.itemName);
            size = itemView.findViewById(R.id.itemSize);
            price = itemView.findViewById(R.id.itemPrice);
            image = itemView.findViewById(R.id.itemImage);
            btnCart = itemView.findViewById(R.id.btnCart);
        }
    }
}
