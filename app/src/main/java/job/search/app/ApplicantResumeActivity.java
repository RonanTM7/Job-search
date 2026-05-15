package job.search.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import job.search.app.utils.CustomToast;

public class ApplicantResumeActivity extends AppCompatActivity {

    private TextView tvResumeContent;
    private FirebaseFirestore db;
    private String seekerId, vacancyId, userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_applicant_resume);

        db = FirebaseFirestore.getInstance();
        seekerId = getIntent().getStringExtra("userId");
        userName = getIntent().getStringExtra("userName");
        vacancyId = getIntent().getStringExtra("vacancyId");

        ((TextView)findViewById(R.id.tv_applicant_name_header)).setText(userName);
        tvResumeContent = findViewById(R.id.tv_resume_content);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_write_message).setOnClickListener(v -> openChat());

        loadResume();
    }

    private void loadResume() {
        db.collection("resumes").document(seekerId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                tvResumeContent.setText(doc.getString("content"));
            } else {
                tvResumeContent.setText("Резюме не найдено");
            }
        }).addOnFailureListener(e -> {
            tvResumeContent.setText("Ошибка загрузки резюме");
        });
    }

    private void openChat() {
        String chatId = seekerId + "_" + vacancyId;
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("CHAT_ID", chatId);
        intent.putExtra("USER_NAME", userName);
        intent.putExtra("IS_EMPLOYER_CHAT", true);
        startActivity(intent);
    }
}
