package job.search.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class RoleSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_role_selection);

        Button btnSeeker = findViewById(R.id.btn_seeker);
        Button btnEmployer = findViewById(R.id.btn_employer);
        TextView tvBackToLogin = findViewById(R.id.tv_back_to_login);

        btnSeeker.setOnClickListener(v -> startRegistration("seeker"));
        btnEmployer.setOnClickListener(v -> startRegistration("employer"));
        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void startRegistration(String role) {
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.putExtra("ROLE", role);
        startActivity(intent);
    }
}
