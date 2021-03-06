package com.protocapture.project.activities;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MotionEventCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.*;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.protocapture.project.ComponentCollectionFragment;
import com.protocapture.project.HelpFragment;
import com.protocapture.project.R;
import com.protocapture.project.SimulatorView;
import com.protocapture.project.database.Joint;
import com.protocapture.project.database.JointViewModel;
import com.protocapture.project.database.Link;
import com.protocapture.project.database.LinkViewModel;
import com.protocapture.project.database.Prototype;
import com.protocapture.project.database.PrototypeViewModel;

import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SimulatorActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.example.project.activity.ViewPrototypeActivity.MESSAGE";
    private final static String TAG = "ALLISON";
    private final static int JOINT_EDITOR_REQUEST_CODE = 1;

    private DisplayMetrics displayMetrics = new DisplayMetrics();
    private SimulatorView mSimulatorView;
    private PrototypeViewModel mPrototypeViewModel;
    private LinkViewModel mLinkViewModel;
    private JointViewModel mJointViewModel;
    private Button okButton;

    private Prototype mPrototype;
    private List<Link> mLinks;
    private List<Joint> mJoints;
    private List<Point> points = new ArrayList<>();
    //private List<Joint> fixJoints = new ArrayList<>();
    private List<Integer> selected = new ArrayList<>();
    private Joint editJoint = null;
    private Point origin;
    private ArrayList<Joint> complete = new ArrayList<>();
    private HashMap<String, Double> radii = new HashMap<>();
    private Joint match = null;
    private Joint motor;

    private Bitmap backgroundBitmap;
    private Canvas background;
    private Paint linksPaint;
    private Paint pathPaint;
    private Paint jointsPaint;

    private boolean selectDriver = false;
    private boolean selectEditJoint = false;
    private boolean selectFixJoints = false;
    private Integer motorIndex = -1;
    private boolean kill = false;
    private int moveCounter = 0;

    //private final ArrayList<Point> points = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simulator);
        mSimulatorView = (SimulatorView) findViewById(R.id.simulator_view);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
        // Set the Title of the screen to the Prototype name
        String prototypeName = getIntent().getStringExtra(EXTRA_MESSAGE);
        //myToolbar.setTitle(prototypeName);

        okButton = (Button) findViewById(R.id.button_ok);
        okButton.bringToFront();

        Toast.makeText(this, "Click and drag links to edit!", Toast.LENGTH_LONG).show();

        // Set up Paint to draw the motion profile
        pathPaint = new Paint();
        pathPaint.setAntiAlias(true);
        pathPaint.setColor(Color.MAGENTA);
        pathPaint.setStrokeWidth(4);

        // Set up Paint to draw the joints (connecting points)
        jointsPaint = new Paint();
        jointsPaint.setAntiAlias(true);
        jointsPaint.setColor(Color.BLUE);
        jointsPaint.setStyle(Paint.Style.FILL);

        // Set up Paint to draw the links (lines)
        linksPaint = new Paint();
        linksPaint.setAntiAlias(true);
        linksPaint.setColor(Color.BLUE);
        linksPaint.setStrokeWidth(4);

        mJointViewModel = new ViewModelProvider(this).get(JointViewModel.class);
        mLinkViewModel = new ViewModelProvider(this).get(LinkViewModel.class);
        mPrototypeViewModel = new ViewModelProvider(this).get(PrototypeViewModel.class);
        mPrototypeViewModel.getPrototype(prototypeName).observe(this, new Observer<Prototype>() {
            @Override
            public void onChanged(@Nullable final Prototype prototype) {

                mPrototype = prototype;
                setBackground();

                myToolbar.setTitle(mPrototype.getPrototypeName());

                mLinkViewModel.getAllProtoLinks(mPrototype.getPrototypeId()).observe(SimulatorActivity.this, new Observer<List<Link>>() {
                    @Override
                    public void onChanged(@Nullable final List<Link> links) {
                        mLinks = links;

                        mJointViewModel.getAllProtoJoints(mPrototype.getPrototypeId()).observe(SimulatorActivity.this, new Observer<List<Joint>>() {
                            @Override
                            public void onChanged(@Nullable final List<Joint> joints) {
                                mJoints = joints;
                                for (Joint joint : mJoints) {
                                    points.add(new Point(joint.getXCoord(), joint.getYCoord()));
                                }
                                drawFrame(mJoints);
                            }
                        });
                    }
                });

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.simulator_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        selected.clear();
        drawFrame(mJoints);

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_edit_joint) {
            actionEditJoint();

        } else if (id == R.id.action_view_sim) {
            actionViewSim();

        } else if (id == R.id.action_fix_joints) {
            actionFixJoints();

        } else if (id == R.id.action_reset) {
            actionReset();

        } else if (id == R.id.action_help) {
            actionHelp();
        }
        return super.onOptionsItemSelected(item);
    }


    private void actionEditJoint() {
        selectDriver = false;
        selectEditJoint = false;
        selectEditJoint = true;
        Toast.makeText(SimulatorActivity.this, "Select joint to edit.", Toast.LENGTH_SHORT).show();
        okButton.setText(R.string.edit_button);
        okButton.setVisibility(View.VISIBLE);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editJoint == null) {
                    Toast.makeText(SimulatorActivity.this, "Please select a joint to edit.", Toast.LENGTH_SHORT).show();
                    return;
                }
                okButton.setVisibility(View.GONE);
                selected.clear();
                Intent intent = new Intent(SimulatorActivity.this, JointEditorActivity.class);
                intent.putExtra("joint_id", editJoint.getJointId());
                startActivityForResult(intent, JOINT_EDITOR_REQUEST_CODE);
            }
        });
    }


    private void actionViewSim() {
        selectEditJoint = false;
        selectFixJoints = false;
        selectDriver = true;

        Toast.makeText(SimulatorActivity.this, "Select motor location.", Toast.LENGTH_LONG).show();
        okButton.setText(R.string.button_animate);
        okButton.setVisibility(View.VISIBLE);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (motorIndex == -1) {
                    Toast.makeText(SimulatorActivity.this, "Please select motor location.", Toast.LENGTH_LONG).show();
                    return;
                }
                selected.clear();
                selectDriver = false;
                animateMechanism(mSimulatorView);
            }
        });
    }


    private void actionFixJoints() {
        selectEditJoint = false;
        selectDriver = false;
        selectFixJoints = true;
        Toast.makeText(SimulatorActivity.this, "Selected joints will be fixed.\n" +
                "All others will be free.", Toast.LENGTH_LONG).show();
        okButton.setText(R.string.fix_button);
        okButton.setVisibility(View.VISIBLE);

        for(int i = 0; i < mJoints.size(); i++) {
            if (mJoints.get(i).getConstraint() == Joint.FIXED) {
                //fixJoints.add(mJoints.get(i));
                selected.add(i);
            }
        }
        drawFrame(mJoints);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i = 0; i < mJoints.size(); i++) {
                    if(selected.contains(i)) {
                        mJoints.get(i).setConstraint(Joint.FIXED);
                    } else {
                        mJoints.get(i).setConstraint(Joint.FREE);
                    }
                }
                mJointViewModel.updateJoints(mJoints);
                selectFixJoints = false;
                okButton.setVisibility(View.GONE);
                selected.clear();
                Toast.makeText(SimulatorActivity.this, "Joints constrained!", Toast.LENGTH_LONG).show();
            }
        });
    }


    private void actionReset() {
        setBackground();
        for (int i = 0; i < mJoints.size(); i++) {
            mJoints.get(i).setXCoord(points.get(i).x);
            mJoints.get(i).setYCoord(points.get(i).y);
        }
        drawFrame(mJoints);
    }


    private void actionHelp() {
        FragmentManager fm = getSupportFragmentManager();
        HelpFragment fragment = new HelpFragment();
        fragment.setStyle(HelpFragment.STYLE_NORMAL, R.style.CustomDialog);
        Bundle args = new Bundle();
        args.putString("key", "simulate");
        fragment.setArguments(args);
        fragment.show(fm, "fragment_help");
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        Log.i(TAG, "Touch");
        float xTouch = -1;
        float yTouch = -1;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                xTouch = (event.getX() / displayMetrics.widthPixels) * background.getWidth();
                yTouch = (event.getY() / displayMetrics.heightPixels) * background.getHeight();
                match = checkJointHit(xTouch, yTouch);
                if(match == null) {
                    return true;
                }

                //int maxDistance = displayMetrics.widthPixels/30;
                if (selectDriver) {
                    selectDriver(xTouch, yTouch);
                } else if (selectEditJoint) {
                    selectEditJoint();
                } else if (selectFixJoints) {
                    setSelectFixJoints();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (selectEditJoint || selectDriver || selectFixJoints) {
                    break;
                }
                Log.i(TAG, "Time: " + System.nanoTime());
                moveCounter++;
                xTouch = (event.getX() / displayMetrics.widthPixels) * background.getWidth();
                yTouch = (event.getY() / displayMetrics.heightPixels) * background.getHeight();
                if(match == null) {
                    return true;
                } else {
                    int index = mJoints.indexOf(match);
                    Log.i(TAG, "match = " + match.getJointName() + " index = " + index);
                    Point moveJoint = points.get(index);
                    moveJoint.x = xTouch;
                    moveJoint.y = yTouch;
                    match.setXCoord((double) xTouch);
                    match.setYCoord((double) yTouch);
                    //mJointViewModel.updateJoint(match);
                    if (moveCounter > 7) {
                        drawFrame(mJoints);
                        moveCounter = 0;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                //selectDriver = false;
                //selectEditJoint = false;
                match = null;
                break;
        }
        return super.onGenericMotionEvent(event);
    }


    private void selectDriver(float xTouch, float yTouch) {
        Log.i(TAG, "Waiting for motor selection...");
        Log.i(TAG, "x = " + xTouch + ", y = " + yTouch);
        if (match.getConstraint() != Joint.FIXED) {
            Toast.makeText(this, "The motor location must be a fixed joint.", Toast.LENGTH_LONG).show();
        } else {
            selected.clear();
            motorIndex = mJoints.indexOf(match);
            selected.add(motorIndex);
            drawFrame(mJoints);
            Toast.makeText(this, "Ready to animate!", Toast.LENGTH_SHORT).show();
        }
    }


    private void selectEditJoint() {
        selected.clear();
        editJoint = match;
        selected.add(mJoints.indexOf(match));
        drawFrame(mJoints);
    }


    private void setSelectFixJoints() {
        if(selected.contains(mJoints.indexOf(match))) {
            selected.remove((Object) mJoints.indexOf(match));
            drawFrame(mJoints);
        } else {
            selected.add(mJoints.indexOf(match));
            drawFrame(mJoints);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        mSimulatorView.stopThread();
        mJointViewModel.updateJoints(mJoints);
    }

    private void setBackground() {
        Bitmap bitmap = null;
        try {
            String filename = mPrototype.getPrototypeBitmap();
            Log.i(TAG, filename);

            FileInputStream input = new FileInputStream(new File(filename));
            if (input.available() != 0) {
                bitmap = BitmapFactory.decodeStream(input);
            }
            input.close();
        } catch (Exception e) {
            Log.e(TAG, "File input Error: ", e);
        }

        backgroundBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        bitmap.recycle();
        background = new Canvas(backgroundBitmap);
    }

    private void drawFrame(List<Joint> drawingPoints) {

        Log.i(TAG, "drawing new frame");
        long startTime = System.nanoTime();

        Bitmap drawableBitmap = backgroundBitmap.copy(backgroundBitmap.getConfig(), true);
        Canvas drawable = new Canvas(drawableBitmap);

        // Draw links
        for (Link link : mLinks) {
            Joint joint1 = getJointbyId(link.getEndpoint1());
            Joint joint2 = getJointbyId(link.getEndpoint2());
            if (joint1 != null && joint2 != null) {
                Point endpoint1 = new Point(joint1.getXCoord(), joint1.getYCoord());
                Point endpoint2 = new Point(joint2.getXCoord(), joint2.getYCoord());

                drawable.drawLine((float) endpoint1.x, (float) endpoint1.y, (float) endpoint2.x, (float) endpoint2.y, linksPaint);
            }
        }

        // Draw joints & motion profiles
        for (int i = 0; i < drawingPoints.size(); i++) {
            Joint joint = drawingPoints.get(i);
            //Log.i(TAG, "Point at: (" + point.x + "," + point.y + ")");
            double x = joint.getXCoord();
            double y = joint.getYCoord();
            if(selected.contains(i)) {
                jointsPaint.setColor(Color.RED);
                drawable.drawCircle((float) x, (float) y, 8, jointsPaint);
                jointsPaint.setColor(Color.BLUE);
            } else {
                drawable.drawCircle((float) x, (float) y, 8, jointsPaint);
            }
            if(joint.getConstraint().equals(Joint.FIXED)) {
                Path path = new Path();
                path.moveTo((float) x, (float) y + 4);
                path.lineTo((float) x + 16, (float) y + 20);
                path.lineTo((float) x - 16, (float) y + 20);
                path.lineTo((float) x, (float) y + 4);
                drawable.drawPath(path, linksPaint);
            }
            background.drawCircle((float) x, (float) y, 2, pathPaint);
        }

        mSimulatorView.drawBitmap(drawableBitmap);
        long stopTime = System.nanoTime();
        Log.i(TAG, "DrawFrame: time elapsed = " + Long.toString(stopTime - startTime));
    }


    public void animateMechanism(View view) {

        // Initialise variables
        complete.clear();
        motor = mJoints.get(motorIndex);
        origin = new Point(motor.getXCoord(), motor.getYCoord());
        Point motorPoint = convertCoordinates(new Point(motor.getXCoord(), motor.getYCoord()), origin);
        //Log.i(TAG, "motor point = (" + motorPoint.x + "," + motorPoint.y + ")");

        // Add distances between joints (radii for later calculations)
        for (Joint joint1 : mJoints) {
            for (Joint joint2 : mJoints) {
                String name = joint1.getJointId() + "to" + joint2.getJointId();
                Point point1 = new Point(joint1.getXCoord(), joint1.getYCoord());
                Point point2 = new Point(joint2.getXCoord(), joint2.getYCoord());
                radii.put(name, getMagnitude(point1, point2));
            }
        }

        // Calculate starting angles
        double theta1 = 0;
        double theta2 = 0;
        Integer driveLink1ID = motor.getLink1ID();
        if (driveLink1ID != null) {
            Point endpoint1 = getOtherEndpoint(driveLink1ID, motor.getJointId());
            theta1 = getAngle(endpoint1, motorPoint);
            //Log.i(TAG, "Theta1 = " + theta1);
        }
        Integer driveLink2ID = motor.getLink2ID();
        if (driveLink2ID != null) {
            Point endpoint2 = getOtherEndpoint(driveLink2ID, motor.getJointId());
            theta2 = getAngle(endpoint2, motorPoint);
           // Log.i(TAG, "Theta2 = " + theta2);
        }

        // Add all FIXED joints to the "complete" list (their position is fixed)
        for (double i = 0; i < 2 * Math.PI; i = i + 0.13) {
            long startTime = System.nanoTime();
            for (Joint joint : mJoints) {
                if (joint.getConstraint() == Joint.FIXED) {
                    complete.add(joint);
                    //Log.i(TAG, "adding joint " + joint.getJointName() + " to fixed joints");
                }
            }

            // Simulate links driven directly by motor
            if (driveLink1ID != null) {
                calculateDriveLink(driveLink1ID, theta1 + i);
            }
            if (driveLink2ID != null) {
                calculateDriveLink(driveLink2ID, theta2 + i);
            }

            // Iterate to finish simulation
            iterate(1);
            if (kill) {
                //Log.i(TAG, "Entered kill loop");
                kill = false;

                for (double j = i - 0.03; j > i - 2 * Math.PI; j = j - 0.03) {
                    complete.clear();
                    for (Joint joint : mJoints) {
                        if (joint.getConstraint() == Joint.FIXED) {
                            complete.add(joint);
                            //Log.i(TAG, "adding joint " + joint.getJointName() + " to fixed joints");
                        }
                    }

                    // Simulate links driven directly by motor
                    if (driveLink1ID != null) {
                        calculateDriveLink(driveLink1ID, theta1 + j);
                    }
                    if (driveLink2ID != null) {
                        calculateDriveLink(driveLink2ID, theta2 + j);
                    }

                    // Iterate to finish simulation
                    iterate(1);
                    if (kill) {
                        kill = false;
                        return;
                    }

                    // Once all locations have been calculated, draw the step then start over
                    drawFrame(complete);
                }
                return;
            }

            // Once all locations have been calculated, draw the step then start over
            if(complete.size() != mJoints.size()) {
                Toast.makeText(this, "Cannot calculate all points!\n" +
                        "Mechanism may be under-constrained.", Toast.LENGTH_SHORT).show();
                return;
            }
            for (Link link: mLinks) {
                Joint joint1 = getJointbyId(link.getEndpoint1());
                Joint joint2 = getJointbyId(link.getEndpoint2());
                String name = joint1.getJointId() + "to" + joint2.getJointId();
                Point point1 = new Point(joint1.getXCoord(), joint1.getYCoord());
                Point point2 = new Point(joint2.getXCoord(), joint2.getYCoord());
                double mag = getMagnitude(point1, point2);
                //Log.i(TAG, "magnitude = " + mag + ", radius = " + radii.get(name));
                if(mag - radii.get(name) > .001 || mag - radii.get(name) < -.001) {
                    Toast.makeText(this, "Invalid conditions!\n" +
                            "Mechanism may be over-constrained.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            long stopTime = System.nanoTime();
            Log.i(TAG, "Animation: time elapsed = " + Long.toString(stopTime - startTime));
            drawFrame(complete);
            complete.clear();
        }
    }

    private void calculateDriveLink(int driveLinkID, double theta) {
        List<Joint> driveLink1Joints = getJointsOnLink(driveLinkID);
        for (Joint joint : driveLink1Joints) {
            if (!complete.contains(joint)) {
                //Log.i(TAG, "original point = (" + joint.getXCoord() + "," + joint.getYCoord() + ")");
                //Point calcPoint = convertCoordinates(new Point(joint.getXCoord(), joint.getYCoord()), origin);
                //double radius = getMagnitude(calcPoint, motorPoint);
                double radius = radii.get(joint.getJointId() + "to" + motor.getJointId());
                Point drawPoint = reconvertCoordinates(new Point(radius * Math.cos(theta), radius * Math.sin(theta)), origin);
                joint.setXCoord(drawPoint.x);
                joint.setYCoord(drawPoint.y);
                //Log.i(TAG, "new point = (" + drawPoint.x + "," + drawPoint.y + ")");
                complete.add(joint);
                //Log.i(TAG, "adding " + joint.getJointName() + " to complete");
            }
        }
    }

    private void iterate(int index) {
        // Get the next joint from the "complete" list (location has been calculated)
        Joint next = complete.get(index);
        //Log.i(TAG, "complete " + index + " is joint " + next.getJointName());

        // Check the first link for a new point to calculate
        Integer link1id = next.getLink1ID();
        if (link1id != null) {
            //Log.i(TAG, "Trying link 1 of joint " + next.getJointName());
            simulate(next, link1id);
        }
        if (kill) {
            //Log.i(TAG, "iterate method: I am dead!");
            return;
        }

        // Check the second link for a new point to calculate
        Integer link2id = next.getLink2ID();
        if (link2id != null) {
            //Log.i(TAG, "Trying link 2 of joint " + next.getJointName());
            simulate(next, link2id);
        }
        if (kill) {
            //Log.i(TAG, "iterate method: I am dead!");
            return;
        }

        Integer link3id = next.getLink3ID();
        if (link3id != null) {
            //Log.i(TAG, "Trying link 3 of joint " + next.getJointName());
            simulate(next, link3id);
        }
        if (kill) {
            //Log.i(TAG, "iterate method: I am dead!");
            return;
        }

        Integer link4id = next.getLink4ID();
        if (link4id != null) {
            //Log.i(TAG, "Trying link 4 of joint " + next.getJointName());
            simulate(next, link4id);
        }
        if (kill) {
            //Log.i(TAG, "iterate method: I am dead!");
            return;
        }

        // Repeat with next joint
        index++;
        if (index < complete.size()) {
            iterate(index);
        }
    }

    private void simulate(Joint joint0, int linkid) {

        // Get all the joints on the selected link
        List<Joint> link1Joints = getJointsOnLink(linkid);
        if (link1Joints.isEmpty()) {
            return;
        }

        // Look for any joints that are still "free" (location not calculated yet)
        for (Joint joint1 : link1Joints) {
            if (!complete.contains(joint1)) {

                // Once a free joint has been located...
                //Log.i(TAG, "Found free joint " + joint1.getJointName());

                // Check its first link for another fixed point (location already calculated)
                Integer link1id = joint1.getLink1ID();
                if (link1id != null) {
                    //Log.i(TAG, "Trying link 1 of joint " + joint1.getJointName());
                    completeSimulation(joint0, joint1, link1id);
                }
                if (kill) {
                    //Log.i(TAG, "simulate method: I am dead!");
                    return;
                }

                // Check its second link for another fixed point (Location already calculated)
                Integer link2id = joint1.getLink2ID();
                if (link2id != null) {
                    //Log.i(TAG, "Trying link2 of joint " + joint1.getJointName());
                    completeSimulation(joint0, joint1, link2id);
                }
                if (kill) {
                    //Log.i(TAG, "simulate method: I am dead!");
                    return;
                }

                // Check its third link for another fixed point (location already calculated)
                Integer link3id = joint1.getLink3ID();
                if (link3id != null) {
                    //Log.i(TAG, "Trying link 3 of joint " + joint1.getJointName());
                    completeSimulation(joint0, joint1, link3id);
                }
                if (kill) {
                    //Log.i(TAG, "simulate method: I am dead!");
                    return;
                }

                // Check its first link for another fixed point (location already calculated)
                Integer link4id = joint1.getLink4ID();
                if (link4id != null) {
                    //Log.i(TAG, "Trying link 4 of joint " + joint1.getJointName());
                    completeSimulation(joint0, joint1, link4id);
                }
            }
        }
    }

    private void completeSimulation(Joint joint0, Joint joint1, int linkid) {
        // If the location of this joint has already been calculated, return (could happen on second link)
        if (complete.contains(joint1)) {
            return;
        }

        // Look for second fixed point on link
        Joint joint2 = null;
        List<Joint> link2Joints = getJointsOnLink(linkid);
        if (link2Joints.isEmpty()) {
            return;
        }
        for (Joint joint : link2Joints) {
            if (complete.contains(joint) && !joint.equals(joint0)) {
                joint2 = joint;
                //Log.i(TAG, "Found second fixed joint: " + joint2.getJointName());
            }
        }
        if (joint2 == null) {
            return;
        }

        // Calculate free joints location using intersection of two circles method
        try {

            // Convert all points into a coordinate system that's easy to deal with
            Point fixed1 = convertCoordinates(new Point(joint0.getXCoord(), joint0.getYCoord()), origin);
            //Log.i(TAG, "Fixed1 = " + joint0.getJointName());
            Point free = convertCoordinates(new Point(joint1.getXCoord(), joint1.getYCoord()), origin);
            //Log.i(TAG, "Free = " + joint1.getJointName());
            Point fixed2 = convertCoordinates(new Point(joint2.getXCoord(), joint2.getYCoord()), origin);
            //Log.i(TAG, "Fixed2 = " + joint2.getJointName());

            // Get radii from list
            double radius1 = radii.get(joint0.getJointId() + "to" + joint1.getJointId());
            //Log.i(TAG, "radius 1 = " + radius1);
            double radius2 = radii.get(joint2.getJointId() + "to" + joint1.getJointId());
            //Log.i(TAG, "radius 2 = " + radius2);

            // Lots of weird math to locate circle intersections
            double u = -1 * (fixed1.y - fixed2.y) / (fixed1.x - fixed2.x);
            double v = (sqr(radius2) - sqr(radius1) + sqr(fixed1.x) - sqr(fixed2.x) + sqr(fixed1.y) - sqr(fixed2.y)) / (2 * (fixed1.x - fixed2.x));
            double aQuad = sqr(u) + 1;
            double bQuad = 2 * u * v - 2 * fixed1.x * u - 2 * fixed1.y;
            double cQuad = sqr(v) - 2 * fixed1.x * v + sqr(fixed1.x) + sqr(fixed1.y) - sqr(radius1);
            double determinant = sqr(bQuad) - 4 * aQuad * cQuad;
            //Log.i(TAG, "determinant = " + determinant);

            // If determinant is less than zero, equations are unsolvable and there is no solution
            if (determinant < 0) {
                Toast.makeText(this, "Limit reached.", Toast.LENGTH_SHORT).show();
                kill = true;
                return;
            }

            // Calculate the two possible next locations for the free joint
            double y1 = (-1 * bQuad + Math.sqrt(determinant)) / (2 * aQuad);
            double y2 = (-1 * bQuad - Math.sqrt(determinant)) / (2 * aQuad);
            double x1 = u * y1 + v;
            double x2 = u * y2 + v;

            // Find the location that is closest to the current location of the free joint
            double mag1 = getMagnitude(new Point(x1, y1), free);
            //Log.i(TAG, "Magnitude 1 = " + mag1);
            double mag2 = getMagnitude(new Point(x2, y2), free);
            //Log.i(TAG, "Magnitude 2 = " + mag2);

            // Change the location of the free joint to the correct point
            if (mag1 < mag2) {
                Point newPoint = reconvertCoordinates(new Point(x1, y1), origin);
                joint1.setXCoord(newPoint.x);
                joint1.setYCoord(newPoint.y);
            } else {
                Point newPoint = reconvertCoordinates(new Point(x2, y2), origin);
                joint1.setXCoord(newPoint.x);
                joint1.setYCoord(newPoint.y);
            }
        } catch (NullPointerException npe) {
            Log.i(TAG, "no radius saved");
        }

        // Add the newly calculated joint to the complete list
        complete.add(joint1);
       // Log.i(TAG, "Added " + joint1.getJointName() + " to complete");
    }

    private double sqr(double number) {
        return number * number;
    }

    private double getMagnitude(Point point1, Point point2) {
        return Math.sqrt(sqr(point2.x - point1.x) + sqr(point2.y - point1.y));
    }

    private double getAngle(Point point1, Point point2) {
        double dx = point1.x - point2.x;
        double dy = point1.y - point2.y;
        if (dx == 0) {
            if (dy > 0) {
                return Math.PI / 2;
            }
            return 3 * Math.PI / 2;
        }
        double theta = Math.atan(dy / dx);
        if (dx < 0) {
            return Math.PI + theta;
        }
        if (dx > 0 && dy < 0) {
            return 2 * Math.PI + theta;
        }
        return theta;
    }

    private Point convertCoordinates(Point point, Point origin) {
        int yMax = background.getHeight();
        return new Point(point.x - origin.x, yMax - point.y - (yMax - origin.y));
    }

    private Point reconvertCoordinates(Point point, Point origin) {
        int yMax = background.getHeight();
        return new Point(point.x + origin.x, yMax - (point.y + (yMax - origin.y)));
    }

    private Joint getJointbyId(int id) {
        for (Joint joint : mJoints) {
            if (joint.getJointId() == id) {
                return joint;
            }
        }
        return null;
    }

    private Link getLinkById(int id) {
        for (Link link : mLinks) {
            if (link.getLinkId() == id) {
                return link;
            }
        }
        return null;
    }


    private List<Joint> getJointsOnLink(Integer linkID) {
        List<Joint> linkJoints = new ArrayList<>();
        for (Joint joint : mJoints) {
            if (joint.getLink1ID() != null && joint.getLink1ID().equals(linkID)) {
                linkJoints.add(joint);
                //Log.i(TAG, "Adding " + joint.getJointName() + " to " + linkID);
            }
            if (joint.getLink2ID() != null && joint.getLink2ID().equals(linkID)) {
                linkJoints.add(joint);
                //Log.i(TAG, "Adding " + joint.getJointName() + " to " + linkID);
            }
            if (joint.getLink3ID() != null && joint.getLink3ID().equals(linkID)) {
                linkJoints.add(joint);
                //Log.i(TAG, "Adding " + joint.getJointName() + " to " + linkID);
            }
            if (joint.getLink4ID() != null && joint.getLink4ID().equals(linkID)) {
                linkJoints.add(joint);
                //Log.i(TAG, "Adding " + joint.getJointName() + " to " + linkID);
            }
        }

        return linkJoints;
    }


    private Joint checkJointHit(float xTouch, float yTouch) {
        int maxDistance = displayMetrics.widthPixels / 30;
        for (int i = 0; i < mJoints.size(); i++) {
            Log.i(TAG, "SelectEditJoint: Touch at (" + xTouch + "," + yTouch + ")");
            if (Math.abs(mJoints.get(i).getXCoord() - xTouch) < maxDistance && Math.abs(mJoints.get(i).getYCoord() - yTouch) < maxDistance) {
                Log.i(TAG, "Index actually " + i);
                return mJoints.get(i);
            }
        }
        return null;
    }

    private Point getOtherEndpoint(Integer linkID, Integer jointID) {
        Point endpoint;
        try {
            Link link = getLinkById(linkID);
            Integer joint1ID = link.getEndpoint1();
            Joint otherEndpoint;
            if (jointID == joint1ID) {
                Integer joint2ID = link.getEndpoint2();
                otherEndpoint = getJointbyId(joint2ID);
            } else {
                otherEndpoint = getJointbyId(joint1ID);
            }
            endpoint = convertCoordinates(new Point(otherEndpoint.getXCoord(), otherEndpoint.getYCoord()), origin);
        } catch (NullPointerException npe) {
            return null;
        }
        return endpoint;
    }
}