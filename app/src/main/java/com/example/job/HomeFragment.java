package com.example.job;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.job.adapter.CategoryAdapter;
import com.example.job.adapter.JobAdapter;
import com.example.job.model.Job;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView jobsRecyclerView;
    private RecyclerView categoriesRecyclerView;
    private JobAdapter jobAdapter;
    private CategoryAdapter categoryAdapter;
    private List<Job> jobList = new ArrayList<>();
    private List<Job> filteredJobList = new ArrayList<>();
    private List<String> categories = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        jobsRecyclerView = view.findViewById(R.id.jobsRecyclerView);
        categoriesRecyclerView = view.findViewById(R.id.categoriesRecyclerView);

        setupRecyclerView();
        setupCategoryRecyclerView();
        loadMockData();
        setupCategories();
    }

    private void setupRecyclerView() {
        jobAdapter = new JobAdapter(filteredJobList, new JobAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Job job) {
                openJobDetails(job);
            }
        });

        jobsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        jobsRecyclerView.setAdapter(jobAdapter);
    }

    private void setupCategoryRecyclerView() {
        categoryAdapter = new CategoryAdapter(categories, new CategoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String category) {
                filterJobsByCategory(category);
            }
        });

        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
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

    private void openJobDetails(Job job) {
        Intent intent = new Intent(getActivity(), JobDetailActivity.class);
        intent.putExtra("job", job);
        startActivity(intent);
        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
