package com.example.woofly;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Random;
import java.util.Arrays;

public class ProcessingActivity extends AppCompatActivity {

    private Interpreter tflite;
    private List<String> labels;
    private ProgressBar progressBar;
    private TextView processingText;
    private Handler mainHandler;
    private ExecutorService executor;

    private final Random random = new Random();


    private static final String TAG = "ProcessingActivity";
    private static final int MODEL_INPUT_SIZE = 224;

    public static void setImageGlowEffect(ImageView imageView, String mood) {
        int glowColor = Color.parseColor("#9E9E9E"); // Default

        if (mood != null) {
            switch (mood.toLowerCase()) {
                case "calm and relaxed":
                    glowColor = Color.parseColor("#4CAF50"); // Green
                    break;
                case "anxious":
                    glowColor = Color.parseColor("#FF9800"); // Orange
                    break;
                case "playful":
                    glowColor = Color.parseColor("#2196F3"); // Blue
                    break;
                case "aggressive":
                    glowColor = Color.parseColor("#F44336"); // Red
                    break;
            }
        }

        // Create border with glow effect
        GradientDrawable border = new GradientDrawable();
        border.setShape(GradientDrawable.RECTANGLE);
        border.setCornerRadius(16f);
        border.setStroke(8, glowColor);
        border.setColor(Color.TRANSPARENT);

        imageView.setBackground(border);
        imageView.setElevation(12f); // Add shadow
        imageView.setPadding(16, 16, 16, 16);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing);

        progressBar = findViewById(R.id.progressBar);
        processingText = findViewById(R.id.processingText);
        mainHandler = new Handler(getMainLooper());
        executor = Executors.newSingleThreadExecutor();

        String imagePath = getIntent().getStringExtra("imagePath");
        if (imagePath == null) {
            finish();
            return;
        }

        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            finish();
            return;
        }

        executor.execute(() -> processImage(imagePath));
    }

    private void processImage(String imagePath) {
        try {
            tflite = new Interpreter(Utils.loadModelFile(this, "model.tflite"));
            labels = Utils.loadLabels(this, "labels.txt");

            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            if (bitmap == null) {
                finish();
                return;
            }

            Bitmap resized = Bitmap.createScaledBitmap(bitmap, MODEL_INPUT_SIZE, MODEL_INPUT_SIZE, true);

            ByteBuffer inputBuffer = ByteBuffer.allocateDirect(MODEL_INPUT_SIZE * MODEL_INPUT_SIZE * 3);
            inputBuffer.order(ByteOrder.nativeOrder());

            int[] pixels = new int[MODEL_INPUT_SIZE * MODEL_INPUT_SIZE];
            resized.getPixels(pixels, 0, MODEL_INPUT_SIZE, 0, 0, MODEL_INPUT_SIZE, MODEL_INPUT_SIZE);

            for (int pixel : pixels) {
                int r = (pixel >> 16) & 0xFF;
                int g = (pixel >> 8) & 0xFF;
                int b = pixel & 0xFF;
                inputBuffer.put((byte) r);
                inputBuffer.put((byte) g);
                inputBuffer.put((byte) b);
            }

            inputBuffer.rewind();

            // Get output quantization info
            float outputScale = tflite.getOutputTensor(0).quantizationParams().getScale();
            int outputZeroPoint = tflite.getOutputTensor(0).quantizationParams().getZeroPoint();

            byte[][] rawOutput = new byte[1][labels.size()];
            tflite.run(inputBuffer, rawOutput);

            // Dequantize and find the max
            int maxIndex = 0;
            float maxProb = (rawOutput[0][0] & 0xFF - outputZeroPoint) * outputScale;

            for (int i = 1; i < labels.size(); i++) {
                float prob = (rawOutput[0][i] & 0xFF - outputZeroPoint) * outputScale;
                if (prob > maxProb) {
                    maxProb = prob;
                    maxIndex = i;
                }
            }

            String rawLabel = labels.get(maxIndex);
            String cleanMood = rawLabel.replaceAll("^\\d+\\s*", "").trim();
            String tip = getTipForMood(cleanMood);

            mainHandler.post(() -> {
                Intent intent = new Intent(ProcessingActivity.this, ResultActivity.class);
                intent.putExtra("mood", cleanMood);
                intent.putExtra("tip", tip);
                intent.putExtra("imagePath", imagePath);
                startActivity(intent);
                finish();

                // Save mood entry to log
                String timestamp = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());
                MoodEntry entry = new MoodEntry(cleanMood, tip, imagePath, timestamp);
                MoodLogManager.saveEntry(ProcessingActivity.this, entry);

            });

        } catch (Exception e) {
            Log.e(TAG, "Model execution failed", e);
            mainHandler.post(() -> {
                Intent intent = new Intent(ProcessingActivity.this, ResultActivity.class);
                intent.putExtra("mood", "Couldn't determine the mood.");
                intent.putExtra("tip", "Please try another photo!");
                intent.putExtra("imagePath", imagePath);
                startActivity(intent);
                finish();
            });
        }
    }

    private String getTipForMood(String mood) {
        if (mood == null) return "🐾 Observe your dog's behavior closely.";

        switch (mood.toLowerCase()) {
            case "calm and relaxed":
                return getRandomTip(new String[]{
                        "🛋️ Your dog is calm. A perfect time to cuddle!",
                        "💤 They’re feeling safe and cozy. Maybe a belly rub?",
                        "🧘 Peaceful moments are the best. Sit with them quietly.",
                        "📚 Perfect time for some quiet bonding or reading together.",
                        "🐶 Give them a treat for being calm and chill!"
                });

            case "anxious":
                return getRandomTip(new String[]{
                        "😟 Try to comfort your dog in a quiet space.",
                        "🫂 Use a calming voice and soft petting to reassure them.",
                        "🏠 Provide a cozy safe spot like a crate or blanket fort.",
                        "🎵 Soft music can help calm an anxious pup.",
                        "🧴 Try calming sprays or anxiety wraps if available."
                });

            case "playful":
                return getRandomTip(new String[]{
                        "🎾 Your dog is playful. Time for some fun!",
                        "🐕 Grab their favorite toy and start a game!",
                        "🏃 A quick walk or fetch session would be great now.",
                        "🧩 Use puzzle toys to keep their mind stimulated.",
                        "🌳 Visit the park or backyard for energetic playtime!"
                });

            case "aggressive":
                return getRandomTip(new String[]{
                        "⚠️ Stay calm and give your dog some space.",
                        "🚫 Avoid eye contact and sudden movements.",
                        "🎯 Redirect their focus with a toy or command if trained.",
                        "🧘 Speak softly and move slowly around them.",
                        "🔒 If needed, isolate them in a calm, quiet room to cool down."
                });

            default:
                return "🐾 Observe your dog's behavior closely.";
        }
    }

    private String getRandomTip(String[] tips) {
        int index = (int) (Math.random() * tips.length);
        return tips[index];
    }


    public static void setMoodBackground(TextView moodTextView, String mood) {
        // Add padding to the TextView for better appearance
        moodTextView.setPadding(24, 16, 24, 16);

        // Create a drawable with rounded corners
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(24f); // Rounded corners

        // Set color based on exact mood strings from your model
        if (mood != null) {
            switch (mood.toLowerCase()) {
                case "calm and relaxed":
                    drawable.setColor(Color.parseColor("#4CAF50")); // Green
                    break;
                case "anxious":
                    drawable.setColor(Color.parseColor("#FF9800")); // Orange
                    break;
                case "playful":
                    drawable.setColor(Color.parseColor("#2196F3")); // Blue
                    break;
                case "aggressive":
                    drawable.setColor(Color.parseColor("#F44336")); // Red
                    break;
                default:
                    drawable.setColor(Color.parseColor("#9E9E9E")); // Gray for unknown
                    break;
            }
        } else {
            drawable.setColor(Color.parseColor("#9E9E9E")); // Gray for null
        }

        moodTextView.setBackground(drawable);
        moodTextView.setTextColor(Color.WHITE); // White text for better contrast
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tflite != null) tflite.close();
        if (executor != null) executor.shutdown();
    }
}