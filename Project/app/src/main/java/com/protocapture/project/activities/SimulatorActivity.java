package com.protocapture.project.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
    private Prototype mPrototype;
    private List<Link> mLinks;
    private List<Joint> mJoints;
    private Button okButton;
    private Joint editJoint = null;
    private Point origin;
    private ArrayList<Joint> complete = new ArrayList<>();
    private HashMap<String, Double> radii = new HashMap<>();

    private Bitmap backgroundBitmap;
    private Canvas background;
    private Paint linksPaint;
    private Paint pathPaint;
    private Paint jointsPaint;

    private boolean selectDriver = false;
    //private boolean selectDrawer = false;
    private boolean selectEditJoint = false;
    private Integer motorIndex = -1;
    private boolean kill = false;

    private final ArrayList<Point> points = new ArrayList<>();
    private ArrayList<Pair<Point, Point>> drawLines = new ArrayList<>();

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
        myToolbar.setTitle(prototypeName);

        okButton = (Button) findViewById(R.id.button_ok);
        okButton.bringToFront();

        // Set up Paint to draw the motion profile
        pathPaint = new Paint();
        pathPaint.setAntiAlias(true);
        pathPaint.setColor(Color.MAGENTA);
        pathPaint.setStrokeWidth(4);

        // Set up Paint to draw the joints (connecting points)
        jointsPaint = new Paint();
        jointsPaint.setAntiAlias(true);
        jointsPaint.setColor(Color.BLACK);
        jointsPaint.setStyle(Paint.Style.FILL);

        // Set up Paint to draw the links (lines)
        linksPaint = new Paint();
        linksPaint.setAntiAlias(true);
        linksPaint.setColor(Color.BLACK);
        linksPaint.setStrokeWidth(4);

        mJointViewModel = new ViewModelProvider(this).get(JointViewModel.class);
        mLinkViewModel = new ViewModelProvider(this).get(LinkViewModel.class);
        mPrototypeViewModel = new ViewModelProvider(this).get(PrototypeViewModel.class);
        mPrototypeViewModel.getPrototype(prototypeName).observe(this, new Observer<Prototype>() {
            @Override
            public void onChanged(@Nullable final Prototype prototype) {

                mPrototype = prototype;
                Bitmap bitmap = null;
                try {
                    String filename = mPrototype.getPrototypeBitmap();
                    Log.i(TAG, filename);

                    FileInputStream input = new FileInputStream(new File(filename));
                    if(input.available() != 0) {
                        bitmap = BitmapFactory.decodeStream(input);
                    }
                    input.close();
                } catch (Exception e) {
                    Log.e(TAG, "File input Error: ", e);
                }

                backgroundBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                bitmap.recycle();
                background = new Canvas(backgroundBitmap);

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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_edit_joint) {
            //selectDrawer = false;
            selectDriver = false;
            selectEditJoint = true;
            Toast.makeText(SimulatorActivity.this, "Select joint to edit.", Toast.LENGTH_LONG).show();
            okButton.setText("Edit");
            okButton.setVisibility(View.VISIBLE);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(editJoint == null) {
                        Toast.makeText(SimulatorActivity.this, "Please select a joint to edit.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Intent intent = new Intent(SimulatorActivity.this, JointEditorActivity.class);
                    intent.putExtra("joint_id", editJoint.getJointId());
                    startActivityForResult(intent, JOINT_EDITOR_REQUEST_CODE);
                }
            });

        } else if (id == R.id.action_view_sim) {
            //selectDrawer = false;
            selectEditJoint = false;
            selectDriver = true;
            Toast.makeText(SimulatorActivity.this, "Select motor location.", Toast.LENGTH_LONG).show();
            okButton.setText("Animate");
            okButton.setVisibility(View.VISIBLE);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(motorIndex == -1) {
                        Toast.makeText(SimulatorActivity.this, "Please select motor location.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    animateMechanism(mSimulatorView);
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i(TAG, "Touch");
        if (event.getAction() != MotionEvent.ACTION_DOWN && event.getAction() != MotionEvent.ACTION_POINTER_DOWN) {
            //Log.i(TAG, "MainActivity.onTouch: Leavin on a jetplane.");
            return true;
        }

        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        float xTouch = (event.getX() / displayMetrics.widthPixels) * background.getWidth();
        float yTouch = (event.getY() / displayMetrics.heightPixels) * background.getHeight();

        int maxDistance = displayMetrics.widthPixels/30;
        if(selectDriver) {
            Log.i(TAG, "Waiting for motor selection...");
            Log.i(TAG, "x = " + xTouch + ", y = " + yTouch);
            Joint motor = checkJointHit(xTouch, yTouch);
            if(motor == null) {
                return true;
            } else if (motor.getConstraint() != Joint.FIXED) {
                Toast.makeText(this, "The motor location must be a fixed joint.", Toast.LENGTH_LONG).show();
            } else {
                //fillPoints(driver);
                motorIndex = mJoints.indexOf(motor);
                Toast.makeText(this, "Ready to animate!", Toast.LENGTH_LONG).show();
                selectDriver = false;
                //selectEditJoint = false;
                //selectDrawer = true;
                //Toast.makeText(this, "Select the focus point.", Toast.LENGTH_LONG).show();
            }
        /*} else if(selectDrawer) {
            for (int i = 0; i < points.size(); i++) {
                Log.i(TAG, "SelectDrawer: Touch at (" + xTouch + "," + yTouch + ")");
                if (Math.abs(points.get(i).x - xTouch) < maxDistance && Math.abs(points.get(i).y - yTouch) < maxDistance) {
                    focusIndex = i;
                    Toast.makeText(this, "Ready to animate!", Toast.LENGTH_LONG).show();
                    selectDrawer = false;
                    return true;
                }
            }*/
        } else if(selectEditJoint) {
            editJoint = checkJointHit(xTouch, yTouch);
            if(editJoint == null) {
                return true;
            }
            Toast.makeText(this, "Edit " + editJoint.getJointName() + "?", Toast.LENGTH_LONG).show();
            selectEditJoint = false;
        }
        return super.onGenericMotionEvent(event);
    }


    @Override
    protected void onStop() {
        super.onStop();
        mSimulatorView.stopThread();
    }


    private void drawFrame(List<Joint> drawingPoints) {

        Log.i(TAG, "drawing new frame");

        Bitmap drawableBitmap = backgroundBitmap.copy(backgroundBitmap.getConfig(), true);
        Canvas drawable = new Canvas(drawableBitmap);

        // Draw links
        for(Link link: mLinks) {
            Joint joint1 = getJointbyId(link.getEndpoint1());
            Joint joint2 = getJointbyId(link.getEndpoint2());
            if(joint1 != null && joint2 != null) {
                Point endpoint1 = new Point(joint1.getXCoord(), joint1.getYCoord());
                Point endpoint2 = new Point(joint2.getXCoord(), joint2.getYCoord());

                drawable.drawLine((float) endpoint1.x, (float) endpoint1.y, (float) endpoint2.x, (float) endpoint2.y, linksPaint);
            }
        }

        // Draw joints & motion profiles
        for(Joint joint: drawingPoints) {
            //Log.i(TAG, "Point at: (" + point.x + "," + point.y + ")");
            double x = joint.getXCoord();
            double y = joint.getYCoord();
            drawable.drawCircle((float) x, (float) y, 8, jointsPaint);
            background.drawCircle((float) x, (float) y, 2, pathPaint);
        }

        mSimulatorView.drawBitmap(drawableBitmap);
    }


    public void animateMechanism(View view) {

        // Initialise variables
        complete.clear();
        origin = points.get(motorIndex);
        Joint motor = mJoints.get(motorIndex);
        Point motorPoint = convertCoordinates(new Point(motor.getXCoord(), motor.getYCoord()), origin);

        // Add distances between joints (radii for later calculations)
        for(Joint joint1: mJoints) {
            for(Joint joint2: mJoints) {
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
        if(driveLink1ID != null) {
            Point endpoint1 = getOtherEndpoint(driveLink1ID, motor.getJointId());
            theta1 = getAngle(endpoint1, motorPoint);
            Log.i(TAG, "Theta1 = " + theta1);
        }
        Integer driveLink2ID = motor.getLink2ID();
        if(driveLink2ID != null) {
            Point endpoint2 = getOtherEndpoint(driveLink2ID, motor.getJointId());
            theta2 = getAngle(endpoint2, motorPoint);
            Log.i(TAG, "Theta2 = " + theta2);
        }

        // Add all FIXED joints to the "complete" list (their position is fixed)
        for(double i = 0; i <= 2 * Math.PI; i = i + 0.05) {
            for(Joint joint: mJoints) {
                if(joint.getConstraint() == Joint.FIXED) {
                    complete.add(joint);
                    Log.i(TAG, "adding joint " + joint.getJointName() + " to fixed joints");
                }
            }
            for(Joint joint: complete) {
                Log.i(TAG, "complete contains: " + joint.getJointName());
            }

            // Simulate links driven directly by motor
            if(driveLink1ID != null) {
                List<Joint> driveLink1Joints = getJointsOnLink(driveLink1ID);
                for (Joint joint : driveLink1Joints) {
                    if (!complete.contains(joint)) {
                        Point calcPoint = convertCoordinates(new Point(joint.getXCoord(), joint.getYCoord()), origin);
                        double radius = getMagnitude(calcPoint, motorPoint);
                        Point drawPoint = reconvertCoordinates(new Point(radius * Math.cos(theta1 + i), radius * Math.sin(theta1 + i)), origin);
                        joint.setXCoord(drawPoint.x);
                        joint.setYCoord(drawPoint.y);
                        complete.add(joint);
                    }
                }
            }
            if(driveLink2ID != null) {
                List<Joint> driveLink2Joints = getJointsOnLink(driveLink2ID);
                for (Joint joint : driveLink2Joints) {
                    if (!complete.contains(joint)) {
                        Point calcPoint = convertCoordinates(new Point(joint.getXCoord(), joint.getYCoord()), origin);
                        double radius = getMagnitude(calcPoint, motorPoint);
                        Point drawPoint = reconvertCoordinates(new Point(radius * Math.cos(theta2 + i), radius * Math.sin(theta2 + i)), origin);
                        joint.setXCoord(drawPoint.x);
                        joint.setYCoord(drawPoint.y);
                        complete.add(joint);
                    }
                }
            }

            // Iterate to finish simulation
            iterate(1);
            if(kill) {
                return;
            }

            // Once all locations have been calculated, draw the step then start over
            drawFrame(complete);
            complete.clear();
        }
    }

    private void iterate(int index) {
        // Get the next joint from the "complete" list (location has been calculated)
        Joint next = complete.get(index);
        Log.i(TAG, "complete " + index + " is joint " + next.getJointName());

        // Check the first link for a new point to calculate
        Integer link1id = next.getLink1ID();
        if(link1id != null) {
            Log.i(TAG, "Trying link 1 of joint " + next.getJointName());
            simulate(next, link1id);
        }
        if(kill) {
            return;
        }

        // Check the second link for a new point to calculate
        Integer link2id = next.getLink2ID();
        if(link2id != null) {
            Log.i(TAG, "Trying link 2 of joint " + next.getJointName());
            simulate(next, link2id);
        }
        if(kill) {
            return;
        }

        // Repeat with next joint
        index++;
        if(index < complete.size()) {
            iterate(index);
        }
    }

    private void simulate(Joint joint0, int linkid) {

        // Get all the joints on the selected link
        List<Joint> link1Joints = getJointsOnLink(linkid);
        if(link1Joints.isEmpty()) {
            return;
        }

        // Look for any joints that are still "free" (location not calculated yet)
        for(Joint joint1: link1Joints) {
            if(!complete.contains(joint1))  {

                // Once a free joint has been located...
                Log.i(TAG, "Found free joint " + joint1.getJointName());

                // Check its first link for another fixed point (location already calculated)
                Integer link1id = joint1.getLink1ID();
                if(link1id != null) {
                    Log.i(TAG, "Trying link 1 of joint " + joint1.getJointName());
                    completeSimulation(joint0, joint1, link1id);
                }
                if(kill) {
                    return;
                }

                // Check its second link for another fixed point (Location already calculated)
                Integer link2id = joint1.getLink2ID();
                if(link2id != null) {
                    Log.i(TAG, "Trying link2 of joint " + joint1.getJointName());
                    completeSimulation(joint0, joint1, link2id);
                }
            }
        }
    }

    private void completeSimulation(Joint joint0, Joint joint1, int linkid) {
        // If the location of this joint has already been calculated, return (could happen on second link)
        if(complete.contains(joint1)) {
            return;
        }

        // Look for second fixed point on link
        Joint joint2 = null;
        List<Joint> link2Joints = getJointsOnLink(linkid);
        if(link2Joints.isEmpty()) {
            return;
        }
        for(Joint joint: link2Joints) {
            if (complete.contains(joint) && !joint.equals(joint0)) {
                joint2 = joint;
                Log.i(TAG, "Found second fixed joint: " + joint2.getJointName());
            }
        }
        if(joint2 == null) {
            return;
        }

        // Calculate free joints location using intersection of two circles method
        try {

            // Convert all points into a coordinate system that's easy to deal with
            Point fixed1 = convertCoordinates(new Point(joint0.getXCoord(), joint0.getYCoord()), origin);
            Log.i(TAG, "Fixed1 = " + joint0.getJointName());
            Point free = convertCoordinates(new Point(joint1.getXCoord(), joint1.getYCoord()), origin);
            Log.i(TAG, "Free = " + joint1.getJointName());
            Point fixed2 = convertCoordinates(new Point(joint2.getXCoord(), joint2.getYCoord()), origin);
            Log.i(TAG, "Fixed2 = " + joint2.getJointName());

            // Get radii from list
            double radius1 = radii.get(joint0.getJointId() + "to" + joint1.getJointId());
            double radius2 = radii.get(joint2.getJointId() + "to" + joint1.getJointId());

            // Lots of weird math to locate circle intersections
            double u = -1 * (fixed1.y - fixed2.y) / (fixed1.x - fixed2.x);
            double v = (sqr(radius2) - sqr(radius1) + sqr(fixed1.x) - sqr(fixed2.x) + sqr(fixed1.y) - sqr(fixed2.y)) / (2 * (fixed1.x - fixed2.x));
            double aQuad = sqr(u) + 1;
            double bQuad = 2 * u * v - 2 * fixed1.x * u - 2 * fixed1.y;
            double cQuad = sqr(v) - 2 * fixed1.x * v + sqr(fixed1.x) + sqr(fixed1.y) - sqr(radius1);
            double determinant = sqr(bQuad) - 4 * aQuad * cQuad;

            // If determinant is less than zero, equations are unsolvable and there is no solution
            if (determinant < 0) {
                Toast.makeText(this, "Impossible to complete simulation.", Toast.LENGTH_LONG).show();
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
            Log.i(TAG, "Magnitude 1 = " + mag1);
            double mag2 = getMagnitude(new Point(x2, y2), free);
            Log.i(TAG, "Magnitude 2 = " + mag2);

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
        } catch(NullPointerException npe) {
            Log.i(TAG, "no radius saved");
        }

        // Add the newly calculated joint to the complete list
        complete.add(joint1);
        Log.i(TAG, "Added " + joint1.getJointName() + " to complete");
    }

    private double sqr(double number) {
        return number * number;
    }

    private double getMagnitude(Point point1, Point point2) {
        return Math.sqrt(sqr(point2.x - point1.x) + sqr(point2.y - point1.y));
    }

    private double getAngle(Point point1, Point point2) {
        return Math.atan((point2.y - point1.y) / (point2.x - point1.x));
    }

    private Point convertCoordinates (Point point, Point origin) {
        int yMax = background.getHeight();
        return new Point(point.x - origin.x, yMax - point.y - (yMax - origin.y));
    }

    private Point reconvertCoordinates (Point point, Point origin) {
        int yMax = background.getHeight();
        return new Point(point.x + origin.x, yMax - (point.y + (yMax - origin.y)));
    }

    private Joint getJointbyId(int id) {
        for(Joint joint: mJoints) {
            if(joint.getJointId() == id) {
                return joint;
            }
        }
        return null;
    }

    private Link getLinkById(int id) {
        for(Link link: mLinks) {
            if(link.getLinkId() == id) {
                return link;
            }
        }
        return null;
    }


    private List<Joint> getJointsOnLink(Integer linkID) {
        List<Joint> linkJoints = new ArrayList<>();
        for(Joint joint: mJoints) {
            if(joint.getLink1ID() != null && joint.getLink1ID().equals(linkID)) {
                linkJoints.add(joint);
                Log.i(TAG, "Adding " + joint.getJointName() + " to " + linkID);
            }
            if(joint.getLink2ID() != null && joint.getLink2ID().equals(linkID)) {
                linkJoints.add(joint);
                Log.i(TAG, "Adding " + joint.getJointName() + " to " + linkID);
            }
        }

        return linkJoints;
    }


    private Joint checkJointHit(float xTouch, float yTouch) {
        int maxDistance = displayMetrics.widthPixels/30;
        for(Joint joint: mJoints) {
            Log.i(TAG, "SelectEditJoint: Touch at (" + xTouch + "," + yTouch + ")");
            if (Math.abs(joint.getXCoord()- xTouch) < maxDistance && Math.abs(joint.getYCoord() - yTouch) < maxDistance) {
                return joint;
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

    private Link checkLinkHit(float xTouch, float yTouch) {
        Log.i(TAG, "Looking for a match...");
        for(Link link: mLinks) {
            Integer joint1_id = link.getEndpoint1();
            Integer joint2_id = link.getEndpoint2();
            Joint point1 = getJointbyId(joint1_id);
            Joint point2 = getJointbyId(joint2_id);

            if(point1 == null || point2 == null) {
                return null;
            }

            // https://stackoverflow.com/questions/24335866/how-to-create-clickable-lines-in-android
            int maxDistance = 4000;
            double distFromLink = (point2.getXCoord() - point1.getXCoord()) * (yTouch - point1.getYCoord()) -
                    (point2.getYCoord() - point1.getYCoord()) * (xTouch - point1.getXCoord());
            Log.i(TAG, "distance(ish) = " + distFromLink);
            if (distFromLink < maxDistance && distFromLink > -1 * maxDistance) {
                Log.i(TAG, "FOUND MATCH!");
                return link;
            }
        }
        return null;
    }



    private void fillPoints(Link driver) {

        Log.i(TAG, "driver id = " + driver.getLinkId());
        if(points.size() == mJoints.size()) {
            Log.i(TAG, "Exiting.");
            return;
        }

        int joint1_id = driver.getEndpoint1();
        int joint2_id = driver.getEndpoint2();
        Joint point1 = getJointbyId(joint1_id);
        Joint point2 = getJointbyId(joint2_id);

        if(points.isEmpty()) {
            Log.i(TAG, "setting first point");
            if (point1.getConstraint() == Joint.FIXED) {
                // if(point2.getConstraint() == Joint.FIXED) {
                // Toast.makeText(this, "Please choose a non-static link as the driver.", Toast.LENGTH_LONG).show();
                // }
                points.add(new Point(point1.getXCoord(), point1.getYCoord()));
                Log.i(TAG, "1a: There is " + points.size() + " points");
            } else if(point2.getConstraint() == Joint.FIXED) {
                points.add(new Point(point2.getXCoord(), point2.getYCoord()));
                Log.i(TAG, "1b: There is " + points.size() + " points");
            } else {
                Toast.makeText(this, "The driving link should have at least 1 fixed joint.", Toast.LENGTH_LONG).show();
                return;
            }
        }

        Link nextLink;
        if(point1.getXCoord() == points.get(points.size() - 1).x && point1.getYCoord() == points.get(points.size() - 1).y) {
            points.add(new Point(point2.getXCoord(), point2.getYCoord()));
            Log.i(TAG, "2a: There are " + points.size() + " points");
            Log.i(TAG, "Link1 id = " + point2.getLink1ID());
            Log.i(TAG, "Link2 id = " + point2.getLink2ID());
            if(point2.getLink1ID().equals(driver.getLinkId())) {
                Log.i(TAG, "FLAG D");
                int nextLinkId = point2.getLink2ID();
                nextLink = getLinkById(nextLinkId);
                fillPoints(nextLink);
            } else if(point2.getLink2ID().equals(driver.getLinkId())) {
                Log.i(TAG, "FLAG E");
                int nextLinkId = point2.getLink1ID();
                nextLink = getLinkById(nextLinkId);
                fillPoints(nextLink);
            } else {
                Log.i(TAG, "FLAG F");
                return;
            }
        } else if(point2.getXCoord() == points.get(points.size() - 1).x && point2.getYCoord() == points.get(points.size() - 1).y) {
            points.add(new Point(point1.getXCoord(), point1.getYCoord()));
            Log.i(TAG, "2b: There are " + points.size() + " points");
            Log.i(TAG, "Link1 id = " + point1.getLink1ID());
            Log.i(TAG, "Link2 id = " + point1.getLink2ID());
            if(point1.getLink1ID().equals(driver.getLinkId())) {
                Log.i(TAG, "FLAG H");
                int nextLinkId = point1.getLink2ID();
                nextLink = getLinkById(nextLinkId);
                fillPoints(nextLink);
            } else if(point1.getLink2ID().equals(driver.getLinkId())) {
                Log.i(TAG, "FLAG I");
                int nextLinkId = point1.getLink1ID();
                nextLink = getLinkById(nextLinkId);
                fillPoints(nextLink);
            } else {
                Log.i(TAG, "FLAG J");
                return;
            }
        }
    }


}
