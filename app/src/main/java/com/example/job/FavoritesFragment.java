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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FavoritesFragment extends Fragment implements JobAdapter.OnFavoriteClickListener {
//dsda//
    private FragmentFavoritesBinding binding;
    private JobAdapter jobAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<Job> favoriteJobs = new ArrayList<>();
    private Set<String> favoriteJobIds = new HashSet<>();


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
        db.collection("vacancies").whereIn("id", jobIds).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                favoriteJobs.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Job job = document.toObject(Job.class);
                    job.setId(document.getId());
                    favoriteJobs.add(job);
                }
                jobAdapter.updateData(favoriteJobs);
            }
            binding.progressBar.setVisibility(View.GONE);
        });
    }

    @Override
    public void onFavoriteClick(Job job) {
        String userId = mAuth.getCurrentUser().getUid();
        String vacancyId = job.getId();
        String favoriteId = userId + "_" + vacancyId;

        if (favoriteJobIds.contains(vacancyId)) {
            db.collection("favorites").document(favoriteId).delete().addOnSuccessListener(aVoid -> {
                favoriteJobIds.remove(vacancyId);
                favoriteJobs = favoriteJobs.stream().filter(j -> !j.getId().equals(vacancyId)).collect(Collectors.toList());
                jobAdapter.updateData(favoriteJobs);
                jobAdapter.setFavoriteJobIds(favoriteJobIds);
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
