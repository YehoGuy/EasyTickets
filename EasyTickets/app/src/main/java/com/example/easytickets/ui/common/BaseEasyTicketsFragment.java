package com.example.easytickets.ui.common;

import android.view.View;

import androidx.fragment.app.Fragment;

import com.example.easytickets.EasyTicketsApplication;
import com.example.easytickets.di.AppContainer;
import com.google.android.material.snackbar.Snackbar;

public abstract class BaseEasyTicketsFragment extends Fragment {

    protected AppContainer getAppContainer() {
        return ((EasyTicketsApplication) requireActivity().getApplication()).getAppContainer();
    }

    protected void showSnackbar(View anchor, String message) {
        if (anchor != null && message != null && !message.trim().isEmpty()) {
            Snackbar.make(anchor, message, Snackbar.LENGTH_LONG).show();
        }
    }
}
