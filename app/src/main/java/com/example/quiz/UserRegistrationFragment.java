package com.example.quiz;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import android.text.InputType;
import android.widget.ImageButton;

public class UserRegistrationFragment extends Fragment {
    private EditText etUsername, etPassword;
    private NavigationListener navigationListener;
    private ImageButton togglePassword;

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
        View view = inflater.inflate(R.layout.fragment_user_registration, container, false);

        etUsername = view.findViewById(R.id.et_username);
        etPassword = view.findViewById(R.id.et_password);
        Button btnRegister = view.findViewById(R.id.btn_register);
        TextView tvLogin = view.findViewById(R.id.tv_login);
        togglePassword = view.findViewById(R.id.togglePassword);

        togglePassword.setOnClickListener(v -> {
            if (etPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)) {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                togglePassword.setImageResource(android.R.drawable.ic_menu_view);
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                togglePassword.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
            }
        });

        btnRegister.setOnClickListener(v -> {
            String user = etUsername.getText().toString();
            String pass = etPassword.getText().toString();

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(getActivity(), R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseHelper db = new DatabaseHelper(getActivity());
            if (db.checkUserExists(user)) {
                Toast.makeText(getActivity(), R.string.user_exists, Toast.LENGTH_SHORT).show();
            } else {
                db.addUser(user, pass);
                Toast.makeText(getActivity(), R.string.registration_success, Toast.LENGTH_SHORT).show();
                ((MainActivity) getActivity()).setCurrentUsername(user);
                navigationListener.navigateToFragment(new MenuFragment(), true);
            }
        });

        tvLogin.setOnClickListener(v -> 
            navigationListener.navigateToFragment(new LoginFragment(), true));

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        navigationListener = null;
    }
}