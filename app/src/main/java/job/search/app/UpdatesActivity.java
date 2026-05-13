package job.search.app;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import job.search.app.adapter.UpdatesAdapter;
import job.search.app.model.Update;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class UpdatesActivity extends AppCompatActivity {

    private RecyclerView updatesRecyclerView;
    private UpdatesAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvNoUpdates;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_updates);

        db = FirebaseFirestore.getInstance();

        ImageButton btnBack = findViewById(R.id.btn_back);
        updatesRecyclerView = findViewById(R.id.updatesRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvNoUpdates = findViewById(R.id.tvNoUpdates);

        btnBack.setOnClickListener(v -> finish());

        setupRecyclerView();
        loadUpdates();
    }

    private void setupRecyclerView() {
        adapter = new UpdatesAdapter();
        updatesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        updatesRecyclerView.setAdapter(adapter);
    }

    private void loadUpdates() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("updates")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        List<Update> updates = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Update update = document.toObject(Update.class);
                            updates.add(update);
                        }

                        if (updates.isEmpty()) {
                            tvNoUpdates.setVisibility(View.VISIBLE);
                        } else {
                            adapter.setUpdates(updates);
                        }
                    } else {
                        tvNoUpdates.setVisibility(View.VISIBLE);
                        tvNoUpdates.setText("Ошибка загрузки данных");
                    }
                });
    }
}
