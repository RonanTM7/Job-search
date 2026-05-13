package com.example.job.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.job.R;
import com.example.job.model.ChatMeta;
import java.util.ArrayList;
import java.util.List;

public class AdminChatAdapter extends RecyclerView.Adapter<AdminChatAdapter.ChatViewHolder> {

    private List<ChatMeta> chats = new ArrayList<>();
    private final OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(ChatMeta chat);
    }

    public AdminChatAdapter(OnChatClickListener listener) {
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setChats(List<ChatMeta> chats) {
        this.chats = chats;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMeta chat = chats.get(position);
        holder.userName.setText(chat.getUserName());
        holder.lastMessage.setText(chat.getLastMessage());
        holder.itemView.setOnClickListener(v -> listener.onChatClick(chat));
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView userName, lastMessage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.chat_user_name);
            lastMessage = itemView.findViewById(R.id.last_message);
        }
    }
}
