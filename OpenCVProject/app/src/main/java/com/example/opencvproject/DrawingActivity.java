package com.example.opencvproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.core.Point;

import java.io.File;
import java.io.FileInputStream;

public class DrawingActivity extends AppCompatActivity {

    private final static String TAG = "ALLISON";
    private SimulatorView mSimulatorView;
    Bitmap backgroundBitmap;
    Canvas background;
    Paint linksPaint;
    Paint pathPaint;
    Paint jointsPaint;



    private final Point[] points = {new Point(553, 354),
            new Point(553, 228),
            new Point(960, 128),
            new Point(960, 354)};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);
        mSimulatorView = (SimulatorView) findViewById(R.id.simulator_view);

        Toast.makeText(DrawingActivity.this, "Welcome to the drawing activity!", Toast.LENGTH_LONG).show();

        Bitmap bitmap = null;
        String filename = getIntent().getStringExtra("BitmapImage");
        Log.i(TAG, filename);
        try {
            FileInputStream input = new FileInputStream(new File(filename));
            bitmap = BitmapFactory.decodeStream(input);
            input.close();
        } catch (Exception e) {
            Log.i(TAG, "File input Error: ", e);
        }

        backgroundBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        bitmap.recycle();
        background = new Canvas(backgroundBitmap);

        pathPaint = new Paint();
        pathPaint.setAntiAlias(true);
        pathPaint.setColor(Color.GREEN);
        pathPaint.setStrokeWidth(4);

        jointsPaint = new Paint();
        jointsPaint.setAntiAlias(true);
        jointsPaint.setColor(Color.RED);
        jointsPaint.setStyle(Paint.Style.FILL);

        linksPaint = new Paint();
        linksPaint.setAntiAlias(true);
        linksPaint.setColor(Color.BLACK);
        linksPaint.setStrokeWidth(4);

        drawFrame(points);
    }

    private void drawFrame(Point[] drawingPoints) {

        background.drawCircle((float) drawingPoints[2].x, (float) drawingPoints[2].y, 2, pathPaint);

        Log.i(TAG, "drawing new frame");

        Bitmap drawableBitmap = backgroundBitmap.copy(backgroundBitmap.getConfig(), true);
        Canvas drawable = new Canvas(drawableBitmap);

        for(Point point: drawingPoints) {
            Log.i(TAG, "Point at: (" + point.x + "," + point.y + ")");
            drawable.drawCircle((float) point.x, (float) point.y, 8, jointsPaint);
        }

        drawable.drawLine((float) drawingPoints[0].x, (float) drawingPoints[0].y, (float) drawingPoints[1].x, (float) drawingPoints[1].y, linksPaint);
        drawable.drawLine((float) drawingPoints[1].x, (float) drawingPoints[1].y, (float) drawingPoints[2].x, (float) drawingPoints[2].y, linksPaint);
        drawable.drawLine((float) drawingPoints[2].x, (float) drawingPoints[2].y, (float) drawingPoints[3].x, (float) drawingPoints[3].y, linksPaint);
        drawable.drawLine((float) drawingPoints[3].x, (float) drawingPoints[3].y, (float) drawingPoints[0].x, (float) drawingPoints[0].y, linksPaint);

        mSimulatorView.drawBitmap(drawableBitmap);
    }


    public void animateMechanism(View view) {

        double gamma;
        double thetaStart;

        Point[] workingPoints = convertCoordinates(points[0]);

        assert getMagnitude(new Point(0,0), new Point(0, 4)) == 4;
        assert getMagnitude(new Point(0, 0), new Point(4, 0)) == 4;
        assert getMagnitude(new Point(0, 0), new Point(3, 4)) == 5;
        double a = getMagnitude(workingPoints[0], workingPoints[1]);
        double b = getMagnitude(workingPoints[1], workingPoints[2]);
        double c = getMagnitude(workingPoints[2], workingPoints[3]);
        double d = getMagnitude(workingPoints[3], workingPoints[0]);

        gamma = getAngle(workingPoints[0], workingPoints[3]);
        thetaStart = getAngle(workingPoints[0], workingPoints[1]) - gamma;

        for(double theta = thetaStart; theta < thetaStart + 2 * Math.PI; theta = theta + 0.05) {
            double e = Math.sqrt(a * a + d * d - 2 * a * d * Math.cos(theta));
            double alpha = Math.asin((a * Math.sin(theta)) / e);
            double beta = Math.acos((e * e + c * c - b * b) / (2 * e * c));
            double phi = alpha + beta;

            Point A = workingPoints[0];
            Point D = workingPoints[3];
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
            double mag1 = getMagnitude(new Point(x1, y1), workingPoints[2]);
            double mag2 = getMagnitude(new Point(x2, y2), workingPoints[2]);

            if(mag1 < mag2) {
                C = new Point(x1, y1);
            } else {
                C = new Point(x2, y2);
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
                Log.e(TAG, ie.getMessage());
            }

            Point[] drawingPoints = reconvertCoordinates(new Point[] {A, B, C, D}, points[0]);
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

    private Point[] convertCoordinates (Point origin) {
        int yMax = background.getHeight();
        Point[] workingPoints = new Point[points.length];

        for(int i = 0; i < points.length; i++) {
            workingPoints[i] = new Point(points[i].x - origin.x, yMax - points[i].y - (yMax - origin.y));
        }
        return workingPoints;
    }

    private Point[] reconvertCoordinates (Point[] pointList, Point origin) {
        int yMax = background.getHeight();
        Point[] drawingPoints = new Point[points.length];

        for(int i = 0; i < pointList.length; i++) {
            drawingPoints[i] = new Point(pointList[i].x + origin.x, yMax - (pointList[i].y + (yMax - origin.y)));
        }
        return drawingPoints;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSimulatorView.stopThread();
    }
}
