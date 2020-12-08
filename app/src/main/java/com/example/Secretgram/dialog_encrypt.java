package com.example.Secretgram;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

public class dialog_encrypt extends AppCompatActivity {
    EditText key ;
    Button dec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_encrypt);
        InitializeFields();

    }
    private void InitializeFields() {
        dec=(Button)findViewById(R.id.decrypt);
        key = (EditText)findViewById(R.id.key);

    }
}
