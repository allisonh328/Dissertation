package com.protocapture.project.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.protocapture.project.R;
import com.protocapture.project.database.Joint;
import com.protocapture.project.database.JointViewModel;
import com.protocapture.project.database.Link;
import com.protocapture.project.database.LinkViewModel;
import com.protocapture.project.database.Prototype;

public class LinkEditorActivity extends AppCompatActivity {

    private LinkViewModel mLinkViewModel;
    private Link mLink;

    private EditText linkName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link_editor);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        int linkID = intent.getIntExtra("link_id", 0);
        if(linkID == 0) {
            Toast.makeText(this, "Corrupted Link", Toast.LENGTH_LONG).show();
            return;
        }

        mLinkViewModel = new ViewModelProvider(this).get(LinkViewModel.class);
        mLinkViewModel.getLinkById(linkID).observe(this, new Observer<Link>() {
            @Override
            public void onChanged(@Nullable final Link link) {
                // Update the cached copy of the words in the adapter.
                mLink = link;
                myToolbar.setTitle(link.getLinkName());

                linkName = findViewById(R.id.editLinkName);
                linkName.setText(mLink.getLinkName());
            }
        });

        mLinkViewModel.getParentPrototype(linkID).observe(this, new Observer<Prototype>() {
            @Override
            public void onChanged(@NonNull final Prototype prototype) {
                TextView prototypeView = findViewById(R.id.textViewPrototypeParent);
                String displayPrototype = "Parent Prototype: " + prototype.getPrototypeName();
                prototypeView.setText(displayPrototype);
            }
        });

        mLinkViewModel.getEndpoint1(linkID).observe(this, new Observer<Joint>() {
            @Override
            public void onChanged(@NonNull final Joint joint1) {
                TextView endpoint1View = findViewById(R.id.textViewEndpoint1);
                String displayEndpoint1 = "Endpoint 1 joint: " + joint1.getJointName();
                endpoint1View.setText(displayEndpoint1);
            }
        });

        mLinkViewModel.getEndpoint2(linkID).observe(this, new Observer<Joint>() {
            @Override
            public void onChanged(@Nullable final Joint joint2) {
                TextView endpoint2View = findViewById(R.id.textViewEndpoint2);
                String displayEndpoint2 = "Endpoint 2 joint: " + joint2.getJointName();
                endpoint2View.setText(displayEndpoint2);
            }
        });
    }

    public void saveLink(View view) {

        mLink.setLinkName(linkName.getText().toString());
        mLinkViewModel.updateLink(mLink);

        Toast.makeText(this, "Link saved!", Toast.LENGTH_LONG).show();
    }

}
