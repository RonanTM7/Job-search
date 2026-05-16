package job.search.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EmployerVacancyDetailActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ApplicantAdapter adapter;
    private List<Applicant> applicantList = new ArrayList<>();
    private FirebaseFirestore db;
    private String vacancyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_employer_vacancy_detail);

        db = FirebaseFirestore.getInstance();
        vacancyId = getIntent().getStringExtra("vacancyId");
        String vacancyTitle = getIntent().getStringExtra("vacancyTitle");

        ((TextView)findViewById(R.id.tv_vacancy_title)).setText(vacancyTitle);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recycler_applicants);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ApplicantAdapter();
        recyclerView.setAdapter(adapter);

        loadApplicants();
    }

    private void loadApplicants() {
        db.collection("applications").whereEqualTo("vacancyId", vacancyId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        applicantList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String userId = document.getString("userId");
                            long timestamp = document.getLong("timestamp") != null ? document.getLong("timestamp") : 0;
                            applicantList.add(new Applicant(userId, timestamp));
                        }
                        adapter.notifyDataSetChanged();
                        loadApplicantNames();
                    }
                });
    }

    private void loadApplicantNames() {
        for (Applicant applicant : applicantList) {
            db.collection("seekers").document(applicant.userId).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    applicant.name = doc.getString("username");
                    adapter.notifyDataSetChanged();
                } else {
                    applicant.name = "Анонимный пользователь";
                    adapter.notifyDataSetChanged();
                }
            }).addOnFailureListener(e -> {
                applicant.name = "Ошибка загрузки";
                adapter.notifyDataSetChanged();
            });
        }
    }

    private static class Applicant {
        String userId;
        long timestamp;
        String name = "Загрузка...";

        Applicant(String userId, long timestamp) {
            this.userId = userId;
            this.timestamp = timestamp;
        }
    }

    private class ApplicantAdapter extends RecyclerView.Adapter<ApplicantAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_applicant, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Applicant applicant = applicantList.get(position);
            holder.tvName.setText(applicant.name);

            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
            holder.tvDate.setText(sdf.format(new Date(applicant.timestamp)));

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(EmployerVacancyDetailActivity.this, ApplicantResumeActivity.class);
                intent.putExtra("userId", applicant.userId);
                intent.putExtra("userName", applicant.name);
                intent.putExtra("vacancyId", vacancyId);
                startActivity(intent);
            });

            holder.btnChat.setOnClickListener(v -> {
                String chatId = applicant.userId + "_" + vacancyId;
                Intent intent = new Intent(EmployerVacancyDetailActivity.this, ChatActivity.class);
                intent.putExtra("CHAT_ID", chatId);
                intent.putExtra("USER_NAME", applicant.name);
                intent.putExtra("IS_EMPLOYER_CHAT", true);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return applicantList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvDate;
            View btnChat;
            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_applicant_name);
                tvDate = itemView.findViewById(R.id.tv_applied_date);
                btnChat = itemView.findViewById(R.id.btn_item_chat);
            }
        }
    }
}
