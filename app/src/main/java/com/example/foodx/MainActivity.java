
package com.example.foodx;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button signInUserBtn, signInVendorBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find Buttons by ID
        signInUserBtn = findViewById(R.id.signInUserBtn);
        signInVendorBtn = findViewById(R.id.signInVendorBtn);

        // Button: Sign in as User
        signInUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UserSignInActivity.class);
                startActivity(intent);
            }
        });

        // Button: Sign in as Vendor
        signInVendorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VendorSignInActivity.class);
                startActivity(intent);
            }
        });
    }
}

