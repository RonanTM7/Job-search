package job.search.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import job.search.app.model.Job;
import job.search.app.utils.CustomToast;

public class AddEditVacancyActivity extends AppCompatActivity {

    private EditText etCompanyName, etVacancyTitle, etSalaryMin, etSalaryMax, etCity, etDescription, etRequirements, etWorkType, etJobFormat;
    private Button btnSave, btnDelete;
    private FirebaseFirestore db;
    private String employerId;
    private String vacancyId;
    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_add_edit_vacancy);

        db = FirebaseFirestore.getInstance();
        employerId = FirebaseAuth.getInstance().getUid();

        etCompanyName = findViewById(R.id.et_company_name);
        etVacancyTitle = findViewById(R.id.et_vacancy_title);
        etSalaryMin = findViewById(R.id.et_salary_min);
        etSalaryMax = findViewById(R.id.et_salary_max);
        etCity = findViewById(R.id.et_city);
        etDescription = findViewById(R.id.et_description);
        etRequirements = findViewById(R.id.et_requirements);
        etWorkType = findViewById(R.id.et_work_type);
        etJobFormat = findViewById(R.id.et_job_format);
        btnSave = findViewById(R.id.btn_save_vacancy);
        btnDelete = findViewById(R.id.btn_delete_vacancy);

        Job job = (Job) getIntent().getSerializableExtra("job");
        if (job != null) {
            isEditing = true;
            vacancyId = job.getId();
            fillData(job);
            btnDelete.setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.tv_title)).setText("Редактирование вакансии");
        }

        btnSave.setOnClickListener(v -> saveVacancy());
        btnDelete.setOnClickListener(v -> deleteVacancy());
    }

    private void fillData(Job job) {
        etCompanyName.setText(job.getCompany());
        etVacancyTitle.setText(job.getTitle());

        String salary = job.getSalary();
        if (salary != null && !salary.isEmpty() && salary.length() % 2 == 0) {
            int middle = salary.length() / 2;
            etSalaryMin.setText(salary.substring(0, middle));
            etSalaryMax.setText(salary.substring(middle));
        } else {
            etSalaryMin.setText(salary);
        }

        etCity.setText(job.getLocation());
        etDescription.setText(job.getDescription());
        etRequirements.setText(job.getRequirements());
        etWorkType.setText(job.getCategory());
        etJobFormat.setText(job.isRemote() ? "Удалённо" : "В офисе");
    }

    private void saveVacancy() {
        String company = etCompanyName.getText().toString().trim();
        String title = etVacancyTitle.getText().toString().trim();
        String salaryMin = etSalaryMin.getText().toString().trim();
        String salaryMax = etSalaryMax.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String requirements = etRequirements.getText().toString().trim();
        String workType = etWorkType.getText().toString().trim();
        String jobFormat = etJobFormat.getText().toString().trim();

        if (company.isEmpty() || title.isEmpty() || salaryMin.isEmpty() || salaryMax.isEmpty() || city.isEmpty()) {
            CustomToast.showToast(this, "Заполните основные поля", 4000);
            return;
        }

        String salaryCombined = salaryMin + salaryMax;

        Map<String, Object> vacancy = new HashMap<>();
        vacancy.put("companyName", company);
        vacancy.put("title", title);
        vacancy.put("salary", Long.parseLong(salaryCombined));
        vacancy.put("city", city);
        vacancy.put("description", description);
        vacancy.put("requirements", requirements);
        vacancy.put("workType", workType);
        vacancy.put("jobFormat", jobFormat);
        vacancy.put("employerId", employerId);

        if (isEditing) {
            db.collection("vacancies").document(vacancyId).set(vacancy)
                    .addOnSuccessListener(aVoid -> {
                        CustomToast.showToast(this, "Обновлено", 4000);
                        finish();
                    });
        } else {
            db.collection("vacancies").add(vacancy)
                    .addOnSuccessListener(documentReference -> {
                        CustomToast.showToast(this, "Вакансия создана", 4000);
                        finish();
                    });
        }
    }

    private void deleteVacancy() {
        db.collection("vacancies").document(vacancyId).delete()
                .addOnSuccessListener(aVoid -> {
                    // Note: In a real app we might want to mark applications as "vacancy deleted" here
                    // but according to requirements we'll handle it on the seeker side by checking vacancy existence.
                    CustomToast.showToast(this, "Вакансия удалена", 4000);
                    finish();
                });
    }
}
