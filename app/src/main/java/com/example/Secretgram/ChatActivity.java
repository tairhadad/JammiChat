package com.example.Secretgram;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    static int idMessage=0;
    private String messageReceiverID, messageReceiverName, messageReceiverImage, messageSenderID;
    private TextView userName, userLastSeen;
    private CircleImageView userImage;

    private Toolbar ChatToolBar;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef, UsersRef;
    private ImageButton SendMessageButton;
    private ImageButton decryptedButton;
    private EditText MessageInputText;

    private final List<Messages> messagesList = new ArrayList<>();
    private List<Messages> messagesListtemp = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();
        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        messageReceiverImage = getIntent().getExtras().get("visit_image").toString();
        RootRef = FirebaseDatabase.getInstance().getReference();
        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        IntializeControllers();

        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);
        decryptedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.activity_dialog_encrypte,null);
                final EditText dKey = (EditText) mView.findViewById(R.id.key) ;
                final EditText dMessageNum = (EditText) mView.findViewById(R.id.input_message_number) ;
                final EditText TextDec = (EditText) mView.findViewById(R.id.TextDec) ;
                final TextView TextDecrypte = (TextView) mView.findViewById(R.id.TextDecrypte) ;

                Button mDes = (Button) mView.findViewById(R.id.decrypte);
                Button mRSA = (Button) mView.findViewById(R.id.Rsa);

                mDes.setOnClickListener(new View.OnClickListener() {
                    @Override

                    public void onClick(View v) {
                        messagesListtemp = new ArrayList<>(messagesList);
                        DES decrypted = new DES();
                        String value= dMessageNum.getText().toString();
                        int finalValue=Integer.parseInt(value);
                        System.out.println(messagesListtemp.get(finalValue-1).getMessage());
                        String[] cut = messagesListtemp.get(finalValue-1).getMessage().split("\n", 2);
                        cut[1] = cut[1].replaceAll("\n","");
                        String decrypted_msg = decrypted.Cipher(cut[1], dKey.getText().toString(), 2);
                        TextDec.setText( decrypted_msg);
                        TextDecrypte.setVisibility(View.VISIBLE);
                        TextDec.setVisibility(View.VISIBLE);

                    }

                });

                mRSA.setOnClickListener(new View.OnClickListener() {
                    @Override

                    public void onClick(View v) {
                        messagesListtemp = new ArrayList<>(messagesList);
                        DES decrypted = new DES();
                        String value= dMessageNum.getText().toString();
                        int finalValue=Integer.parseInt(value);
                        System.out.println(messagesListtemp.get(finalValue-1).getMessage());
                        String[] cut = messagesListtemp.get(finalValue-1).getMessage().split("\n", 2);
                        cut[1] = cut[1].replaceAll("\n","");
                        String decrypted_msg = decrypted.Cipher(cut[1], "RSA", 2);
                        TextDec.setText( decrypted_msg);
                        TextDecrypte.setVisibility(View.VISIBLE);
                        TextDec.setVisibility(View.VISIBLE);

                    }

                });
                builder.setView(mView);
                AlertDialog dialog = builder.create();
                dialog.show();

            }

        });
        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]
                        {
                                "DES",
                                "RSA",
                                "NONE"
                        };
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            SendMessage(0);

                        }
                        if (i == 1) {
                            SendMessage(1);
                        }
                        if (i == 2) {
                            SendMessage(2);
                        }
                    }
                });
                builder.show();
            }
        });
    }

    private void IntializeControllers() {

        ChatToolBar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolBar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);


        userImage = (CircleImageView) findViewById(R.id.custom_profile_Image);
        userName = (TextView) findViewById(R.id.custom_profile_name);
        userLastSeen = (TextView) findViewById(R.id.custom_user_last_seen);

        SendMessageButton = (ImageButton) findViewById(R.id.send_message_btn);
        decryptedButton = (ImageButton) findViewById(R.id.decrypte);

        MessageInputText = (EditText) findViewById(R.id.input_message);

        messageAdapter = new MessageAdapter(messagesList);
        userMessagesList = (RecyclerView) findViewById(R.id.private_message_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();

        RootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        Messages messages = dataSnapshot.getValue(Messages.class);

                        messagesList.add(messages);
                        messageAdapter.notifyDataSetChanged();

                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void SendMessage(int i) {
        if (i == 1) {


            //starting from here I'm trying...
            final String[] my_key = new String[1];
            UsersRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                    my_key[0] = dataSnapshot.child(messageSenderID).child("Key").getValue().toString();
                    String aa = "a";
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                    String aa = "a";
                }
            });

            UsersRef.child(messageSenderID).child("name").setValue("kfir8");

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
            DatabaseReference idRef = userRef.child(messageSenderID);
            DatabaseReference keyRef = idRef.child("Key");
            String sender_key = keyRef.toString();
            System.out.println(sender_key);
            //DatabaseReference userConnectionKey = RootRef.child("Users").child(messageSenderID).child("Key");
            //String connectionKey = userConnectionKey.getKey();

//untill here.


            String messageText ="Message number: " + (messagesList.size()+1) +"\n"+  MessageInputText.getText().toString();
            if (TextUtils.isEmpty(messageText)) {
                Toast.makeText(this, "first write your message...", Toast.LENGTH_SHORT).show();
            } else {
                String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = RootRef.child("Messages").child(messageSenderID)
                        .child(messageReceiverID).push();

                String messagePushID = userMessageKeyRef.getKey();

                Map messageTextBody = new HashMap();
                messageTextBody.put("message", messageText);
                messageTextBody.put("type", "text");
                messageTextBody.put("from", messageSenderID);

                Map messageBodyDetails = new HashMap();
                messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

                RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(ChatActivity.this, "Message sent Successful...", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                        MessageInputText.setText("");
                    }
                });
            }
        }

        if(i==0){
            final String messageText = MessageInputText.getText().toString();
            if (TextUtils.isEmpty(messageText)) {
                Toast.makeText(this, "first write your message...", Toast.LENGTH_SHORT).show();
            } else {

                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                View mView1 = getLayoutInflater().inflate(R.layout.activity_dialog_key, null);
                final EditText e_Key = (EditText) mView1.findViewById(R.id.key_Enc);

                Button mDes = (Button) mView1.findViewById(R.id.ecrypte);
                mDes.setOnClickListener(new View.OnClickListener() {
                    @Override

                    public void onClick(View v) {
                        DES des = new DES();
                        if (e_Key.length() != 8) {
                            Toast.makeText(ChatActivity.this, "The key most be 8 charcters...", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            String encrypted_msg = "Message number: " + (messagesList.size() + 1) + "\n" + des.Cipher(messageText, e_Key.getText().toString(), 1);

                            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                            String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                            DatabaseReference userMessageKeyRef = RootRef.child("Messages").child(messageSenderID)
                                    .child(messageReceiverID).push();

                            String messagePushID = userMessageKeyRef.getKey();

                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message", encrypted_msg);
                            messageTextBody.put("type", "text");
                            messageTextBody.put("from", messageSenderID);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {

                                        Toast.makeText(ChatActivity.this, "Message sent Successful...", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                    MessageInputText.setText("");
                                }
                            });

                        }
                    }
                });
                builder.setView(mView1);
                AlertDialog dialog = builder.create();
                dialog.show();
                }
        }
        if(i==2){
            final String messageText = MessageInputText.getText().toString();
            if (TextUtils.isEmpty(messageText)) {
                Toast.makeText(this, "first write your message...", Toast.LENGTH_SHORT).show();
            } else {

                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                View mView1 = getLayoutInflater().inflate(R.layout.activity_dialog_key, null);
                Button mDes = (Button) mView1.findViewById(R.id.Rsa);
                mDes.setOnClickListener(new View.OnClickListener() {
                    @Override

                    public void onClick(View v) {

                        DES des = new DES();
                        String encrypted_msg = "Message number: " + (messagesList.size() + 1) + "\n" + des.Cipher(messageText, "rsa", 1);

                        String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                        String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                        DatabaseReference userMessageKeyRef = RootRef.child("Messages").child(messageSenderID)
                                .child(messageReceiverID).push();

                        String messagePushID = userMessageKeyRef.getKey();

                        Map messageTextBody = new HashMap();
                        messageTextBody.put("message", encrypted_msg);
                        messageTextBody.put("type", "text");
                        messageTextBody.put("from", messageSenderID);

                        Map messageBodyDetails = new HashMap();
                        messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                        messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

                        RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (task.isSuccessful()) {

                                    Toast.makeText(ChatActivity.this, "Message sent Successful...", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                }
                                MessageInputText.setText("");
                            }
                        });


                    }
                });
                builder.setView(mView1);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
    }

}
