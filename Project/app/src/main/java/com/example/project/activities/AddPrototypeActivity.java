package com.example.project.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.example.project.R;
import com.example.project.database.Prototype;
import com.example.project.database.PrototypeViewModel;

public class AddPrototypeActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.example.project.activity.AddPrototypeActivity.MESSAGE";
    public static final int NEW_PROTOTYPE_ACTIVITY_REQUEST_CODE = 1;

    private PrototypeViewModel mPrototypeViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_prototype);
    }

    public void createPrototype(View view) {
        Intent intent = new Intent(AddPrototypeActivity.this, PrototypeCaptureActivity.class);
        mPrototypeViewModel = new ViewModelProvider(this).get(PrototypeViewModel.class);
        EditText editText = (EditText) findViewById(R.id.editText);
        String name = editText.getText().toString();
        Prototype prototype = new Prototype();
        prototype.setPrototypeName(name);
        Integer prototypeID = prototype.getPrototypeId();
        mPrototypeViewModel.insert(prototype);
        intent.putExtra(EXTRA_MESSAGE, name);
        startActivityForResult(intent, NEW_PROTOTYPE_ACTIVITY_REQUEST_CODE);
    }}
