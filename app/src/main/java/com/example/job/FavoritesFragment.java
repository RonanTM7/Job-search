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
import com.example.job.adapter.JobAdapter;
import com.example.job.databinding.FragmentFavoritesBinding;
import com.example.job.model.Job;
import com.example.job.model.Vacancy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavoritesFragment extends Fragment implements JobAdapter.OnFavoriteClickListener {
    private FragmentFavoritesBinding binding;
    private JobAdapter jobAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private final List<Job> favoriteJobs = new ArrayList<>();
    private final Set<String> favoriteJobIds = new HashSet<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        setupRecyclerView();
        loadFavoriteJobs();
    }

    private void setupRecyclerView() {
        jobAdapter = new JobAdapter(favoriteJobs, favoriteJobIds, this::openJobDetails, this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(jobAdapter);
    }

    private void loadFavoriteJobs() {
        assert mAuth.getCurrentUser() != null;
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("favorites").whereEqualTo("userId", userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                favoriteJobIds.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    favoriteJobIds.add(document.getString("vacancyId"));
                }
                if (!favoriteJobIds.isEmpty()) {
                    fetchJobsByIds(new ArrayList<>(favoriteJobIds));
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                    favoriteJobs.clear();
                    jobAdapter.updateData(favoriteJobs);
                }
            }
        });
    }

    private void fetchJobsByIds(List<String> jobIds) {
        db.collection("vacancies").whereIn(FieldPath.documentId(), jobIds).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                favoriteJobs.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Vacancy vacancy = document.toObject(Vacancy.class);
                    Job job = new Job(
                            document.getId(),
                            vacancy.getTitle(),
                            vacancy.getCompanyName(),
                            String.valueOf(vacancy.getSalary()),
                            vacancy.getCity(),
                            vacancy.getDescription(),
                            vacancy.getRequirements(),
                            "Удалённо".equals(vacancy.getJobFormat()),
                            vacancy.getWorkType()
                    );
                    favoriteJobs.add(job);
                }
                jobAdapter.updateData(favoriteJobs);
            }
            binding.progressBar.setVisibility(View.GONE);
        });
    }

    @Override
    public void onFavoriteClick(Job job) {
        assert mAuth.getCurrentUser() != null;
        String userId = mAuth.getCurrentUser().getUid();
        String vacancyId = job.getId();
        String favoriteId = userId + "_" + vacancyId;

        if (favoriteJobIds.contains(vacancyId)) {
            db.collection("favorites").document(favoriteId).delete().addOnSuccessListener(aVoid -> {
                favoriteJobIds.remove(vacancyId);
                favoriteJobs.removeIf(j -> j.getId().equals(vacancyId));
                jobAdapter.updateData(favoriteJobs);
                jobAdapter.updateFavorites(favoriteJobIds);
            });
        }
    }

    private void openJobDetails(Job job) {
        Intent intent = new Intent(getActivity(), JobDetailActivity.class);
        intent.putExtra("job", job);
        startActivity(intent);
        requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}