package com.app.chatapp.component;

import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.app.chatapp.ChatActivity;
import com.app.chatapp.MainActivity;
import com.app.chatapp.R;
import com.app.chatapp.models.Contact;

import static com.app.chatapp.ChatActivity.CHAT_UID;
import static com.app.chatapp.ChatActivity.CONTACT_NAME;
import static com.app.chatapp.ChatActivity.CONTACT_UID;

public class ContactHolder extends RecyclerView.ViewHolder {

    private ConstraintLayout contactLayout;
    private TextView contactName;

    public ContactHolder(View itemView) {
        super(itemView);
        contactLayout = itemView.findViewById(R.id.cl_contact);
        contactName = itemView.findViewById(R.id.tv_contact_name);
    }

    public void bind(final Context context, final Contact contact) {
        contactName.setText(contact.getPhoneNo());
        contactLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra(CONTACT_NAME, contact.getPhoneNo());
                intent.putExtra(CONTACT_UID, contact.getUid());
                intent.putExtra(CHAT_UID, contact.getChatID());
                ((MainActivity) context).startActivity(intent);
            }
        });
    }
}
