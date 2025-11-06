package com.example.job.utils;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.os.Handler;
import android.os.Looper;

import com.example.job.R;

public class CustomToast {

    public static void show(Activity activity, String message) {
        if (activity == null || activity.isFinishing()) {
            return;
        }

        ViewGroup rootView = (ViewGroup) activity.findViewById(android.R.id.content);
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast_layout, null);

        TextView text = layout.findViewById(R.id.custom_toast_text);
        text.setText(message);

        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        int marginTop = (int) (16 * activity.getResources().getDisplayMetrics().density);
        params.setMargins(0, marginTop, 0, 0);

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(params);
        layout.setLayoutParams(layoutParams);

        rootView.addView(layout);

        layout.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            layout.setX((rootView.getWidth() - layout.getWidth()) / 2f);
        });

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (layout.getParent() != null) {
                rootView.removeView(layout);
            }
        }, 4000);
    }
}
