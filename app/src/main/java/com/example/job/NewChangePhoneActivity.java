package com.example.job;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.job.utils.CustomToast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class NewChangePhoneActivity extends AppCompatActivity {

    private EditText editTextPhone;
    private Button btnChangePhone;
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
        btnChangePhone = findViewById(R.id.btn_change_phone);
        textError = findViewById(R.id.text_error);
        ImageButton backButton = findViewById(R.id.btn_back);

        backButton.setOnClickListener(v -> onBackPressed());

        editTextPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textError.setVisibility(TextView.GONE);
                if (!s.toString().startsWith("+7")) {
                    editTextPhone.setText("+7");
                    editTextPhone.setSelection(editTextPhone.getText().length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String phoneNumber = s.toString().replaceAll("\\\\D", "");
                if (phoneNumber.length() > 1) {
                    phoneNumber = phoneNumber.substring(1);
                    StringBuilder formatted = new StringBuilder("+7 ");
                    formatted.append(phoneNumber.substring(0, Math.min(3, phoneNumber.length())));
                    if (phoneNumber.length() >= 4) {
                        formatted.append(" ").append(phoneNumber.substring(3, Math.min(6, phoneNumber.length())));
                    }
                    if (phoneNumber.length() >= 7) {
                        formatted.append(" ").append(phoneNumber.substring(6, Math.min(8, phoneNumber.length())));
                    }
                    if (phoneNumber.length() >= 9) {
                        formatted.append(" ").append(phoneNumber.substring(8, Math.min(10, phoneNumber.length())));
                    }

                    editTextPhone.removeTextChangedListener(this);
                    editTextPhone.setText(formatted.toString());
                    editTextPhone.setSelection(formatted.length());
                    editTextPhone.addTextChangedListener(this);
                }
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

        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String currentPhone = documentSnapshot.getString("phone");
                        if (newPhone.equals(currentPhone)) {
                            textError.setText("Номер должен отличаться от используемого сейчас");
                            textError.setVisibility(TextView.VISIBLE);
                            return;
                        }

                        db.collection("users").document(currentUser.getUid())
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
