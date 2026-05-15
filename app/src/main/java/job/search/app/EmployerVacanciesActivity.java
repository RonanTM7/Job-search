package job.search.app;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import job.search.app.adapter.JobAdapter;
import job.search.app.model.Job;
import job.search.app.model.Vacancy;

public class EmployerVacanciesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private JobAdapter adapter;
    private List<Job> jobList = new ArrayList<>();
    private FirebaseFirestore db;
    private String employerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_employer_vacancies);

        db = FirebaseFirestore.getInstance();
        employerId = FirebaseAuth.getInstance().getUid();

        recyclerView = findViewById(R.id.recycler_vacancies);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // We reuse JobAdapter but we don't need favorite functionality here
        adapter = new JobAdapter(jobList, new HashSet<>(), this::onJobClick, null);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_add_vacancy).setOnClickListener(v -> {
            startActivity(new Intent(this, AddEditVacancyActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadVacancies();
    }

    private void loadVacancies() {
        db.collection("vacancies").whereEqualTo("employerId", employerId).get()
                .addOnCompleteListener(task -> {
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
                        adapter.updateData(jobList);
                    }
                });
    }

    private void onJobClick(Job job) {
        Intent intent = new Intent(this, AddEditVacancyActivity.class);
        intent.putExtra("job", job);
        startActivity(intent);
    }
}
