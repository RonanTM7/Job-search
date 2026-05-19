package job.search.app;

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
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        registerGuestInFirestore(user, savedInstanceState);
                    }
                }
            });
            return;
        } else if (currentUser.isAnonymous()) {
            // Если пользователь уже гость, убеждаемся что он есть в базе и инициализируем
            registerGuestInFirestore(currentUser, savedInstanceState);
            return;
        }

        // Если пользователь анонимный, позволяем ему видеть MainActivity (вакансии)
        // Но скрываем/блокируем функции, требующие полной регистрации (favorites, applications)

        String role = getSharedPreferences("AppSettings", MODE_PRIVATE).getString("userRole", "seeker");

        if ("admin".equals(role)) {
            startActivity(new Intent(this, AdminMainActivity.class));
            finish();
            return;
        }

        initApp(currentUser, savedInstanceState, role);
    }

    private void registerGuestInFirestore(FirebaseUser user, Bundle savedInstanceState) {
        String uid = user.getUid();
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        java.util.Map<String, Object> guest = new java.util.HashMap<>();
        guest.put("username", "Гость (" + uid.substring(0, Math.min(4, uid.length())) + ")");
        guest.put("status", "active");
        guest.put("email", "guest_" + uid + "@anonymous.auth");
        guest.put("phone", "N/A");

        db.collection("seekers").document(uid).set(guest, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    getSharedPreferences("AppSettings", MODE_PRIVATE).edit().putString("userRole", "seeker").apply();
                    initApp(user, savedInstanceState, "seeker");
                })
                .addOnFailureListener(e -> android.util.Log.e("MainActivity", "Guest Firestore reg failed", e));
    }

    private void initApp(FirebaseUser user, Bundle savedInstanceState, String role) {
        listenToUserStatus(user.getUid(), role);
        updateFcmToken(user.getUid(), role);

        setContentView(R.layout.activity_main);
        checkForUpdates();

        bottomNavigation = findViewById(R.id.bottom_navigation);
        setupBottomNavigation(role);

        if (savedInstanceState == null) {
            if ("employer".equals(role)) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new EmployerApplicationsFragment())
                        .commit();
            } else {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();
            }
        }
    }

    private void setupBottomNavigation(String role) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        boolean isAnonymous = user != null && user.isAnonymous();

        if ("employer".equals(role)) {
            bottomNavigation.getMenu().findItem(R.id.nav_home).setVisible(false);
            bottomNavigation.getMenu().findItem(R.id.nav_favorites).setVisible(false);
            bottomNavigation.getMenu().findItem(R.id.nav_applications).setTitle("Отклики");
            bottomNavigation.getMenu().findItem(R.id.nav_add_vacancy).setVisible(true);
        }

        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();
                return true;
            } else if (itemId == R.id.nav_favorites) {
                if (isAnonymous) {
                    startActivity(new Intent(this, LoginActivity.class));
                    return false;
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new FavoritesFragment())
                        .commit();
                return true;
            } else if (itemId == R.id.nav_applications) {
                if (isAnonymous) {
                    startActivity(new Intent(this, LoginActivity.class));
                    return false;
                }
                if ("employer".equals(role)) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new EmployerApplicationsFragment())
                            .commit();
                } else {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new ApplicationsFragment())
                            .commit();
                }
                return true;
            } else if (itemId == R.id.nav_add_vacancy) {
                startActivity(new Intent(this, AddEditVacancyActivity.class));
                return false; // Don't select it as a fragment
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
            assert currentFragment.getView() != null;
            currentFragment.onViewCreated(currentFragment.getView(), null);
        } else if (currentFragment instanceof FavoritesFragment) {
            assert currentFragment.getView() != null;
            currentFragment.onViewCreated(currentFragment.getView(), null);
        } else if (currentFragment instanceof ApplicationsFragment) {
            assert currentFragment.getView() != null;
            currentFragment.onViewCreated(currentFragment.getView(), null);
        } else if (currentFragment instanceof EmployerApplicationsFragment) {
            assert currentFragment.getView() != null;
            currentFragment.onViewCreated(currentFragment.getView(), null);
        } else if (currentFragment instanceof SettingsFragment) {
            assert currentFragment.getView() != null;
            currentFragment.onViewCreated(currentFragment.getView(), null);
        }
    }

    private void listenToUserStatus(String uid, String role) {
        String collection = "seeker".equals(role) ? "seekers" : "employers";
        userStatusListener = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection(collection).document(uid)
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

    private void checkForUpdates() {
        job.search.app.utils.UpdateManager.checkForUpdates(new job.search.app.utils.UpdateManager.UpdateCheckCallback() {
            @Override
            public void onUpdateAvailable(String latestVersion, String downloadUrl) {
                showUpdateNotification(latestVersion, downloadUrl);
            }

            @Override
            public void onNoUpdate() {}

            @Override
            public void onError(Exception e) {}
        });
    }

    private void showUpdateNotification(String version, String url) {
        android.view.ViewGroup rootView = findViewById(android.R.id.content);
        if (rootView.findViewById(R.id.update_notification_card) != null) return;

        android.view.View notificationView = getLayoutInflater().inflate(R.layout.layout_update_notification, rootView, false);
        rootView.addView(notificationView);

        notificationView.findViewById(R.id.btn_update_now).setOnClickListener(v -> {
            job.search.app.utils.UpdateManager.downloadAndInstallApk(MainActivity.this, url, version);
            rootView.removeView(notificationView);
        });

        // Simple swipe up to dismiss logic
        notificationView.setOnTouchListener(new android.view.View.OnTouchListener() {
            private float initialY;
            @Override
            public boolean onTouch(android.view.View v, android.view.MotionEvent event) {
                switch (event.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        initialY = event.getRawY();
                        return true;
                    case android.view.MotionEvent.ACTION_MOVE:
                        float deltaY = event.getRawY() - initialY;
                        if (deltaY < 0) {
                            v.setTranslationY(deltaY);
                        }
                        return true;
                    case android.view.MotionEvent.ACTION_UP:
                        if (v.getTranslationY() < -v.getHeight() / 2) {
                            rootView.removeView(v);
                        } else {
                            v.animate().translationY(0).setDuration(200).start();
                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void updateFcmToken(String uid, String role) {
        com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) return;
                    String token = task.getResult();
                    String collection = "employer".equals(role) ? "employers" : "seekers";
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            .collection(collection).document(uid)
                            .update("fcmToken", token);
                });
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