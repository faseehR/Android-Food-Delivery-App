package com.example.foodx;

import android.os.Parcel;
import android.os.Parcelable;

public class Item implements Parcelable {
    private String name;
    private String image;
    private String size;
    private String price;
    private String vendorId; // <-- add this

    public Item() {}

    public Item(String name, String image, String size, String price, String vendorId) {
        this.name = name;
        this.image = image;
        this.size = size;
        this.price = price;
        this.vendorId = vendorId;
    }

    protected Item(Parcel in) {
        name = in.readString();
        image = in.readString();
        size = in.readString();
        price = in.readString();
        vendorId = in.readString();
    }

    public static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

    public String getName() { return name; }
    public String getImage() { return image; }
    public String getSize() { return size; }
    public String getPrice() { return price; }
    public String getVendorId() { return vendorId; }

    public void setName(String name) { this.name = name; }
    public void setImage(String image) { this.image = image; }
    public void setSize(String size) { this.size = size; }
    public void setPrice(String price) { this.price = price; }
    public void setVendorId(String vendorId) { this.vendorId = vendorId; }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(image);
        dest.writeString(size);
        dest.writeString(price);
        dest.writeString(vendorId);
    }
}
