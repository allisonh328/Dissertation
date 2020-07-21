package com.protocapture.project.activities;

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
import android.widget.EditText;
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

public class JointEditorActivity extends AppCompatActivity {

    JointViewModel mJointViewModel;
    Joint mJoint;

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

                EditText jointName = findViewById(R.id.editJointName);
                jointName.setText(mJoint.getJointName());

                EditText jointX = findViewById(R.id.editJointX);
                jointX.setText(Double.toString(mJoint.getXCoord()));

                EditText jointY = findViewById(R.id.editJointY);
                jointY.setText(Double.toString(mJoint.getYCoord()));

                EditText constraint = findViewById(R.id.editJointConstraint);
                constraint.setText(Double.toString(mJoint.getConstraint()));
            }
        });

        mJointViewModel.getParentPrototype(jointID).observe(this, new Observer<Prototype>() {
            @Override
            public void onChanged(@Nullable final Prototype prototype) {
                TextView prototypeView = findViewById(R.id.textViewPrototype);
                prototypeView.setText(prototype.getPrototypeName());
            }
        });

        mJointViewModel.getParentLink1(jointID).observe(this, new Observer<Link>() {
            @Override
            public void onChanged(@Nullable final Link link1) {
                TextView link1View = findViewById(R.id.textViewLink1);
                link1View.setText(link1.getLinkName());
            }
        });

        mJointViewModel.getParentLink2(jointID).observe(this, new Observer<Link>() {
            @Override
            public void onChanged(@Nullable final Link link2) {
                TextView link2View = findViewById(R.id.textViewLink2);
                link2View.setText(link2.getLinkName());
            }
        });

    }

    public void saveJoint(View view) {
        return;
    }

}
