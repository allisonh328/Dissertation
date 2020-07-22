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
import android.view.MotionEvent;
import android.view.View;
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
import java.util.List;

public class SimulatorActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.example.project.activity.ViewPrototypeActivity.MESSAGE";
    private final static String TAG = "ALLISON";
    private SimulatorView mSimulatorView;
    private PrototypeViewModel mPrototypeViewModel;
    private LinkViewModel mLinkViewModel;
    private JointViewModel mJointViewModel;
    private Prototype mPrototype;
    private List<Link> mLinks;
    private List<Joint> mJoints;

    Bitmap backgroundBitmap;
    Canvas background;
    Paint linksPaint;
    Paint pathPaint;
    Paint jointsPaint;

    private boolean selectDriver = false;
    private boolean selectDrawer = false;
    private int focusIndex = -1;

    private final ArrayList<Point> points = new ArrayList<>();

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

        // Set up Paint to draw the motion profile
        pathPaint = new Paint();
        pathPaint.setAntiAlias(true);
        pathPaint.setColor(Color.GREEN);
        pathPaint.setStrokeWidth(4);

        // Set up Paint to draw the joints (connecting points)
        jointsPaint = new Paint();
        jointsPaint.setAntiAlias(true);
        jointsPaint.setColor(Color.MAGENTA);
        jointsPaint.setStyle(Paint.Style.FILL);

        // Set up Paint to draw the links (lines)
        linksPaint = new Paint();
        linksPaint.setAntiAlias(true);
        linksPaint.setColor(Color.MAGENTA);
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
                    }
                });

                mJointViewModel.getAllProtoJoints(mPrototype.getPrototypeId()).observe(SimulatorActivity.this, new Observer<List<Joint>>() {
                    @Override
                    public void onChanged(@Nullable final List<Joint> joints) {
                        mJoints = joints;
                        ArrayList<Point> initPoints = new ArrayList<>();
                        for(Joint joint: mJoints) {
                            initPoints.add(new Point(joint.getXCoord(), joint.getYCoord()));
                        }
                        drawFrame(initPoints);
                        selectDriver = true;
                        Toast.makeText(SimulatorActivity.this, "Select driving link.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void drawFrame(ArrayList<Point> drawingPoints) {

        Log.i(TAG, "drawing new frame");

        Bitmap drawableBitmap = backgroundBitmap.copy(backgroundBitmap.getConfig(), true);
        Canvas drawable = new Canvas(drawableBitmap);

        if(focusIndex != -1) {
            background.drawCircle((float) drawingPoints.get(focusIndex).x, (float) drawingPoints.get(focusIndex).y, 2, pathPaint);

            drawable.drawLine((float) drawingPoints.get(0).x, (float) drawingPoints.get(0).y, (float) drawingPoints.get(1).x, (float) drawingPoints.get(1).y, linksPaint);
            drawable.drawLine((float) drawingPoints.get(1).x, (float) drawingPoints.get(1).y, (float) drawingPoints.get(2).x, (float) drawingPoints.get(2).y, linksPaint);
            drawable.drawLine((float) drawingPoints.get(2).x, (float) drawingPoints.get(2).y, (float) drawingPoints.get(3).x, (float) drawingPoints.get(3).y, linksPaint);
            drawable.drawLine((float) drawingPoints.get(3).x, (float) drawingPoints.get(3).y, (float) drawingPoints.get(0).x, (float) drawingPoints.get(0).y, linksPaint);
        }

        for(Point point: drawingPoints) {
            Log.i(TAG, "Point at: (" + point.x + "," + point.y + ")");
            drawable.drawCircle((float) point.x, (float) point.y, 8, jointsPaint);
        }

        mSimulatorView.drawBitmap(drawableBitmap);
    }


    public void animateMechanism(View view) {

        if(focusIndex == -1 || points.isEmpty()) {
            Toast.makeText(this, "Please select driving link and focus point.", Toast.LENGTH_LONG).show();
        }
        ArrayList<Point> workingPoints = convertCoordinates(points.get(0));

        assert getMagnitude(new Point(0,0), new Point(0, 4)) == 4;
        assert getMagnitude(new Point(0, 0), new Point(4, 0)) == 4;
        assert getMagnitude(new Point(0, 0), new Point(3, 4)) == 5;
        double a = getMagnitude(workingPoints.get(0), workingPoints.get(1));
        double b = getMagnitude(workingPoints.get(1), workingPoints.get(2));
        double c = getMagnitude(workingPoints.get(2), workingPoints.get(3));
        double d = getMagnitude(workingPoints.get(3), workingPoints.get(0));

        double gamma = getAngle(workingPoints.get(0), workingPoints.get(3));
        Log.i(TAG, "gamma = " + gamma);
        double thetaStart = getAngle(workingPoints.get(0), workingPoints.get(1)) - gamma;
        Log.i(TAG, "thetaStart = " + thetaStart);

        for(double theta = thetaStart; theta < thetaStart + 2 * Math.PI; theta = theta + 0.05) {
            double e = Math.sqrt(a * a + d * d - 2 * a * d * Math.cos(theta));
            double alpha = Math.asin((a * Math.sin(theta)) / e);
            double beta = Math.acos((e * e + c * c - b * b) / (2 * e * c));
            double phi = alpha + beta;

            Point A = workingPoints.get(0);
            Point D = workingPoints.get(3);
            Point B = new Point(a * Math.cos(theta + gamma), a * Math.sin(theta + gamma));
            Point C;

            double u = -1 * (B.y - D.y) / (B.x - D.x);
            double v = (c * c - b * b + B.x * B.x - D.x * D.x + B.y * B.y - D.y * D.y) / (2 * (B.x - D.x));
            double aQuad = u * u + 1;
            double bQuad = 2 * u * v - 2 * B.x * u - 2 * B.y;
            double cQuad = v * v - 2* B.x * v + B.x * B.x + B.y * B.y - b * b;
            double determinant = bQuad * bQuad - 4 * aQuad * cQuad;

            if(determinant >= 0) {
                double y1 = (-1 * bQuad + Math.sqrt(determinant)) / (2 * aQuad);
                double y2 = (-1 * bQuad - Math.sqrt(determinant)) / (2 * aQuad);
                double x1 = u * y1 + v;
                double x2 = u * y2 + v;
                double mag1 = getMagnitude(new Point(x1, y1), workingPoints.get(2));
                double mag2 = getMagnitude(new Point(x2, y2), workingPoints.get(2));

                if (mag1 < mag2) {
                    C = new Point(x1, y1);
                } else {
                    C = new Point(x2, y2);
                }

                try {
                    Thread.sleep(40);
                } catch (InterruptedException ie) {
                    return;
                }

                ArrayList<Point> newPoints = new ArrayList<>(Arrays.asList(A, B, C, D));
                ArrayList<Point> drawingPoints = reconvertCoordinates(newPoints, points.get(0));
                drawFrame(drawingPoints);
            } else {
                Toast.makeText(this, "Simulation Failed.", Toast.LENGTH_LONG).show();
                return;
            }
        }
    }

    private double getMagnitude(Point point1, Point point2) {

        return Math.sqrt((point2.x - point1.x) * (point2.x - point1.x) + (point2.y - point1.y) * (point2.y - point1.y));
    }

    private double getAngle(Point point1, Point point2) {
        Log.i(TAG, "In getAngle");
        Pair<Double, Double> horizontal = new Pair(1.0, 0.0); // First vector is a unit horizontal vector
        Pair<Double, Double> vect2 = new Pair(point2.x - point1.x, point2.y - point1.y); // Second vector the vector between the given points

        // dot product = a1 * b1 + a2 * b2 + ... (a1 = 1 and a2 = 0 in this case)
        double dotProduct = horizontal.first * vect2.first + horizontal.second * vect2.second;
        double magnitude = getMagnitude(point1, point2);

        // theta = cos-1(dot product of vectors/ product of vector magnitudes)
        return Math.acos(dotProduct / magnitude);
    }

    private ArrayList<Point> convertCoordinates (Point origin) {
        int yMax = background.getHeight();
        ArrayList<Point> workingPoints = new ArrayList<>();

        for(Point point: points) {
            workingPoints.add(new Point(point.x - origin.x, yMax - point.y - (yMax - origin.y)));
        }
        return workingPoints;
    }

    private ArrayList<Point> reconvertCoordinates (ArrayList<Point> pointList, Point origin) {
        int yMax = background.getHeight();
        ArrayList<Point> drawingPoints = new ArrayList<>();

        for(Point point: pointList) {
            drawingPoints.add(new Point(point.x + origin.x, yMax - (point.y + (yMax - origin.y))));
        }
        return drawingPoints;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i(TAG, "Touch");
        if (event.getAction() != MotionEvent.ACTION_DOWN && event.getAction() != MotionEvent.ACTION_POINTER_DOWN) {
            //Log.i(TAG, "MainActivity.onTouch: Leavin on a jetplane.");
            return true;
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        float xTouch = (event.getX() / displayMetrics.widthPixels) * background.getWidth();
        float yTouch = (event.getY() / displayMetrics.heightPixels) * background.getHeight();

        if(selectDriver) {
            Log.i(TAG, "Waiting for link selection...");
            Log.i(TAG, "x = " + xTouch + ", y = " + yTouch);
            Link driver = checkLinkHit(xTouch, yTouch);
            if(driver == null) {
                return true;
            } else {
                fillPoints(driver);
                selectDriver = false;
                selectDrawer = true;
                Toast.makeText(this, "Select the focus point.", Toast.LENGTH_LONG).show();
            }
        } else if(selectDrawer) {
            int maxDistance = 50;
            for (int i = 0; i < points.size(); i++) {
                Log.i(TAG, "MainActivity.onTouch: Touch at (" + xTouch + "," + yTouch + ")");
                if (Math.abs(points.get(i).x - xTouch) < maxDistance && Math.abs(points.get(i).y - yTouch) < maxDistance) {
                    focusIndex = i;
                    Toast.makeText(this, "Ready to animate!", Toast.LENGTH_LONG).show();
                    selectDrawer = false;
                    return true;
                }
            }
        }
        return super.onGenericMotionEvent(event);
    }

    private Link checkLinkHit(float xTouch, float yTouch) {
        Log.i(TAG, "Looking for a match...");
        for(Link link: mLinks) {
            int joint1_id = link.getEndpoint1();
            int joint2_id = link.getEndpoint2();
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

    @Override
    protected void onStop() {
        super.onStop();
        mSimulatorView.stopThread();
    }
}
