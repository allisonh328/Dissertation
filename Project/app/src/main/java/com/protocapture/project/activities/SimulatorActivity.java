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
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;

import com.protocapture.project.ComponentCollectionFragment;
import com.protocapture.project.R;
import com.protocapture.project.SimulatorView;
import com.protocapture.project.database.Joint;
import com.protocapture.project.database.JointViewModel;
import com.protocapture.project.database.LinkViewModel;
import com.protocapture.project.database.Prototype;
import com.protocapture.project.database.PrototypeViewModel;

import org.opencv.core.Point;

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

    Bitmap backgroundBitmap;
    Canvas background;
    Paint linksPaint;
    Paint pathPaint;
    Paint jointsPaint;

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
        jointsPaint.setColor(Color.RED);
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

                fillPoints();
            }
        });
    }

    private void fillPoints() {
        mJointViewModel.getAllProtoJoints(mPrototype.getPrototypeId()).observe(this, new Observer<List<Joint>>() {
            @Override
            public void onChanged(@Nullable final List<Joint> joints) {
                for(Joint joint: joints) {
                    points.add(new Point(joint.getXCoord(), joint.getYCoord()));
                }
                drawFrame(points);
            }
        });
    }

    private void drawFrame(ArrayList<Point> drawingPoints) {

        background.drawCircle((float) drawingPoints.get(2).x, (float) drawingPoints.get(2).y, 2, pathPaint);

        Log.i(TAG, "drawing new frame");

        Bitmap drawableBitmap = backgroundBitmap.copy(backgroundBitmap.getConfig(), true);
        Canvas drawable = new Canvas(drawableBitmap);

        for(Point point: drawingPoints) {
            Log.i(TAG, "Point at: (" + point.x + "," + point.y + ")");
            drawable.drawCircle((float) point.x, (float) point.y, 8, jointsPaint);
        }

        drawable.drawLine((float) drawingPoints.get(0).x, (float) drawingPoints.get(0).y, (float) drawingPoints.get(1).x, (float) drawingPoints.get(1).y, linksPaint);
        drawable.drawLine((float) drawingPoints.get(1).x, (float) drawingPoints.get(1).y, (float) drawingPoints.get(2).x, (float) drawingPoints.get(2).y, linksPaint);
        drawable.drawLine((float) drawingPoints.get(2).x, (float) drawingPoints.get(2).y, (float) drawingPoints.get(3).x, (float) drawingPoints.get(3).y, linksPaint);
        drawable.drawLine((float) drawingPoints.get(3).x, (float) drawingPoints.get(3).y, (float) drawingPoints.get(0).x, (float) drawingPoints.get(0).y, linksPaint);

        mSimulatorView.drawBitmap(drawableBitmap);
    }


    public void animateMechanism(View view) {

        double gamma;
        double thetaStart;

        ArrayList<Point> workingPoints = convertCoordinates(points.get(0));

        assert getMagnitude(new Point(0,0), new Point(0, 4)) == 4;
        assert getMagnitude(new Point(0, 0), new Point(4, 0)) == 4;
        assert getMagnitude(new Point(0, 0), new Point(3, 4)) == 5;
        double a = getMagnitude(workingPoints.get(0), workingPoints.get(1));
        double b = getMagnitude(workingPoints.get(1), workingPoints.get(2));
        double c = getMagnitude(workingPoints.get(2), workingPoints.get(3));
        double d = getMagnitude(workingPoints.get(3), workingPoints.get(0));

        gamma = getAngle(workingPoints.get(0), workingPoints.get(3));
        thetaStart = getAngle(workingPoints.get(0), workingPoints.get(1)) - gamma;

        for(double theta = thetaStart; theta < thetaStart + 2 * Math.PI; theta = theta + 0.05) {
            double e = Math.sqrt(a * a + d * d - 2 * a * d * Math.cos(theta));
            double alpha = Math.asin((a * Math.sin(theta)) / e);
            double beta = Math.acos((e * e + c * c - b * b) / (2 * e * c));
            double phi = alpha + beta;

            Point A = workingPoints.get(0);
            Point D = workingPoints.get(0);
            Point B = new Point(a * Math.cos(theta + gamma), a * Math.sin(theta + gamma));
            Point C;

            double u = -1 * (B.y - D.y) / (B.x - D.x);
            double v = (c * c - b * b + B.x * B.x - D.x * D.x + B.y * B.y - D.y * D.y) / (2 * (B.x - D.x));
            double aQuad = u * u + 1;
            double bQuad = 2 * u * v - 2 * B.x * u - 2 * B.y;
            double cQuad = v * v - 2* B.x * v + B.x * B.x + B.y * B.y - b * b;

            double y1 = (-1 * bQuad + Math.sqrt(bQuad * bQuad - 4 * aQuad * cQuad)) / (2 * aQuad);
            double y2 = (-1 * bQuad - Math.sqrt(bQuad * bQuad - 4 * aQuad * cQuad)) / (2 * aQuad);
            double x1 = u * y1 + v;
            double x2 = u * y2 + v;
            double mag1 = getMagnitude(new Point(x1, y1), workingPoints.get(2));
            double mag2 = getMagnitude(new Point(x2, y2), workingPoints.get(2));

            if(mag1 < mag2) {
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
        }
    }

    private double getMagnitude(Point point1, Point point2) {

        return Math.sqrt((point2.x - point1.x) * (point2.x - point1.x) + (point2.y - point1.y) * (point2.y - point1.y));
    }

    private double getAngle(Point point1, Point point2) {
        Log.i(TAG, "In getAngle");
        Pair<Double, Double> horizontal = new Pair(1.0, 0.0); // First vector is a unit horizontal vector
        Pair<Double, Double> vect2 = new Pair(point2.y - point1.y, point2.x - point1.y); // Second vector the vector between the given points

        Log.i(TAG, "FLAG A");
        // dot product = a1 * b1 + a2 * b2 + ... (a1 = 1 and a2 = 0 in this case)
        double dotProduct = horizontal.first * vect2.first + horizontal.second * vect2.second;
        Log.i(TAG, "FLAG B");
        double magnitude = getMagnitude(point1, point2);

        Log.i(TAG, "FLAG C");
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
    protected void onStop() {
        super.onStop();
        mSimulatorView.stopThread();
    }
}
