package com.example.quiz;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class ResultFragment extends Fragment {
    private static final String ARG_SCORE = "score";
    private NavigationListener navigationListener;

    public static ResultFragment newInstance(int score) {
        ResultFragment fragment = new ResultFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SCORE, score);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof NavigationListener) {
            navigationListener = (NavigationListener) context;
        } else {
            throw new RuntimeException(context.toString() + " должен реализовать NavigationListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_result, container, false);

        int score = getArguments().getInt(ARG_SCORE);
        TextView scoreText = view.findViewById(R.id.score_text);
        scoreText.setText(getString(R.string.your_result, score));

        Button restartButton = view.findViewById(R.id.btn_restart);
        restartButton.setOnClickListener(v -> 
            navigationListener.navigateToFragment(new MenuFragment(), false));

        Button shareButton = view.findViewById(R.id.btn_share);
        shareButton.setOnClickListener(v -> shareResult(score));

        return view;
    }

    private void shareResult(int score) {
        android.content.Intent shareIntent = new android.content.Intent();
        shareIntent.setAction(android.content.Intent.ACTION_SEND);
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, 
            getString(R.string.share_text, score));
        shareIntent.setType("text/plain");
        startActivity(android.content.Intent.createChooser(shareIntent, getString(R.string.share_title)));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        navigationListener = null;
    }
} 