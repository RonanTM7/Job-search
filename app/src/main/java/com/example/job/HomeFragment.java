package com.example.job;

import android.annotation.SuppressLint;
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
import com.example.job.model.Vacancy;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView jobsRecyclerView;
    private RecyclerView categoriesRecyclerView;
    private JobAdapter jobAdapter;
    private final List<Job> jobList = new ArrayList<>();
    private final List<Job> filteredJobList = new ArrayList<>();
    private final List<String> categories = new ArrayList<>();
    private FirebaseFirestore db;

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
        db = FirebaseFirestore.getInstance();

        setupRecyclerView();
        setupCategoryRecyclerView();
        // Этот метод теперь просто загружает вакансии из Firestore
        loadDataFromFirestore();
    }

    /**
     * Загружает данные о вакансиях из Firestore.
     */
    private void loadDataFromFirestore() {
        loadVacancies();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadVacancies() {
        db.collection("vacancies").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                jobList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Vacancy vacancy = document.toObject(Vacancy.class);
                    // Конвертируем новую модель Vacancy в старую Job для адаптера
                    // Это временное решение, пока адаптер не будет обновлен
                    jobList.add(new Job(
                            document.getId(), // Здесь можно будет использовать ID документа из Firestore
                            vacancy.getTitle(),
                            vacancy.getCompanyName(),
                            String.valueOf(vacancy.getSalary()),
                            vacancy.getCity(),
                            vacancy.getDescription(),
                            vacancy.getRequirements(),
                            "Удалённо".equals(vacancy.getJobFormat()), // Определяем, удаленная ли работа
                            vacancy.getWorkType()
                    ));
                }
                filteredJobList.clear();
                filteredJobList.addAll(jobList);
                jobAdapter.notifyDataSetChanged();
                // Обновляем категории после загрузки вакансий
                setupCategories();
            }
        });
    }

    private void setupRecyclerView() {
        jobAdapter = new JobAdapter(filteredJobList, this::openJobDetails);
        jobsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        jobsRecyclerView.setAdapter(jobAdapter);
    }

    private void setupCategoryRecyclerView() {
        CategoryAdapter categoryAdapter = new CategoryAdapter(categories, this::filterJobsByCategory);
        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        categoriesRecyclerView.setAdapter(categoryAdapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterJobsByCategory(String category) {
        filteredJobList.clear();
        if ("Все".equals(category)) {
            filteredJobList.addAll(jobList);
        } else {
            for (Job job : jobList) {
                if (job.getCategory() != null && job.getCategory().equals(category)) {
                    filteredJobList.add(job);
                }
            }
        }
        jobAdapter.notifyDataSetChanged();
    }

    private void setupCategories() {
        categories.clear(); // Очищаем старые категории
        categories.add("Все");
        for (Job job : jobList) {
            if (!categories.contains(job.getCategory())) {
                categories.add(job.getCategory());
            }
        }
        // Обновляем адаптер категорий
        if (categoriesRecyclerView.getAdapter() != null) {
            categoriesRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    private void openJobDetails(Job job) {
        Intent intent = new Intent(getActivity(), JobDetailActivity.class);
        intent.putExtra("job", job);
        startActivity(intent);
        requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}