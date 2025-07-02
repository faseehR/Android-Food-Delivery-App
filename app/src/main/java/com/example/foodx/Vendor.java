package com.example.foodx;

public class Vendor {
    private String name;
    private String logoBase64;
    private String catchPhrase;
    private String vendorId;

    public Vendor() {} // Required for Firestore

    public Vendor(String name, String logoBase64, String catchPhrase, String vendorId) {
        this.name = name;
        this.logoBase64 = logoBase64;
        this.catchPhrase = catchPhrase;
        this.vendorId = vendorId;
    }

    // Getters
    public String getName() { return name; }
    public String getImageBase64() { return logoBase64; }
    public String getCatchphrase() { return catchPhrase; }
    public String getVendorId() { return vendorId; }

    // Setters
    public void setImageBase64(String logoBase64) { this.logoBase64 = logoBase64; }
    public void setName(String name) { this.name = name; }
    public void setVendorId(String vendorId) { this.vendorId = vendorId; }
}
