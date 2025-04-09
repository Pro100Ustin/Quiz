package com.example.quiz;

import androidx.fragment.app.Fragment;

public interface NavigationListener {
    void navigateToFragment(Fragment fragment, boolean addToBackStack);
}
