package com.app.chatapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.app.chatapp.component.ContactHolder;
import com.app.chatapp.models.Chat;
import com.app.chatapp.models.Contact;
import com.app.chatapp.models.User;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int CODE_LOGIN = 100;
    protected static final String prefKey = "com.app.chatapp";
    protected static final String prefAuthUIDKey = "com.app.chatapp.firebaseauth.user.uid";

    private String userUID;
    private String userPhoneNo;
    private FirebaseRecyclerAdapter contactAdapter;
    private RecyclerView contactRecyclerView;
    private DatabaseReference userDBReference;
    private DatabaseReference chatDBReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        contactRecyclerView = findViewById(R.id.rv_chats);
        setSupportActionBar(toolbar);

        //Check sign in
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            saveMyUID(auth.getCurrentUser().getUid());
            userPhoneNo = auth.getCurrentUser().getPhoneNumber();
            initDB();
            initUI();
        } else {
            startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(Arrays.asList(
                            new AuthUI.IdpConfig.PhoneBuilder().build()
                    )).build(), CODE_LOGIN);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (contactAdapter != null) {
            contactAdapter.startListening();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (contactAdapter != null) {
            contactAdapter.stopListening();
        }
    }

    private void initDB() {
        userDBReference = FirebaseDatabase.getInstance().getReference().child("Users");
        chatDBReference = FirebaseDatabase.getInstance().getReference().child("Chats");
    }

    private void initAdapter() {
        Query query = userDBReference.child(userUID).child("contacts").limitToLast(50);
        FirebaseRecyclerOptions<Contact> options = new FirebaseRecyclerOptions.Builder<Contact>()
                .setQuery(query, new SnapshotParser<Contact>() {
                    @NonNull
                    @Override
                    public Contact parseSnapshot(@NonNull DataSnapshot snapshot) {
                        Contact contact = snapshot.getValue(Contact.class);
                        if (contact != null) {
                            contact.setUid(snapshot.getKey());
                        } else {
                            contact = new Contact();
                        }
                        return contact;
                    }
                }).build();
        contactAdapter = new FirebaseRecyclerAdapter<Contact, ContactHolder>(options) {
            @NonNull
            @Override
            public ContactHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ContactHolder(LayoutInflater.from(MainActivity.this)
                        .inflate(R.layout.item_contact, parent, false));
            }

            @Override
            protected void onBindViewHolder(@NonNull ContactHolder holder, int position, @NonNull Contact model) {
                holder.bind(MainActivity.this, model);
            }
        };
        contactAdapter.startListening();
    }


    private void initUI() {
        initAdapter();
        contactRecyclerView.setAdapter(contactAdapter);
        contactRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDialog().show();
            }
        });
    }

    private AlertDialog createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add New Contact");
        final EditText searchBar = new EditText(MainActivity.this);
        searchBar.setHint("Phone No.");
        searchBar.setInputType(InputType.TYPE_CLASS_PHONE);
        builder.setView(searchBar);
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String contactPhoneNo = "+6" + searchBar.getText().toString();
                Query query = userDBReference.orderByChild("phoneNo").equalTo(contactPhoneNo);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                User contact = data.getValue(User.class);
                                if (contact != null) {
                                    if (contact.getPhoneNo().equals(contactPhoneNo) && data.getKey() != null) {
                                        DatabaseReference ref = chatDBReference.push();
                                        userDBReference.child(userUID) //Set your conversation
                                                .child("contacts")
                                                .child(data.getKey())
                                                .child("chatID").setValue(ref.getKey());
                                        userDBReference.child(userUID)
                                                .child("contacts")
                                                .child(data.getKey())
                                                .child("phoneNo").setValue(contact.getPhoneNo());

                                        userDBReference.child(data.getKey()) //Set your contact's conversation
                                                .child("contacts")
                                                .child(userUID)
                                                .child("chatID").setValue(ref.getKey());
                                        userDBReference.child(data.getKey())
                                                .child("contacts")
                                                .child(userUID)
                                                .child("phoneNo").setValue(userPhoneNo);

                                        Toast.makeText(MainActivity.this, "Contact Added.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Contact not found.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MainActivity.this,
                                "Add Operation Canceled, Please Try Again Later.",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        return builder.create();
    }

    private void saveMyUID(String UID) {
        userUID = UID;
        SharedPreferences pref = this.getSharedPreferences(prefKey, Context.MODE_PRIVATE);
        pref.edit().putString(prefAuthUIDKey, userUID).apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE_LOGIN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            // Successfully signed in
            if (resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    saveMyUID(user.getUid());
                    userPhoneNo = user.getPhoneNumber();
                    initDB();
                    userDBReference.child(userUID).child("phoneNo").setValue(user.getPhoneNumber());
                }
                Log.wtf(TAG, "Login success.");
                initUI();
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show();
                } else if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, "No Network.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Unknown Login Error", Toast.LENGTH_SHORT).show();
                    Log.wtf(TAG, "Sign-in error: ", response.getError());
                }
                finishAffinity();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (contactAdapter != null) {
            contactAdapter.stopListening();
        }
    }
}
