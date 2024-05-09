package com.example.socialmedia.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmedia.R;
import com.example.socialmedia.model.MessageModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ChatHolder> {

    Context context;
    List<MessageModel> list;


    public MessageAdapter(Context context, List<MessageModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_items, parent, false);
        return new ChatHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatHolder holder, int position) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        assert user != null;

        boolean isMessageClicked = false;


        if(list.get(position).getSenderID().equalsIgnoreCase(user.getUid())){
            holder.leftChat.setVisibility(View.GONE);
            holder.rightChat.setVisibility(View.VISIBLE);
            holder.rightChat.setText(list.get(position).getMessage());

            Date date1= list.get(position).getTime();
            if (date1 != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH'h'mm',' dd',' MM',' yyyy");
                String formattedDate = sdf.format(date1);
                holder.rightChatTime.setText(formattedDate);
            } else {
                holder.rightChatTime.setText("Unknown");
            }

        }else{
            holder.rightChat.setVisibility(View.GONE);
            holder.leftChat.setVisibility(View.VISIBLE);
            holder.leftChat.setText(list.get(position).getMessage());
            Date date2= list.get(position).getTime();
            if (date2 != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH'h'mm',' dd',' MM',' yyyy");
                String formattedDate = sdf.format(date2);
                holder.leftChatTime.setText(formattedDate);
            } else {
                holder.leftChatTime.setText("Unknown");
            }
        }


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ChatHolder extends  RecyclerView.ViewHolder{

        TextView leftChat, rightChat, leftChatTime, rightChatTime;
        public ChatHolder(@NonNull View itemView) {
            super(itemView);

            leftChat = itemView.findViewById(R.id.leftChat);
            rightChat = itemView.findViewById(R.id.rightChat);
            leftChatTime= itemView.findViewById(R.id.leftChatTime);
            rightChatTime= itemView.findViewById(R.id.rightChatTime);

        }
    }

}
