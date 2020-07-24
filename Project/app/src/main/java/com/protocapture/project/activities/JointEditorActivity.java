package com.protocapture.project.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.protocapture.project.JointFragment;
import com.protocapture.project.R;
import com.protocapture.project.database.Joint;
import com.protocapture.project.database.JointViewModel;
import com.protocapture.project.database.Link;
import com.protocapture.project.database.Prototype;
import com.protocapture.project.database.PrototypeViewModel;

import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.List;

public class JointEditorActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private JointViewModel mJointViewModel;
    private Joint mJoint;

    private EditText jointName;
    private EditText jointX;
    private EditText jointY;
    private Spinner constraint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joint_editor);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        int jointID = intent.getIntExtra("joint_id", 0);
        if(jointID == 0) {
            Toast.makeText(this, "Corrupted Joint", Toast.LENGTH_LONG).show();
            return;
        }

        mJointViewModel = new ViewModelProvider(this).get(JointViewModel.class);
        mJointViewModel.getJointById(jointID).observe(this, new Observer<Joint>() {
            @Override
            public void onChanged(@Nullable final Joint joint) {
                // Update the cached copy of the words in the adapter.
                mJoint = joint;
                myToolbar.setTitle(joint.getJointName());

                jointName = findViewById(R.id.editJointName);
                jointName.setText(mJoint.getJointName());

                jointX = findViewById(R.id.editJointX);
                jointX.setText(Double.toString(mJoint.getXCoord()));

                jointY = findViewById(R.id.editJointY);
                jointY.setText(Double.toString(mJoint.getYCoord()));

                constraint = (Spinner) findViewById(R.id.editJointConstraint);
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(JointEditorActivity.this,
                        R.array.constraints_array, android.R.layout.simple_spinner_item);
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // Apply the adapter to the spinner
                constraint.setAdapter(adapter);
                constraint.setOnItemSelectedListener(JointEditorActivity.this);
                int spinnerPosition = mJoint.getConstraint();
                constraint.setSelection(spinnerPosition);
            }
        });

        mJointViewModel.getParentPrototype(jointID).observe(this, new Observer<Prototype>() {
            @Override
            public void onChanged(@NonNull final Prototype prototype) {
                TextView prototypeView = findViewById(R.id.textViewPrototype);
                String displayPrototype = "Parent Prototype: " + prototype.getPrototypeName();
                prototypeView.setText(displayPrototype);
            }
        });

        mJointViewModel.getParentLink1(jointID).observe(this, new Observer<Link>() {
            @Override
            public void onChanged(@Nullable final Link link1) {
                TextView link1View = findViewById(R.id.textViewLink1);
                String displayLink1;
                if(link1 == null) {
                    displayLink1 = "Parent Link 1: EMPTY";
                } else {
                    displayLink1 = "Parent Link 1: " + link1.getLinkName();
                }
                link1View.setText(displayLink1);
            }
        });

        mJointViewModel.getParentLink2(jointID).observe(this, new Observer<Link>() {
            @Override
            public void onChanged(@Nullable final Link link2) {
                TextView link2View = findViewById(R.id.textViewLink2);
                String displayLink2;
                if(link2 == null) {
                    displayLink2 = "Parent Link 2: EMPTY";
                } else {
                    displayLink2 = "Parent Link 2: " + link2.getLinkName();
                }
                link2View.setText(displayLink2);
            }
        });
    }

    public void saveJoint(View view) {

        mJoint.setJointName(jointName.getText().toString());
        mJoint.setXCoord(Double.parseDouble(jointX.getText().toString()));
        mJoint.setYCoord(Double.parseDouble(jointY.getText().toString()));
        mJointViewModel.updateJoint(mJoint);

        Toast.makeText(this, "Joint saved!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        mJoint.setConstraint(i);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        return;
    }
}
