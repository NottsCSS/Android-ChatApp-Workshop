package com.app.chatapp.component;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;

import com.app.chatapp.R;
import com.app.chatapp.models.Chat;

public class ChatHolder extends RecyclerView.ViewHolder {

    private TextView messageText;
    private TextView timeText;

    public ChatHolder(View itemView) {
        super(itemView);
        messageText = itemView.findViewById(R.id.text_message_body);
        timeText = itemView.findViewById(R.id.text_message_time);
    }

    public void bind(Chat chat) {
        messageText.setText(chat.getMessage());
        timeText.setText(DateFormat.format("dd/MM/yyyy HH:mm",chat.getTimestampUTC()));
    }
}
