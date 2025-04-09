package com.example.quiz;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import java.util.List;

public class HistoryFragment extends Fragment {
    private NavigationListener navigationListener;
    private String currentUsername;

    @Override
    public void onAttach(android.content.Context context) {
        super.onAttach(context);
        if (context instanceof NavigationListener) {
            navigationListener = (NavigationListener) context;
        } else {
            throw new RuntimeException(context.toString() + " должен реализовать NavigationListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        if (currentUsername == null || currentUsername.isEmpty()) {
            Toast.makeText(getContext(), "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show();
            navigationListener.navigateToFragment(new LoginFragment(), true);
            return view;
        }

        TextView titleText = view.findViewById(R.id.history_title);
        if (titleText != null) {
            titleText.setText(R.string.history_title);
        }

        ListView historyList = view.findViewById(R.id.history_list);
        if (historyList == null) {
            Toast.makeText(getContext(), "Ошибка: не удалось загрузить историю", Toast.LENGTH_SHORT).show();
            return view;
        }

        DatabaseHelper db = new DatabaseHelper(getActivity());
        int userId = db.getUserId(currentUsername);
        
        if (userId < 0) {
            Toast.makeText(getContext(), "Ошибка: пользователь не найден", Toast.LENGTH_SHORT).show();
            navigationListener.navigateToFragment(new LoginFragment(), true);
            return view;
        }

        List<QuizResult> results = db.getUserHistory(userId);
        if (results == null) {
            Toast.makeText(getContext(), "Ошибка: не удалось загрузить историю", Toast.LENGTH_SHORT).show();
            return view;
        }

        HistoryAdapter adapter = new HistoryAdapter(getActivity(), results);
        historyList.setAdapter(adapter);

        return view;
    }

    public void setCurrentUsername(String username) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        this.currentUsername = username;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        navigationListener = null;
    }
} 