package job.search.app.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import job.search.app.R;
import job.search.app.model.Message;
import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> messages = new ArrayList<>();
    private final String currentUserId;

    public MessageAdapter(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setMessages(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.textMessage.setText(message.getText());

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.textMessage.getLayoutParams();

        // If the current user is the sender (either user or admin)
        boolean isMe = message.getSenderId().equals(currentUserId);

        if (isMe) {
            params.addRule(RelativeLayout.ALIGN_PARENT_END);
            params.addRule(RelativeLayout.ALIGN_PARENT_START, 0);
            holder.textMessage.setBackgroundResource(R.drawable.bg_message_user);
            holder.textMessage.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
        } else {
            params.addRule(RelativeLayout.ALIGN_PARENT_START);
            params.addRule(RelativeLayout.ALIGN_PARENT_END, 0);
            holder.textMessage.setBackgroundResource(R.drawable.bg_message_admin);
            holder.textMessage.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.black));
        }
        holder.textMessage.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.text_message);
        }
    }
}
