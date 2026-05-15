package job.search.app;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import job.search.app.utils.CustomToast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class NewChangePhoneActivity extends AppCompatActivity {

    private EditText editTextPhone;
    private TextView textError;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_change_phone);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        editTextPhone = findViewById(R.id.edit_text_phone);
        Button btnChangePhone = findViewById(R.id.btn_change_phone);
        textError = findViewById(R.id.text_error);
        ImageButton backButton = findViewById(R.id.btn_back);

        backButton.setOnClickListener(v -> onBackPressed());

        editTextPhone.addTextChangedListener(new TextWatcher() {
            private boolean formatting;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textError.setVisibility(TextView.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (formatting) return;

                formatting = true;

                String digits = s.toString().replaceAll("\\D", "");

                if (!digits.startsWith("7")) {
                    if (digits.startsWith("8")) {
                        digits = "7" + digits.substring(1);
                    } else {
                        digits = "7" + digits;
                    }
                }

                if (digits.length() > 11) {
                    digits = digits.substring(0, 11);
                }

                StringBuilder formatted = new StringBuilder("+7");
                if (digits.length() > 1) {
                    formatted.append(" ").append(digits.substring(1, Math.min(4, digits.length())));
                }
                if (digits.length() > 4) {
                    formatted.append(" ").append(digits.substring(4, Math.min(7, digits.length())));
                }
                if (digits.length() > 7) {
                    formatted.append(" ").append(digits.substring(7, Math.min(9, digits.length())));
                }
                if (digits.length() > 9) {
                    formatted.append(" ").append(digits.substring(9));
                }

                s.replace(0, s.length(), formatted.toString());
                editTextPhone.setSelection(s.length());

                formatting = false;
            }
        });

        btnChangePhone.setOnClickListener(v -> changePhone());
    }

    private void changePhone() {
        String newPhone = editTextPhone.getText().toString();

        if (newPhone.isEmpty()) {
            textError.setText("Поле не может быть пустым");
            textError.setVisibility(TextView.VISIBLE);
            return;
        }

        if (newPhone.length() != 16) {
            textError.setText("Введите корректный номер телефона");
            textError.setVisibility(TextView.VISIBLE);
            return;
        }

        String role = getSharedPreferences("AppSettings", MODE_PRIVATE).getString("userRole", "seeker");
        String collection = "employer".equals(role) ? "employers" : "seekers";
        db.collection(collection).document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String currentPhone = documentSnapshot.getString("phone");
                        if (newPhone.equals(currentPhone)) {
                            textError.setText("Номер должен отличаться от используемого сейчас");
                            textError.setVisibility(TextView.VISIBLE);
                            return;
                        }

                        db.collection(collection).document(currentUser.getUid())
                                .update("phone", newPhone)
                                .addOnSuccessListener(aVoid -> {
                                    CustomToast.showToast(this, "Номер успешно изменен", Toast.LENGTH_SHORT);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    textError.setText("Не удалось обновить номер телефона.");
                                    textError.setVisibility(TextView.VISIBLE);
                                });
                    }
                });
    }
}
