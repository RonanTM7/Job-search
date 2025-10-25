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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HomeFragment extends Fragment implements JobAdapter.OnFavoriteClickListener {

    private RecyclerView jobsRecyclerView;
    private RecyclerView categoriesRecyclerView;
    private JobAdapter jobAdapter;
    private final List<Job> jobList = new ArrayList<>();
    private final List<Job> filteredJobList = new ArrayList<>();
    private final List<String> categories = new ArrayList<>();
    private final Set<String> favoriteJobIds = new HashSet<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

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
        mAuth = FirebaseAuth.getInstance();

        setupRecyclerView();
        setupCategoryRecyclerView();
        loadFavoriteJobIds();
    }

    private void loadFavoriteJobIds() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("favorites").whereEqualTo("userId", userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                favoriteJobIds.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    favoriteJobIds.add(document.getString("vacancyId"));
                }
                jobAdapter.updateFavorites(favoriteJobIds);
            }
            loadDataFromFirestore();
        });
    }

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
                    jobList.add(new Job(
                            document.getId(),
                            vacancy.getTitle(),
                            vacancy.getCompanyName(),
                            String.valueOf(vacancy.getSalary()),
                            vacancy.getCity(),
                            vacancy.getDescription(),
                            vacancy.getRequirements(),
                            "Удалённо".equals(vacancy.getJobFormat()),
                            vacancy.getWorkType()
                    ));
                }
                filteredJobList.clear();
                filteredJobList.addAll(jobList);
                jobAdapter.updateData(filteredJobList);
                setupCategories();
            }
        });
    }

    private void setupRecyclerView() {
        jobAdapter = new JobAdapter(filteredJobList, favoriteJobIds, this::openJobDetails, this);
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
        categories.clear();
        categories.add("Все");
        for (Job job : jobList) {
            if (!categories.contains(job.getCategory())) {
                categories.add(job.getCategory());
            }
        }
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

    @Override
    public void onFavoriteClick(Job job) {
        String userId = mAuth.getCurrentUser().getUid();
        String vacancyId = job.getId();
        String favoriteId = userId + "_" + vacancyId;

        if (favoriteJobIds.contains(vacancyId)) {
            db.collection("favorites").document(favoriteId).delete().addOnSuccessListener(aVoid -> {
                favoriteJobIds.remove(vacancyId);
                jobAdapter.updateFavorites(favoriteJobIds);
            });
        } else {
            Map<String, Object> favorite = new HashMap<>();
            favorite.put("userId", userId);
            favorite.put("vacancyId", vacancyId);
            db.collection("favorites").document(favoriteId).set(favorite).addOnSuccessListener(aVoid -> {
                favoriteJobIds.add(vacancyId);
                jobAdapter.updateFavorites(favoriteJobIds);
            });
        }
    }
}
