package com.example.woofly;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.BaseAdapter;

import java.util.List;

public class MoodLogAdapter extends BaseAdapter {

    private final Context context;
    private final List<MoodEntry> moodEntries;

    public MoodLogAdapter(Context context, List<MoodEntry> moodEntries) {
        this.context = context;
        this.moodEntries = moodEntries;
    }

    @Override
    public int getCount() {
        return moodEntries.size();
    }

    @Override
    public Object getItem(int position) {
        return moodEntries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        TextView moodText;
        TextView tipText;
        TextView timestamp;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_mood_log, parent, false);
            holder = new ViewHolder();
            holder.moodText = convertView.findViewById(R.id.txtmood);
            holder.tipText = convertView.findViewById(R.id.txttip); // ✅ THIS LINE WAS MISSING
            holder.timestamp = convertView.findViewById(R.id.timestamp);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        MoodEntry entry = moodEntries.get(position);
        holder.moodText.setText(entry.getMood());
        holder.tipText.setText(entry.getTip());
        holder.timestamp.setText(entry.getTimestamp());

        return convertView;
    }
}
