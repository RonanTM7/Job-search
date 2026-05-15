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
import job.search.app.adapter.ApplicationAdapter;
import job.search.app.databinding.FragmentApplicationsBinding;
import job.search.app.model.Job;
import job.search.app.model.Vacancy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ApplicationsFragment extends Fragment implements ApplicationAdapter.OnApplicationActionListener {

    private FragmentApplicationsBinding binding;
    private ApplicationAdapter applicationAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private final List<ApplicationAdapter.ApplicationItem> applicationItems = new ArrayList<>();

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
        applicationAdapter = new ApplicationAdapter(applicationItems, this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(applicationAdapter);
    }

    private void loadAppliedJobs() {
        if (mAuth.getCurrentUser() == null) {
            binding.progressBar.setVisibility(View.GONE);
            return;
        }
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("applications").whereEqualTo("userId", userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                applicationItems.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String vacancyId = document.getString("vacancyId");
                    applicationItems.add(new ApplicationAdapter.ApplicationItem(document.getId(), vacancyId));
                }
                if (!applicationItems.isEmpty()) {
                    fetchJobsData();
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                    applicationAdapter.updateData(applicationItems);
                }
            } else {
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void fetchJobsData() {
        String userId = mAuth.getCurrentUser().getUid();
        for (ApplicationAdapter.ApplicationItem item : applicationItems) {
            db.collection("vacancies").document(item.vacancyId).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    item.vacancyTitle = doc.getString("title");
                    item.companyName = doc.getString("companyName");
                }

                String chatId = userId + "_" + item.vacancyId;
                db.collection("employer_chats").document(chatId).get().addOnSuccessListener(chatDoc -> {
                    if (chatDoc.exists()) {
                        Boolean employerReplied = chatDoc.getBoolean("employerReplied");
                        item.hasChat = employerReplied != null && employerReplied;
                    } else {
                        item.hasChat = false;
                    }
                    applicationAdapter.notifyDataSetChanged();
                });
            });
        }
        binding.progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onDelete(ApplicationAdapter.ApplicationItem item) {
        db.collection("applications").document(item.applicationId).delete().addOnSuccessListener(aVoid -> {
            applicationItems.remove(item);
            applicationAdapter.updateData(applicationItems);
        });
    }

    @Override
    public void onChat(ApplicationAdapter.ApplicationItem item) {
        String userId = mAuth.getCurrentUser().getUid();
        String chatId = userId + "_" + item.vacancyId;
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra("CHAT_ID", chatId);
        intent.putExtra("USER_NAME", item.companyName);
        intent.putExtra("IS_EMPLOYER_CHAT", true);
        startActivity(intent);
    }

    @Override
    public void onClick(ApplicationAdapter.ApplicationItem item) {
        if (item.vacancyTitle == null) return;

        db.collection("vacancies").document(item.vacancyId).get().addOnSuccessListener(document -> {
            if (document.exists()) {
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
                        vacancy.getCategory(),
                        vacancy.getWorkType(),
                        vacancy.getSchedule(),
                        vacancy.getEmployerId()
                );
                Intent intent = new Intent(getActivity(), JobDetailActivity.class);
                intent.putExtra("job", job);
                startActivity(intent);
                if (getActivity() != null) {
                    getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }
        });
    }
}