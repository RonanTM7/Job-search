package com.example.job;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.job.databinding.ActivityJobDetailBinding;
import com.example.job.model.Job;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class JobDetailActivity extends AppCompatActivity {
    private ActivityJobDetailBinding binding;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        binding = ActivityJobDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        Job job = (Job) getIntent().getSerializableExtra("job");
        if (job != null) {
            displayJobDetails(job);
            checkIfApplied(job);
        }

        binding.applyButton.setOnClickListener(v -> {
            if (job != null && currentUser != null) {
                applyToJob(job);
            }
        });

        binding.backButton.setOnClickListener(v -> finish());
    }

    private void checkIfApplied(Job job) {
        if (currentUser != null) {
            String applicationId = currentUser.getUid() + "_" + job.getId();
            db.collection("applications").document(applicationId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    updateApplyButton(true);
                }
            });
        }
    }

    private void applyToJob(Job job) {
        String applicationId = currentUser.getUid() + "_" + job.getId();
        Map<String, Object> application = new HashMap<>();
        application.put("userId", currentUser.getUid());
        application.put("vacancyId", job.getId());
        application.put("timestamp", System.currentTimeMillis());

        db.collection("applications").document(applicationId).set(application)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Ваше резюме было отправлено", Toast.LENGTH_SHORT).show();
                    updateApplyButton(true);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateApplyButton(boolean applied) {
        if (applied) {
            binding.applyButton.setText("Резюме отправлено");
            binding.applyButton.setBackgroundColor(ContextCompat.getColor(this, R.color.ggg));
            binding.applyButton.setEnabled(false);
        }
    }

    private void displayJobDetails(Job job) {
        // Заполняем данные о вакансии
        binding.jobTitle.setText(job.getTitle());
        binding.salaryText.setText(job.getSalary());
        binding.companyName.setText(job.getCompany());
        binding.locationText.setText(job.getLocation());
        binding.descriptionText.setText(job.getDescription());
        binding.requirementsText.setText(job.getRequirements());

        // Показываем/скрываем бейдж "Удаленно"
        if (job.isRemote()) {
            binding.remoteBadge.setVisibility(android.view.View.VISIBLE);
        } else {
            binding.remoteBadge.setVisibility(android.view.View.GONE);
        }
    }

}