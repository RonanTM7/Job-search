package com.example.job;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.job.databinding.ActivityJobDetailBinding;
import com.example.job.model.Job;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class JobDetailActivity extends AppCompatActivity {
    private ActivityJobDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);

        binding = ActivityJobDetailBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        // УБРАТЬ эту строку - тулбара больше нет
        // setSupportActionBar(binding.toolbar);

        // Получаем данные о вакансии из Intent
        Job job = (Job) getIntent().getSerializableExtra("job");

        if (job != null) {
            displayJobDetails(job);
        }

        // Обработчик кнопки отклика
        binding.applyButton.setOnClickListener(v -> {
            Toast.makeText(this, "Отклик отправлен на вакансию: " + job.getTitle(), Toast.LENGTH_SHORT).show();
        });
    }

    private void displayJobDetails(Job job) {
        // Заполняем данные о вакансии
        binding.jobTitle.setText(job.getTitle());
        binding.salaryText.setText(job.getSalary());
        binding.companyName.setText(job.getCompany());
        binding.locationText.setText(job.getLocation());
        binding.descriptionText.setText(job.getDescription());
        binding.requirementsText.setText(job.getRequirements());

        // Устанавливаем иконку компании (если есть метод getLogo())
        // binding.companyLogo.setImageResource(job.getLogo()); // ЗАКОММЕНТИРОВАТЬ - этого поля нет

        // Показываем/скрываем бейдж "Удаленно"
        if (job.isRemote()) {
            binding.remoteBadge.setVisibility(android.view.View.VISIBLE);
        } else {
            binding.remoteBadge.setVisibility(android.view.View.GONE);
        }
    }

}