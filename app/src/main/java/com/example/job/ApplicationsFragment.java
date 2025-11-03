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
import com.example.job.databinding.FragmentApplicationsBinding;
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

public class ApplicationsFragment extends Fragment {

    private FragmentApplicationsBinding binding;
    private JobAdapter jobAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private final List<Job> appliedJobs = new ArrayList<>();
    private final Set<String> favoriteJobIds = new HashSet<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentApplicationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        setupRecyclerView();
        loadAppliedJobs();
    }

    private void setupRecyclerView() {
        jobAdapter = new JobAdapter(appliedJobs, favoriteJobIds, this::openJobDetails, null);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(jobAdapter);
    }

    private void loadAppliedJobs() {
        if (mAuth.getCurrentUser() == null) {
            binding.progressBar.setVisibility(View.GONE);
            return;
        }
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("favorites").whereEqualTo("userId", userId).get().addOnCompleteListener(favoriteTask -> {
            if (favoriteTask.isSuccessful()) {
                favoriteJobIds.clear();
                for (QueryDocumentSnapshot document : favoriteTask.getResult()) {
                    favoriteJobIds.add(document.getString("vacancyId"));
                }
            }

            db.collection("applications").whereEqualTo("userId", userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Set<String> appliedIds = new HashSet<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        appliedIds.add(document.getString("vacancyId"));
                    }
                    if (!appliedIds.isEmpty()) {
                        fetchJobsByIds(new ArrayList<>(appliedIds));
                    } else {
                        binding.progressBar.setVisibility(View.GONE);
                        appliedJobs.clear();
                        jobAdapter.updateData(appliedJobs);
                    }
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                }
            });
        });
    }

    private void fetchJobsByIds(List<String> jobIds) {
        db.collection("vacancies").whereIn(FieldPath.documentId(), jobIds).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                appliedJobs.clear();
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
                    appliedJobs.add(job);
                }
                jobAdapter.updateData(appliedJobs);
                jobAdapter.updateFavorites(favoriteJobIds);
            }
            binding.progressBar.setVisibility(View.GONE);
        });
    }

    private void openJobDetails(Job job) {
        Intent intent = new Intent(getActivity(), JobDetailActivity.class);
        intent.putExtra("job", job);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }
}