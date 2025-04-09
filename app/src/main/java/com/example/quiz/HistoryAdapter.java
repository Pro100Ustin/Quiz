package com.example.quiz;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

public class HistoryAdapter extends ArrayAdapter<QuizResult> {
    private Context context;
    private List<QuizResult> results;

    public HistoryAdapter(Context context, List<QuizResult> results) {
        super(context, R.layout.history_item, results);
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        if (results == null) {
            throw new IllegalArgumentException("Results list cannot be null");
        }
        this.context = context;
        this.results = results;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent cannot be null");
        }

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.history_item, parent, false);
        }

        QuizResult result = results.get(position);
        if (result == null) {
            return convertView;
        }

        TextView titleText = convertView.findViewById(R.id.history_item_title);
        TextView subtitleText = convertView.findViewById(R.id.history_item_subtitle);

        if (titleText != null) {
            titleText.setText(context.getString(R.string.history_item, result.getScore()));
        }

        if (subtitleText != null) {
            subtitleText.setText(context.getString(R.string.history_subtitle, 
                result.getDifficulty(), result.getDate()));
        }

        return convertView;
    }
} 