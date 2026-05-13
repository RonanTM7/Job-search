package com.example.job.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.job.R;
import com.example.job.model.Update;
import java.util.ArrayList;
import java.util.List;

public class UpdatesAdapter extends RecyclerView.Adapter<UpdatesAdapter.UpdateViewHolder> {

    private List<Update> updateList = new ArrayList<>();

    @SuppressLint("NotifyDataSetChanged")
    public void setUpdates(List<Update> updates) {
        this.updateList = updates;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UpdateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_update, parent, false);
        return new UpdateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UpdateViewHolder holder, int position) {
        Update update = updateList.get(position);
        holder.bind(update);
    }

    @Override
    public int getItemCount() {
        return updateList.size();
    }

    static class UpdateViewHolder extends RecyclerView.ViewHolder {
        TextView tvVersion, tvDate, tvDescription;

        public UpdateViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVersion = itemView.findViewById(R.id.tvVersion);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }

        public void bind(Update update) {
            tvVersion.setText(update.getVersion());
            tvDate.setText(update.getDate());
            tvDescription.setText(update.getDescription());
        }
    }
}
