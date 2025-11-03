package com.example.job;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        view.findViewById(R.id.btn_my_profile).setOnClickListener(v -> startActivity(new Intent(getActivity(), MyProfileActivity.class)));

        view.findViewById(R.id.btn_privacy).setOnClickListener(v -> startActivity(new Intent(getActivity(), PrivacyActivity.class)));

        view.findViewById(R.id.btn_appearance).setOnClickListener(v -> startActivity(new Intent(getActivity(), AppearanceActivity.class)));

        view.findViewById(R.id.btn_about).setOnClickListener(v -> startActivity(new Intent(getActivity(), AboutActivity.class)));

        return view;
    }
}