package com.app.chatapp.component;

import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.support.v7.widget.RecyclerView;
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

        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        sdf.setTimeZone(tz);
        timeText.setText(sdf.format(chat.getTimestampUTC()));
    }
}
