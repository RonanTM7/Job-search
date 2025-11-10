package com.example.job.utils;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.job.R;

public class CustomToast {

    public static void showToast(Activity activity, String message, int duration) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, null);

        TextView text = (TextView) layout.findViewById(R.id.toast_text);
        text.setText(message);

        final Toast toast = new Toast(activity.getApplicationContext());
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 50);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.show();
            }
        }, 0);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, duration);
    }
}
