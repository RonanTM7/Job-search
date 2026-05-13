package com.example.job;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private com.google.android.material.bottomnavigation.BottomNavigationView bottomNavigation;
    private static final String PREFS_NAME = "AppSettings";
    private static final String THEME_KEY = "isDarkTheme";
    private com.google.firebase.firestore.ListenerRegistration userStatusListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getBooleanExtra("REFRESH_DATA", false)) {
            refreshCurrentFragment();
        }
        applySavedTheme();
        // Проверка авторизации
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // Если пользователя нет, регистрируем как гостя
            mAuth.signInAnonymously().addOnCompleteListener(task -> {
                if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                    registerGuestInFirestore(mAuth.getCurrentUser());
                    initApp(mAuth.getCurrentUser(), savedInstanceState);
                }
            });
            return;
        }

        if (currentUser.isAnonymous()) {
            registerGuestInFirestore(currentUser);
            initApp(currentUser, savedInstanceState);
            return;
        }

        if ("ronanauf@gmail.com".equals(currentUser.getEmail())) {
            startActivity(new Intent(this, AdminMainActivity.class));
            finish();
            return;
        }

        initApp(currentUser, savedInstanceState);
    }

    private void registerGuestInFirestore(FirebaseUser user) {
        String uid = user.getUid();
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        java.util.Map<String, Object> guest = new java.util.HashMap<>();
        guest.put("username", "Гость (" + uid.substring(0, Math.min(4, uid.length())) + ")");
        guest.put("status", "active");
        guest.put("email", "guest_" + uid + "@anonymous.auth");
        guest.put("phone", "N/A");

        db.collection("users").document(uid).set(guest, com.google.firebase.firestore.SetOptions.merge())
                .addOnFailureListener(e -> android.util.Log.e("MainActivity", "Guest Firestore reg failed", e));
    }

    private void initApp(FirebaseUser user, Bundle savedInstanceState) {
        listenToUserStatus(user.getUid());

        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottom_navigation);
        setupBottomNavigation();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            boolean isAnonymous = user == null || user.isAnonymous();

            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();
                return true;
            } else if (itemId == R.id.nav_favorites) {
                if (isAnonymous) {
                    com.example.job.utils.CustomToast.showToast(this, "Для начала авторизуйтесь", 4000);
                    startActivity(new Intent(this, LoginActivity.class));
                    return false;
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new FavoritesFragment())
                        .commit();
                return true;
            } else if (itemId == R.id.nav_applications) {
                if (isAnonymous) {
                    com.example.job.utils.CustomToast.showToast(this, "Для начала авторизуйтесь", 4000);
                    startActivity(new Intent(this, LoginActivity.class));
                    return false;
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ApplicationsFragment())
                        .commit();
                return true;
            } else if (itemId == R.id.nav_settings) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new SettingsFragment())
                        .commit();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent.getBooleanExtra("REFRESH_DATA", false)) {
            refreshCurrentFragment();
        }
    }

    private void refreshCurrentFragment() {
        androidx.fragment.app.Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof HomeFragment) {
            ((HomeFragment) currentFragment).onViewCreated(currentFragment.getView(), null);
        } else if (currentFragment instanceof FavoritesFragment) {
            ((FavoritesFragment) currentFragment).onViewCreated(currentFragment.getView(), null);
        } else if (currentFragment instanceof ApplicationsFragment) {
            ((ApplicationsFragment) currentFragment).onViewCreated(currentFragment.getView(), null);
        } else if (currentFragment instanceof SettingsFragment) {
            ((SettingsFragment) currentFragment).onViewCreated(currentFragment.getView(), null);
        }
    }

    private void listenToUserStatus(String uid) {
        userStatusListener = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) return;
                    if (snapshot != null && snapshot.exists()) {
                        String status = snapshot.getString("status");
                        if ("blocked".equals(status)) {
                            Intent intent = new Intent(MainActivity.this, BlockedActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else if ("deleted".equals(status)) {
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.putExtra("TOAST_MESSAGE", "Ваш аккаунт был удален");
                            startActivity(intent);
                            finish();
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userStatusListener != null) {
            userStatusListener.remove();
        }
    }

    private void applySavedTheme() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isDarkTheme = sharedPreferences.getBoolean(THEME_KEY, false);

        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}