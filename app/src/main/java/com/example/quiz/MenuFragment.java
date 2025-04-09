package com.example.quiz;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MenuFragment extends Fragment {
    private NavigationListener navigationListener;

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
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        view.findViewById(R.id.btn_start).setOnClickListener(v -> showDifficultyDialog());
        view.findViewById(R.id.btn_history).setOnClickListener(v ->
                navigationListener.navigateToFragment(new HistoryFragment(), true));

        return view;
    }

    private void showDifficultyDialog() {
        String[] levels = {
            getString(R.string.easy),
            getString(R.string.medium),
            getString(R.string.hard)
        };
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.select_difficulty)
                .setItems(levels, (dialog, which) -> {
                    String difficulty = which == 0 ? "easy" : which == 1 ? "medium" : "hard";
                    navigationListener.navigateToFragment(QuestionFragment.newInstance(difficulty), true);
                })
                .show();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        navigationListener = null;
    }
}