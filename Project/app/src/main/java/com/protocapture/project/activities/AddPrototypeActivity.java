package com.protocapture.project.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.protocapture.project.R;
import com.protocapture.project.database.Prototype;
import com.protocapture.project.database.PrototypeViewModel;

public class AddPrototypeActivity extends AppCompatActivity {

    public static final String TAG = "ALLISON_COMMENT";
    public static final String EXTRA_MESSAGE = "com.example.project.activity.AddPrototypeActivity.MESSAGE";
    public static final int NEW_PROTOTYPE_ACTIVITY_REQUEST_CODE = 1;

    private PrototypeViewModel mPrototypeViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_prototype);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setTitle("ProtoCapture");
    }

    public void createPrototype(View view) {
        Intent intent = new Intent(AddPrototypeActivity.this, PrototypeCaptureActivity.class);
        mPrototypeViewModel = new ViewModelProvider(this).get(PrototypeViewModel.class);
        EditText editText = (EditText) findViewById(R.id.editText);
        String name = editText.getText().toString();
        Prototype prototype = new Prototype();
        prototype.setPrototypeName(name);
        mPrototypeViewModel.insert(prototype);
        intent.putExtra(EXTRA_MESSAGE, name);
        startActivityForResult(intent, NEW_PROTOTYPE_ACTIVITY_REQUEST_CODE);

    }}
