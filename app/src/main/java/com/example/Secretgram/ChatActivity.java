package com.example.Secretgram;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.squareup.picasso.Picasso;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    //static int idMessage=0;
    private String messageReceiverID;
    private String messageSenderID;
    private TextView userName;
    private CircleImageView userImage;

    private DatabaseReference RootRef;
    private ImageButton SendMessageButton;
    private ImageButton decryptedButton;
    private EditText MessageInputText;

    private final List<Messages> messagesList = new ArrayList<>();
    private List<Messages> messagesListtemp = new ArrayList<>();
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;
    private final String[] my_key = new String[1];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        messageSenderID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        messageReceiverID = Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).get("visit_user_id")).toString();
        String messageReceiverName = Objects.requireNonNull(getIntent().getExtras().get("visit_user_name")).toString();
        String messageReceiverImage = Objects.requireNonNull(getIntent().getExtras().get("visit_image")).toString();
        RootRef = FirebaseDatabase.getInstance().getReference();
        //DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        InitializeControllers();

        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);
        decryptedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.activity_dialog_encrypt,null);
                final EditText dKey = (EditText) mView.findViewById(R.id.key) ;
                final EditText dMessageNum = (EditText) mView.findViewById(R.id.input_message_number) ;
                final EditText TextDec = (EditText) mView.findViewById(R.id.TextDec) ;
                final TextView TextDecrypt = (TextView) mView.findViewById(R.id.TextDecrypt) ;

                Button mDes = (Button) mView.findViewById(R.id.decrypt);
                Button mRSA = (Button) mView.findViewById(R.id.Rsa);

                mDes.setOnClickListener(new View.OnClickListener() {
                    @Override

                    public void onClick(View v) {
                        if (dKey.length() == 0){
                            Toast.makeText(ChatActivity.this, "Insert a Key", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            String DESKey = dKey.getText().toString();
                            if (dKey.length() != 8){
                                DESKey = autoCompleteKey(DESKey);
                            }
                            messagesListtemp = new ArrayList<>(messagesList);
                            DES decrypted = new DES();
                            String value= dMessageNum.getText().toString();
                            int finalValue=Integer.parseInt(value);
                            if (finalValue <= messagesListtemp.size()){

                                System.out.println(messagesListtemp.get(finalValue-1).getMessage());
                                String[] cut = messagesListtemp.get(finalValue-1).getMessage().split("\n", 2);
                                cut[1] = cut[1].replaceAll("\n","");
                                String decrypted_msg = null;
                                try {
                                    decrypted_msg = decrypted.Cipher(cut[1], DESKey, 2);
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                                TextDec.setText( decrypted_msg);
                                TextDecrypt.setVisibility(View.VISIBLE);
                                TextDec.setVisibility(View.VISIBLE);
                            }
                            else{
                                Toast.makeText(ChatActivity.this, "The Message Number does not exist.", Toast.LENGTH_SHORT).show();
                            }
                        }

                    }

                });

                mRSA.setOnClickListener(new View.OnClickListener() {
                    @Override

                    public void onClick(View v) {
                        //TODO - get the key from RSA list and decrypt it
                        messagesListtemp = new ArrayList<>(messagesList);
                        DES decrypted = new DES();
                        String value= dMessageNum.getText().toString();
                        int finalValue=Integer.parseInt(value);
                        System.out.println(messagesListtemp.get(finalValue-1).getMessage());
                        String[] cut = messagesListtemp.get(finalValue-1).getMessage().split("\n", 2);
                        cut[1] = cut[1].replaceAll("\n","");
                        String decrypted_msg = null;
                        try {
                            decrypted_msg = decrypted.Cipher(cut[1], "45454545", 2);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        TextDec.setText( decrypted_msg);
                        TextDecrypt.setVisibility(View.VISIBLE);
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
                CharSequence[] options = new CharSequence[]
                        {
                                "DES",
                                "RSA",
                                "NONE"
                        };
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 1) {
                            RootRef.child("Users").child(messageSenderID).child("RSA").setValue("1");
                        }
                        try {
                            SendMessage(i);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        //CloseKeyboard();
                    }
                });
                builder.show();
            }
        });
    }

    private void InitializeControllers() {

        Toolbar chatToolBar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolBar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        @SuppressLint("InflateParams") View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);


        userImage = (CircleImageView) findViewById(R.id.custom_profile_Image);
        userName = (TextView) findViewById(R.id.custom_profile_name);
        TextView userLastSeen = (TextView) findViewById(R.id.custom_user_last_seen);

        SendMessageButton = (ImageButton) findViewById(R.id.send_message_btn);
        decryptedButton = (ImageButton) findViewById(R.id.decrypt);

        MessageInputText = (EditText) findViewById(R.id.input_message);

        messageAdapter = new MessageAdapter(messagesList);
        userMessagesList = (RecyclerView) findViewById(R.id.private_message_list_of_users);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        RootRef.child("Users").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String myKey = Objects.requireNonNull(dataSnapshot.child(messageSenderID).child("Key").getValue()).toString();
                my_key[0] = myKey;
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

        RootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Messages messages = dataSnapshot.getValue(Messages.class);
                        messagesList.add(messages);
                        messageAdapter.notifyDataSetChanged();
                        userMessagesList.smoothScrollToPosition(Objects.requireNonNull(userMessagesList.getAdapter()).getItemCount());
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

    private void SendMessage(int i) throws UnsupportedEncodingException {
        //RSA sending message.
        if (i == 1) {
            //TODO send message and encrypt it with RSA
            final String messageText = "Message Number: " + (messagesList.size() + 1) + "\n" + MessageInputText.getText().toString();
            if (TextUtils.isEmpty(messageText)) {
                Toast.makeText(this, "Enter a Message", Toast.LENGTH_SHORT).show();
            } else {
               /* UsersRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        my_key[0] = dataSnapshot.child(messageSenderID).child("Key").getValue().toString();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });*/

                final Map messageBodyDetails = new HashMap();

                //if (my_key[0] != null) {
                    //String str = my_key[0];
                    DES des = new DES();
                    String encrypted_msg = "Message number: " + (messagesList.size() + 1) + "\n" + des.Cipher(messageText, "45454545", 1);

                    String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                    String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                    DatabaseReference userMessageKeyRef = RootRef.child("Messages").child(messageSenderID)
                            .child(messageReceiverID).push();

                    String messagePushID = userMessageKeyRef.getKey();

                    Map messageTextBody = new HashMap();
                    messageTextBody.put("message", encrypted_msg);
                    messageTextBody.put("type", "text");
                    messageTextBody.put("from", messageSenderID);
                    messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                    messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

                    RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {

                                Toast.makeText(ChatActivity.this, "Message Sent Successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                            }
                            MessageInputText.setText("");
                        }
                    });


               // }
            }
        }

        //DES sending message.
        else if(i==0){
            final AlertDialog dialog;
            final String messageText = MessageInputText.getText().toString();
            if (TextUtils.isEmpty(messageText)) {
                Toast.makeText(this, "Enter a Message", Toast.LENGTH_SHORT).show();
            } else {

                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                View mView1 = getLayoutInflater().inflate(R.layout.activity_dialog_key, null);
                final EditText e_Key = (EditText) mView1.findViewById(R.id.key_Enc);

                final Button mDes = (Button) mView1.findViewById(R.id.ecrypt);
                builder.setView(mView1);
                dialog = builder.create();
                dialog.show();
                mDes.setOnClickListener(new View.OnClickListener() {
                    @Override

                    public void onClick(View v) {
                        DES des = new DES();

                        //completing the Key to be 8 characters
                        if (e_Key.length() == 0) {
                            Toast.makeText(ChatActivity.this, "Insert a Key", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            String DESKey = e_Key.getText().toString();
                            if (e_Key.length() != 8) {
                                DESKey = autoCompleteKey(DESKey);
                            }
                            String encrypted_msg = null;
                            try {
                                encrypted_msg = "Message number: " + (messagesList.size() + 1) + "\n" + des.Cipher(messageText, DESKey, 1);
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
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
                                        Toast.makeText(ChatActivity.this, "Message Sent Successfully", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    } else {
                                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                    MessageInputText.setText("");
                                }
                            });
                        }
                    }
                });
                }
        }
        //No encryption sending message.
        else if(i==2){
            String messageText ="Message number: " + (messagesList.size()+1) +"\n"+  MessageInputText.getText().toString();
            if (TextUtils.isEmpty(messageText)) {
                Toast.makeText(this, "Enter a Message", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(ChatActivity.this, "Message Sent Successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                        MessageInputText.setText("");
                    }
                });
            }
        }
    }

    private void CloseKeyboard() {
        this.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
        this.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
    }

    private String autoCompleteKey(String key){
        if (key.length() < 8){
            while (key.length() < 8)
                key = key + forgeKey(key);
        }
        else{
            String extraKey = key.substring(7);
            key = key.substring(0,7) + forgeKey(extraKey);
        }
        return key;
    }

    //a number between 65-122 (A to z) to add to the key.
    private String forgeKey(String key){
        int forgedKey = 0;
        for (char c : key.toCharArray()){
            forgedKey += c;
        }
        forgedKey = (forgedKey % 57) + 65;
        return Character.toString((char) forgedKey);
    }

}
