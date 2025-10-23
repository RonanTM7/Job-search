package com.example.job;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.job.databinding.ActivityJobDetailBinding;
import com.example.job.model.Job;

public class JobDetailActivity extends AppCompatActivity {
    private ActivityJobDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        binding = ActivityJobDetailBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        // Получаем данные о вакансии из Intent
        Job job = (Job) getIntent().getSerializableExtra("job");

        if (job != null) {
            displayJobDetails(job);
        }

        // Обработчик кнопки отклика
        binding.applyButton.setOnClickListener(v -> {
            assert job != null;
            Toast.makeText(this, "Ваше резюме было отправлено", Toast.LENGTH_SHORT).show();
        });

        binding.backButton.setOnClickListener(v -> finish());
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