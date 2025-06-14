package com.example.woofly;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MoodLogManager {
    private static final String PREFS_NAME = "MoodLogPrefs";
    private static final String KEY_LOG = "MoodLog";

    public static void saveEntry(Context context, MoodEntry entry) {
        List<MoodEntry> currentLog = getLog(context);
        currentLog.add(0, entry); // newest first

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LOG, new Gson().toJson(currentLog)).apply();
    }

    public static List<MoodEntry> getLog(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_LOG, null);
        Type type = new TypeToken<List<MoodEntry>>() {}.getType();
        return json == null ? new ArrayList<>() : new Gson().fromJson(json, type);
    }
}
