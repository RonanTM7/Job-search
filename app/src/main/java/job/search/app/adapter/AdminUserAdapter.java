package job.search.app.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import job.search.app.R;
import job.search.app.model.User;
import java.util.ArrayList;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {

    private List<User> users = new ArrayList<>();
    private final OnUserActionListener listener;

    public interface OnUserActionListener {
        void onResetPassword(User user);
        void onBlockToggle(User user);
        void onDelete(User user);
    }

    public AdminUserAdapter(OnUserActionListener listener) {
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setUsers(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_user, parent, false);
        return new UserViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.userName.setText(user.getUsername());
        holder.userEmail.setText(user.getEmail());
        holder.userPhone.setText(user.getPhone());

        String status = user.getStatus();
        if (status == null) status = "active";

        String statusText = holder.itemView.getContext().getString(R.string.user_status_prefix);
        if ("blocked".equals(status)) {
            holder.userStatus.setText(statusText + holder.itemView.getContext().getString(R.string.status_blocked));
            holder.userStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.primary_red));
            holder.btnBlock.setText("Разблок.");
        } else if ("deleted".equals(status)) {
            holder.userStatus.setText(statusText + holder.itemView.getContext().getString(R.string.status_deleted));
            holder.userStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.darker_gray));
            holder.btnBlock.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
            holder.btnResetPassword.setVisibility(View.GONE);
        } else {
            holder.userStatus.setText(statusText + holder.itemView.getContext().getString(R.string.status_active));
            holder.userStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.primary_green));
            holder.btnBlock.setText("Блок");
            holder.btnBlock.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnResetPassword.setVisibility(View.VISIBLE);
        }

        holder.btnResetPassword.setOnClickListener(v -> listener.onResetPassword(user));
        holder.btnBlock.setOnClickListener(v -> listener.onBlockToggle(user));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(user));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userEmail, userPhone, userStatus;
        Button btnResetPassword, btnBlock, btnDelete;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            userEmail = itemView.findViewById(R.id.user_email);
            userPhone = itemView.findViewById(R.id.user_phone);
            userStatus = itemView.findViewById(R.id.user_status);
            btnResetPassword = itemView.findViewById(R.id.btn_reset_password);
            btnBlock = itemView.findViewById(R.id.btn_block);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
