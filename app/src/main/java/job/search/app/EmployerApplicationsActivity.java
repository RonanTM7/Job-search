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

public class EmployerApplicationsActivity extends AppCompatActivity {

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

        setContentView(R.layout.activity_employer_applications);

        db = FirebaseFirestore.getInstance();
        employerId = FirebaseAuth.getInstance().getUid();

        recyclerView = findViewById(R.id.recycler_vacancies_with_apps);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new JobAdapter(jobList, new HashSet<>(), this::onJobClick, null);
        recyclerView.setAdapter(adapter);

        loadVacancies();
    }

    private void loadVacancies() {
        db.collection("applications").whereEqualTo("employerId", employerId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        HashSet<String> vacancyIds = new HashSet<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            vacancyIds.add(document.getString("vacancyId"));
                        }
                        if (vacancyIds.isEmpty()) {
                            jobList.clear();
                            adapter.updateData(jobList);
                        } else {
                            fetchVacanciesByIds(new ArrayList<>(vacancyIds));
                        }
                    }
                });
    }

    private void fetchVacanciesByIds(List<String> ids) {
        db.collection("vacancies").whereIn(com.google.firebase.firestore.FieldPath.documentId(), ids).get()
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
                                    vacancy.getCategory(),
                                    vacancy.getWorkType(),
                                    vacancy.getSchedule(),
                                    vacancy.getEmployerId()
                            ));
                        }
                        adapter.updateData(jobList);
                    }
                });
    }

    private void onJobClick(Job job) {
        Intent intent = new Intent(this, EmployerVacancyDetailActivity.class);
        intent.putExtra("vacancyId", job.getId());
        intent.putExtra("vacancyTitle", job.getTitle());
        startActivity(intent);
    }
}
