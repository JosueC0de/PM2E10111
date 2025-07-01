package com.example.pm2e10111.Configuracion;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pm2e10111.R;

public class ActivityVerImagen extends AppCompatActivity {

    ImageView imageView;
Button btnvolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_imagen);

        imageView = findViewById(R.id.imageViewVerImagen);
        Button btnVolver = findViewById(R.id.btnVolver);


        byte[] imageBytes = getIntent().getByteArrayExtra("imagen");

        if (imageBytes != null) {

            Bitmap selectedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            imageView.setImageBitmap(selectedImage);
        } else {
            Toast.makeText(this, "No hay imagen para mostrar", Toast.LENGTH_SHORT).show();
        }

        btnVolver.setOnClickListener(view -> {

            finish();
        });


    }
}