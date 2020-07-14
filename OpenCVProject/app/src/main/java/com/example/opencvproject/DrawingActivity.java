package com.example.opencvproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;

public class DrawingActivity extends AppCompatActivity {

    private final static String TAG = "ALLISON";
    Canvas background;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);

        Bitmap bitmap = null;
        String filename = getIntent().getStringExtra("BitmapImage");
        Log.i(TAG, filename);
        try {
            FileInputStream is = new FileInputStream(new File(filename));
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        bitmap.recycle();
        background = new Canvas(mutableBitmap);
        ImageView imageView = findViewById(R.id.image_view);
        imageView.setImageBitmap(mutableBitmap);

        Toast.makeText(DrawingActivity.this, "Welcome to the drawing activity!", Toast.LENGTH_LONG).show();

    }
}
