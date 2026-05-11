package com.example.job;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class NoInternetActivity extends AppCompatActivity {

    private Button btnTryAgain;
    private ProgressBar progressBar;
    private TextView tvConnectionRestored;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_no_internet);

        btnTryAgain = findViewById(R.id.btnTryAgain);
        progressBar = findViewById(R.id.progressBar);
        tvConnectionRestored = findViewById(R.id.tvConnectionRestored);

        btnTryAgain.setOnClickListener(v -> tryReconnect());
    }

    private void tryReconnect() {
        btnTryAgain.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        // Имитируем проверку соединения
        handler.postDelayed(() -> {
            if (isNetworkAvailable()) {
                // Сообщаем JobApp, что начато ручное восстановление, чтобы не было дублирующих уведомлений
                if (getApplication() instanceof JobApp) {
                    ((JobApp) getApplication()).setManualRecoveryInProgress(true);
                }

                progressBar.setVisibility(View.GONE);
                tvConnectionRestored.setVisibility(View.VISIBLE);

                // Ждем 5 секунд и закрываем активность
                handler.postDelayed(this::finish, 5000);
            } else {
                progressBar.setVisibility(View.GONE);
                btnTryAgain.setVisibility(View.VISIBLE);
            }
        }, 1500); // Небольшая задержка для анимации загрузки
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        // Блокируем кнопку назад
    }
}
