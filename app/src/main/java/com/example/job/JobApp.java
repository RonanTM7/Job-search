package com.example.job;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.job.utils.CustomToast;

public class JobApp extends Application {

    private Activity currentActivity;
    private String activityBeforeNoInternet;
    private boolean isNoInternetVisible = false;
    private boolean isManualRecoveryInProgress = false;

    private static final String PREFS_NAME = "AppSettings";
    private static final String THEME_KEY = "isDarkTheme";
    private static final String FIRST_RUN_KEY = "isFirstRun";

    @Override
    public void onCreate() {
        super.onCreate();

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                currentActivity = activity;
                if (activity instanceof NoInternetActivity) {
                    isNoInternetVisible = true;
                }
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                currentActivity = activity;
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                currentActivity = activity;
                if (activity instanceof NoInternetActivity) {
                    isNoInternetVisible = true;
                }
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {}

            @Override
            public void onActivityStopped(@NonNull Activity activity) {}

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                if (currentActivity == activity) {
                    currentActivity = null;
                }
                if (activity instanceof NoInternetActivity) {
                    isNoInternetVisible = false;

                    // Если восстановление было ручным, перенаправляем здесь
                    if (isManualRecoveryInProgress) {
                        redirectIfNeeded();
                        isManualRecoveryInProgress = false;
                    }
                }
            }
        });

        setupNetworkMonitoring();

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstRun = sharedPreferences.getBoolean(FIRST_RUN_KEY, true);

        if (isFirstRun) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            sharedPreferences.edit().putBoolean(FIRST_RUN_KEY, false).apply();
        } else {
            boolean isDarkTheme = sharedPreferences.getBoolean(THEME_KEY, false);
            if (isDarkTheme) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        }
    }

    public void setManualRecoveryInProgress(boolean inProgress) {
        this.isManualRecoveryInProgress = inProgress;
    }

    private void setupNetworkMonitoring() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return;

        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        connectivityManager.registerNetworkCallback(networkRequest, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (isNoInternetVisible && !isManualRecoveryInProgress) {
                        if (currentActivity instanceof NoInternetActivity) {
                            currentActivity.finish();
                            // isNoInternetVisible = false; // Будет обнулено в onDestroy

                            // Показываем уведомление только при автоматическом восстановлении
                            CustomToast.showToast(currentActivity, getString(R.string.connection_restored_toast), 4000);

                            redirectIfNeeded();
                        }
                    }
                });
            }

            @Override
            public void onLost(@NonNull Network network) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (!isNoInternetVisible && currentActivity != null && !(currentActivity instanceof NoInternetActivity)) {
                        // Сохраняем имя текущей активности перед переходом
                        activityBeforeNoInternet = currentActivity.getClass().getSimpleName();

                        Intent intent = new Intent(currentActivity, NoInternetActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        isNoInternetVisible = true;
                    }
                });
            }
        });
    }

    private void redirectIfNeeded() {
        // Мы используем сохраненное имя активности, так как NoInternetActivity уже может быть закрыта,
        // а currentActivity еще не обновлена на предыдущую.
        String className = activityBeforeNoInternet;
        if (className == null) return;

        // Список Activity где НУЖНО оставить пользователя
        boolean shouldStay = className.equals("JobDetailActivity") ||
                           className.equals("CheckCurrentPasswordActivity") ||
                           className.equals("EnterNewPasswordActivity") ||
                           className.equals("ConfirmNewPasswordActivity") ||
                           className.equals("NewChangeEmailActivity") ||
                           className.equals("NewChangePhoneActivity") ||
                           className.equals("LoginActivity") ||
                           className.equals("RegisterActivity") ||
                           className.equals("ForgotPasswordActivity") ||
                           className.equals("MainActivity");

        if (!shouldStay) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("REFRESH_DATA", true);
            startActivity(intent);
        } else {
            // Если мы остаемся, и это MainActivity, нужно обновить данные
            if (className.equals("MainActivity") && currentActivity instanceof MainActivity) {
                ((MainActivity) currentActivity).onNewIntent(new Intent().putExtra("REFRESH_DATA", true));
            } else if (className.equals("MainActivity")) {
                 // На всякий случай, если currentActivity уже не MainActivity
                 Intent intent = new Intent(this, MainActivity.class);
                 intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                 intent.putExtra("REFRESH_DATA", true);
                 startActivity(intent);
            }
        }

        activityBeforeNoInternet = null;
    }
}
