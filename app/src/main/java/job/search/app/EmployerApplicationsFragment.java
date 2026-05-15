package job.search.app;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import job.search.app.adapter.JobAdapter;
import job.search.app.model.Job;
import job.search.app.model.Vacancy;

public class EmployerApplicationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private JobAdapter adapter;
    private List<Job> jobList = new ArrayList<>();
    private FirebaseFirestore db;
    private String employerId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_employer_applications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        employerId = FirebaseAuth.getInstance().getUid();

        recyclerView = view.findViewById(R.id.recycler_vacancies_with_apps);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new JobAdapter(jobList, new HashSet<>(), this::onJobClick, null);
        recyclerView.setAdapter(adapter);

        loadVacancies();
    }

    private void loadVacancies() {
        if (employerId == null) return;
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
        Intent intent = new Intent(getActivity(), EmployerVacancyDetailActivity.class);
        intent.putExtra("vacancyId", job.getId());
        intent.putExtra("vacancyTitle", job.getTitle());
        startActivity(intent);
    }
}
