package com.example.job;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.job.adapter.JobAdapter;
import com.example.job.model.Job;
import java.util.ArrayList;
import java.util.List;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.job.adapter.CategoryAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.example.job.adapter.CategoryAdapter;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private RecyclerView jobsRecyclerView;
    private RecyclerView categoriesRecyclerView;
    private com.google.android.material.bottomnavigation.BottomNavigationView bottomNavigation;
    private JobAdapter jobAdapter;
    private CategoryAdapter categoryAdapter;
    private List<Job> jobList = new ArrayList<>();
    private List<Job> filteredJobList = new ArrayList<>();
    private List<String> categories = new ArrayList<>();
    private static final String PREFS_NAME = "AppSettings";
    private static final String THEME_KEY = "isDarkTheme";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applySavedTheme();
        // Проверка авторизации
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        jobsRecyclerView = findViewById(R.id.jobsRecyclerView);
        categoriesRecyclerView = findViewById(R.id.categoriesRecyclerView);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        setupUI();
        setupRecyclerView();
        setupCategoryRecyclerView();
        loadMockData();
        setupCategories();
        setupBottomNavigation();
    }

    private void setupUI() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    private void setupRecyclerView() {
        jobAdapter = new JobAdapter(filteredJobList, new JobAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Job job) {
                openJobDetails(job);
            }
        });

        jobsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        jobsRecyclerView.setAdapter(jobAdapter);
    }

    private void setupCategoryRecyclerView() {
        categoryAdapter = new CategoryAdapter(categories, new CategoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String category) {
                filterJobsByCategory(category);
            }
        });

        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        categoriesRecyclerView.setAdapter(categoryAdapter);
    }

    private void loadMockData() {
        jobList.clear();
        filteredJobList.clear();

        jobList.add(new Job("1", "Android Developer", "Яндекс", "150 000 - 200 000 ₽", "Москва", "Описание...", "Требования...", true, "Мобильная разработка"));
        jobList.add(new Job("2", "Java Developer", "Сбер", "180 000 - 220 000 ₽", "Москва", "Описание...", "Требования...", false, "Бэкенд"));
        jobList.add(new Job("3", "Flutter Developer", "VK", "140 000 - 190 000 ₽", "Санкт-Петербург", "Описание...", "Требования...", true, "Мобильная разработка"));
        jobList.add(new Job("4", "iOS Developer", "Tinkoff", "160 000 - 210 000 ₽", "Москва", "Описание...", "Требования...", true, "Мобильная разработка"));
        jobList.add(new Job("5", "Frontend Developer", "Ozon", "120 000 - 180 000 ₽", "Москва", "Описание...", "Требования...", true, "Фронтенд"));
        jobList.add(new Job("6", "QA Engineer", "Avito", "100 000 - 150 000 ₽", "Москва", "Описание...", "Требования...", false, "Тестирование"));
        jobList.add(new Job("7", "Data Scientist", "Mail.ru Group", "200 000 - 250 000 ₽", "Москва", "Описание...", "Требования...", false, "Аналитика"));
        jobList.add(new Job("8", "Product Manager", "Wildberries", "180 000 - 230 000 ₽", "Москва", "Описание...", "Требования...", false, "Менеджмент"));

        filteredJobList.addAll(jobList);
        jobAdapter.notifyDataSetChanged();
    }
    private void filterJobsByCategory(String category) {
        filteredJobList.clear();

        if (category.equals("Все")) {
            filteredJobList.addAll(jobList);
        } else {
            for (Job job : jobList) {
                if (job.getCategory().equals(category)) {
                    filteredJobList.add(job);
                }
            }
        }

        jobAdapter.notifyDataSetChanged();
    }

    private void setupCategories() {
        categories.add("Все");
        for (Job job : jobList) {
            if (!categories.contains(job.getCategory())) {
                categories.add(job.getCategory());
            }
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_search) {
                showSearch();
                return true;
            } else if (itemId == R.id.nav_profile) {
                openProfile();
                return true;
            }
            return false;
        });
    }

    private void showSearch() {
        // Поиск уже в toolbar
        Toast.makeText(this, "Используйте поиск вверху", Toast.LENGTH_SHORT).show();
    }

    private void openProfile() {
        startActivity(new Intent(this, ProfileActivity.class));
    }

    private void openJobDetails(Job job) {
        Intent intent = new Intent(this, JobDetailActivity.class);
        intent.putExtra("job", job);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        // Настройка поиска
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterJobs(newText);
                return true;
            }
        });

        return true;
    }

    private void filterJobs(String query) {
        filteredJobList.clear();

        if (query.isEmpty()) {
            filteredJobList.addAll(jobList);
        } else {
            for (Job job : jobList) {
                if (job.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        job.getCompany().toLowerCase().contains(query.toLowerCase()) ||
                        job.getLocation().toLowerCase().contains(query.toLowerCase())) {
                    filteredJobList.add(job);
                }
            }
        }

        jobAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_settings) {
            openSettings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    @Override
    public void onBackPressed() {
        // Подтверждение выхода
        super.onBackPressed();
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Выход")
                .setMessage("Вы уверены, что хотите выйти из приложения?")
                .setPositiveButton("Да", (dialog, which) -> finish())
                .setNegativeButton("Нет", null)
                .show();
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