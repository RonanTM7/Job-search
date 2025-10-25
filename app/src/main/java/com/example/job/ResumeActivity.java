package com.example.job;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class ResumeActivity extends AppCompatActivity {

    private EditText resumeEditText;
    private Button saveResumeButton;
    private FirebaseUser currentUser;
    private DocumentReference resumeRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resume);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        resumeEditText = findViewById(R.id.et_resume);
        saveResumeButton = findViewById(R.id.btn_save_resume);
        ImageButton backButton = findViewById(R.id.btn_back);

        backButton.setOnClickListener(v -> onBackPressed());

        if (currentUser != null) {
            resumeRef = db.collection("resumes").document(currentUser.getUid());
            loadResume();
        }

        saveResumeButton.setOnClickListener(v -> saveResume());

        resumeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                saveResumeButton.setText("Сохранить");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadResume() {
        resumeRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String resumeText = documentSnapshot.getString("content");
                resumeEditText.setText(resumeText);
                saveResumeButton.setText("Редактировать");
            } else {
                saveResumeButton.setText("Сохранить");
            }
        });
    }

    private void saveResume() {
        String resumeText = resumeEditText.getText().toString().trim();
        if (resumeText.isEmpty()) {
            Toast.makeText(this, "Поле должно быть заполнено", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> resumeData = new HashMap<>();
        resumeData.put("content", resumeText);
        resumeData.put("userId", currentUser.getUid());

        resumeRef.set(resumeData).addOnSuccessListener(aVoid -> {
            Toast.makeText(ResumeActivity.this, "Сохранено", Toast.LENGTH_SHORT).show();
            saveResumeButton.setText("Редактировать");
        }).addOnFailureListener(e -> {
            Toast.makeText(ResumeActivity.this, "Ошибка сохранения", Toast.LENGTH_SHORT).show();
        });
    }
}
