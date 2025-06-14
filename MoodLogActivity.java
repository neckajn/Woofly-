package com.example.woofly;

import android.os.Bundle;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class MoodLogActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_log);

        List<MoodEntry> moodEntries = MoodLogManager.getLog(this);
        ListView listView = findViewById(R.id.moodLogListView);
        MoodLogAdapter adapter = new MoodLogAdapter(this, moodEntries);
        listView.setAdapter(adapter);
    }
}
