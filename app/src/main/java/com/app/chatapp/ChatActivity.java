package com.app.chatapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.app.chatapp.component.ChatHolder;
import com.app.chatapp.models.Chat;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import static com.app.chatapp.MainActivity.prefAuthUIDKey;
import static com.app.chatapp.MainActivity.prefKey;

public class ChatActivity extends AppCompatActivity {

    public static final String CONTACT_UID = "contact_uid_key";
    public static final String CHAT_UID = "chat_uid_key";
    public static final String CONTACT_NAME = "contact_name_key";
    private FirebaseRecyclerAdapter chatAdapter;
    private String userUID;
    private DatabaseReference dbReference;
    private RecyclerView chatRecyclerView;
    private Button sendBtn;
    private EditText sendText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Get bundle data
        String contactName = getIntent().getStringExtra(CONTACT_NAME);
        String chatUID = getIntent().getStringExtra(CHAT_UID);

        //set actionbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(contactName);
        }

        //Firebase Data
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userUID = this.getSharedPreferences(prefKey, MODE_PRIVATE)
                .getString(prefAuthUIDKey, user != null ? user.getUid() : "");
        dbReference = FirebaseDatabase.getInstance().getReference()
                .child("Chat")
                .child(chatUID);

        chatRecyclerView = findViewById(R.id.rv_messages);
        sendBtn = findViewById(R.id.btn_send);
        sendText = findViewById(R.id.et_send_message);

        initAdapter();
        chatRecyclerView.setAdapter(chatAdapter);

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long epoch = System.currentTimeMillis();
                Chat chat = new Chat(
                        userUID,
                        sendText.getText().toString(),
                        epoch);
                //Set index to epoch
                dbReference.child(Long.toString(epoch))
                        .setValue(chat, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                if (databaseError != null) {
                                    Toast.makeText(ChatActivity.this, "Failed to send msg.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                sendText.getText().clear();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        chatAdapter.startListening();
    }

    @Override
    protected void onPause() {
        super.onPause();
        chatAdapter.stopListening();
    }

    private void initAdapter() {
        //Setup query for the messages
        Query query = dbReference.limitToLast(50);
        FirebaseRecyclerOptions<Chat> options = new FirebaseRecyclerOptions.Builder<Chat>()
                .setQuery(query, Chat.class)
                .build();

        //Initializing the recycler chatAdapter
        chatAdapter = new FirebaseRecyclerAdapter<Chat, ChatHolder>(options) {
            private static final int VIEW_TYPE_OUTGOING = 1;
            private static final int VIEW_TYPE_INCOMING = 2;

            @Override
            protected void onBindViewHolder(@NonNull ChatHolder holder, int position, @NonNull Chat model) {
                holder.bind(model);
            }

            @Override
            public int getItemViewType(int position) {
                Chat msg = getItem(position);
                if (msg.getUid().equals(userUID)) {
                    return VIEW_TYPE_OUTGOING;
                } else {
                    return VIEW_TYPE_INCOMING;
                }
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                TextView textView = findViewById(R.id.tv_no_messages);
                textView.setText("Start a new conversation!");
                textView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }

            @NonNull
            @Override
            public ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view;
                if (viewType == VIEW_TYPE_OUTGOING) {
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_chat_outgoing, parent, false);
                    return new ChatHolder(view);
                } else {
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_chat_incoming, parent, false);
                    return new ChatHolder(view);
                }
            }
        };

        chatAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount());
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        chatAdapter.stopListening();
    }
}
