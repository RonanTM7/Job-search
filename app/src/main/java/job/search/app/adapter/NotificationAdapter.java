package job.search.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import job.search.app.R;
import job.search.app.model.Notification;
import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Notification> notifications = new ArrayList<>();
    private final OnNotificationClickListener clickListener;
    private final OnDeleteClickListener deleteClickListener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Notification notification);
    }

    public NotificationAdapter(OnNotificationClickListener clickListener, OnDeleteClickListener deleteClickListener) {
        this.clickListener = clickListener;
        this.deleteClickListener = deleteClickListener;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.tvTitle.setText(notification.getTitle());
        holder.tvMessage.setText(notification.getMessage());

        holder.itemView.setOnClickListener(v -> clickListener.onNotificationClick(notification));
        holder.btnDelete.setOnClickListener(v -> deleteClickListener.onDeleteClick(notification));
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_notification_title);
            tvMessage = itemView.findViewById(R.id.tv_notification_message);
            btnDelete = itemView.findViewById(R.id.btn_delete_notification);
        }
    }
}
