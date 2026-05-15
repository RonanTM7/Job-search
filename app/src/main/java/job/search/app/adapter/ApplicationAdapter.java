package job.search.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import job.search.app.R;

public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ViewHolder> {

    private List<ApplicationItem> items;
    private OnApplicationActionListener listener;

    public interface OnApplicationActionListener {
        void onDelete(ApplicationItem item);
        void onChat(ApplicationItem item);
        void onClick(ApplicationItem item);
    }

    public ApplicationAdapter(List<ApplicationItem> items, OnApplicationActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_application, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ApplicationItem item = items.get(position);
        holder.tvTitle.setText(item.vacancyTitle != null ? item.vacancyTitle : "Вакансия удалена");
        holder.tvCompany.setText(item.companyName != null ? item.companyName : "");

        if (item.vacancyTitle == null) {
            holder.tvStatus.setText("Вакансия удалена");
            holder.tvStatus.setTextColor(0xFFFF4444); // Red
        } else {
            holder.tvStatus.setText("Статус: Отправлено");
            holder.tvStatus.setTextColor(0xFF007AFF); // Blue
        }

        holder.btnChat.setVisibility(item.hasChat ? View.VISIBLE : View.GONE);

        holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));
        holder.btnChat.setOnClickListener(v -> listener.onChat(item));
        holder.itemView.setOnClickListener(v -> listener.onClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateData(List<ApplicationItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCompany, tvStatus;
        ImageButton btnDelete;
        Button btnChat;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_job_title);
            tvCompany = itemView.findViewById(R.id.tv_company_name);
            tvStatus = itemView.findViewById(R.id.tv_status);
            btnDelete = itemView.findViewById(R.id.btn_delete_app);
            btnChat = itemView.findViewById(R.id.btn_chat_with_employer);
        }
    }

    public static class ApplicationItem {
        public String applicationId;
        public String vacancyId;
        public String vacancyTitle;
        public String companyName;
        public boolean hasChat;

        public ApplicationItem(String applicationId, String vacancyId) {
            this.applicationId = applicationId;
            this.vacancyId = vacancyId;
        }
    }
}
