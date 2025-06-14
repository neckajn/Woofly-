package com.example.woofly;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView moodTextView;
    private TextView tipTextView;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        imageView = findViewById(R.id.imgresult);
        moodTextView = findViewById(R.id.txtmood);
        tipTextView = findViewById(R.id.txttip);
        btnBack = findViewById(R.id.takeanotherbtn);

        Intent intent = getIntent();
        String mood = intent.getStringExtra("mood");
        String tip = intent.getStringExtra("tip");
        String imagePath = intent.getStringExtra("imagePath");

        // Load and display the image
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
        }

        // Apply glow effect to image based on mood
        ProcessingActivity.setImageGlowEffect(imageView, mood);


        // Set mood text with colored background
        moodTextView.setText(mood != null ? mood : "Unknown");
        ProcessingActivity.setMoodBackground(moodTextView, mood);

        // Set tip text
        tipTextView.setText(tip != null ? tip : "No tip available");

        btnBack.setOnClickListener(v -> {
            Intent backIntent = new Intent(ResultActivity.this, MainActivity.class);
            backIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(backIntent);
        });
    }
}