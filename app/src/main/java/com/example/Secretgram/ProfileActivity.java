package com.example.Secretgram;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    @SuppressLint("StaticFieldLeak")
    public static ProfileActivity instance;
    private String receiverUserID, senderUserID, Current_State;
    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button SendMessageReqestButton, DeclineMessageRequestButton;

    private DatabaseReference UserRef, ChatRequestRef, ContactsRef, NotificationRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        receiverUserID = Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).get("visit_user_id")).toString();
        senderUserID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        userProfileImage = (CircleImageView) findViewById(R.id.visit_profile_image);
        userProfileName = (TextView) findViewById(R.id.visit_user_name);
        userProfileStatus = (TextView) findViewById(R.id.visit_profile_status);
        SendMessageReqestButton = (Button) findViewById(R.id.send_message_request_button);
        DeclineMessageRequestButton = (Button) findViewById(R.id.decline_message_request_button);

        Current_State = "new";
        instance = this;
        RetrieveUserInfo();
    }

    private void RetrieveUserInfo() {
        UserRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && (dataSnapshot.hasChild("image"))) {
                    String userImage = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();
                    String userName = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
                    String userStatus = Objects.requireNonNull(dataSnapshot.child("status").getValue()).toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequests();
                } else {
                    String userName = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
                    String userStatus = Objects.requireNonNull(dataSnapshot.child("status").getValue()).toString();
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);
                    ManageChatRequests();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void ManageChatRequests() {

        ChatRequestRef.child(senderUserID).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(receiverUserID)) {
                    String request_type = Objects.requireNonNull(dataSnapshot.child(receiverUserID).child("request_type").getValue()).toString();
                    if (request_type.equals("sent")) {
                        Current_State = "request_sent";
                        SendMessageReqestButton.setText("Cancel Chat Request");
                    } else if (request_type.equals("received")) {
                        Current_State = "request_received";
                        SendMessageReqestButton.setText("Accept ChatRequect");
                        DeclineMessageRequestButton.setVisibility(View.VISIBLE);
                        DeclineMessageRequestButton.setEnabled(true);
                        DeclineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CancelChatRequest();
                            }
                        });
                    }
                } else {
                    ContactsRef.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(receiverUserID)) {
                                Current_State = "friends";
                                SendMessageReqestButton.setText("Remove this Contact");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if (!senderUserID.equals(receiverUserID)) {
            SendMessageReqestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SendMessageReqestButton.setEnabled(false);
                    if (Current_State.equals("new")) {
                        SendChatRequest();
                    }
                    if (Current_State.equals("request_sent")) {
                        CancelChatRequest();
                    }
                    if (Current_State.equals("request_received")) {
                        AcceptChetRequest();
                    }
                    if (Current_State.equals("friends")) {
                        RemoveSpecificContact();
                    }
                }
            });
        } else {
            SendMessageReqestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void RemoveSpecificContact() {
        ContactsRef.child(senderUserID).child(receiverUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            ContactsRef.child(receiverUserID).child(senderUserID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @SuppressLint("SetTextI18n")
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                SendMessageReqestButton.setEnabled(true);
                                                Current_State = "new";
                                                SendMessageReqestButton.setText("Send Chat Request");

                                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineMessageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptChetRequest() {
        ContactsRef.child(senderUserID).child(receiverUserID).child("Contacts")
                .setValue("Saves").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    ContactsRef.child(receiverUserID).child(senderUserID).child("Contacts")
                            .setValue("Saves").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                ChatRequestRef.child(senderUserID).child(receiverUserID).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    ChatRequestRef.child(receiverUserID).child(senderUserID).removeValue()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @SuppressLint("SetTextI18n")
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        SendMessageReqestButton.setEnabled(true);
                                                                        Current_State = "friends";
                                                                        SendMessageReqestButton.setText("Remove this Contact");
                                                                        DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                                        DeclineMessageRequestButton.setEnabled(false);
                                                                    }

                                                                }
                                                            });
                                                }

                                            }
                                        });
                            }
                        }
                    });
                }
            }
        });
    }

    private void CancelChatRequest() {

        ChatRequestRef.child(senderUserID).child(receiverUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            ChatRequestRef.child(receiverUserID).child(senderUserID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @SuppressLint("SetTextI18n")
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                SendMessageReqestButton.setEnabled(true);
                                                Current_State = "new";
                                                SendMessageReqestButton.setText("Send Chat Request");

                                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineMessageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void SendChatRequest() {
        ChatRequestRef.child(senderUserID).child(receiverUserID).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            ChatRequestRef.child(receiverUserID).child(senderUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            {
                                                if (task.isSuccessful()) {

                                                    HashMap<String, String> chatNotificatioMap = new HashMap<>();
                                                    chatNotificatioMap.put("from", senderUserID);
                                                    chatNotificatioMap.put("type", "request");
                                                    NotificationRef.child(receiverUserID).push()
                                                            .setValue(chatNotificatioMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @SuppressLint("SetTextI18n")
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                SendMessageReqestButton.setEnabled(true);
                                                                Current_State = "request_sent";
                                                                SendMessageReqestButton.setText("Cancel Chat Request");
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}










