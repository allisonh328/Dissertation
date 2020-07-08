package com.example.opencvproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.core.Rect;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "ALLISON_COMMENT";

    private boolean createLink = false;
    private boolean paused = false;
    private Mat mRgba;
    private ArrayList<Point> centers;
    private ArrayList<Point> lines;
    private Button createButton;
    private int height;
    private int width;

    private CameraBridgeViewBase mOpenCvCameraView;
    private final int MY_PERMISSIONS_REQUEST_USE_CAMERA = 0x00AF;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(MainActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        createButton = (Button) findViewById(R.id.button_create);
        createButton.bringToFront();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG,"Permission not available requesting permission");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_USE_CAMERA);
        } else {
            Log.d(TAG,"Permission has already granted");
            mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_detection_activity_surface_view);
            mOpenCvCameraView.setCameraPermissionGranted();
            mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
            mOpenCvCameraView.setCvCameraViewListener(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.capture_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add) {
            createLink = true;
            Toast.makeText(MainActivity.this, "Select endpoints first, then any other joints on the link.\n" +
                    "Click 'Create' to create link.", Toast.LENGTH_LONG).show();
            createButton.setVisibility(View.VISIBLE);
            createButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                }
            });
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_USE_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG,"permission was granted! Do your stuff");
                    mOpenCvCameraView.setCameraPermissionGranted();
                } else {
                    Log.d(TAG,"permission denied! Disable the function related with permission.");
                    Toast toast =
                            Toast.makeText(this, "Camera permission required.", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.i(TAG, "MainActivity.onCameraViewStarted: Camera starting.");
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        centers = new ArrayList<>();
        lines = new ArrayList<>();
        if(mRgba == null) {
            Log.i(TAG, "MainActivity.onCameraViewStarted: Initialized incorrectly.");
        }
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    // https://stackoverflow.com/questions/22863842/how-to-make-circle-clickable
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        super.onTouchEvent(event);

        if (event.getAction() != MotionEvent.ACTION_DOWN && event.getAction() != MotionEvent.ACTION_POINTER_DOWN) {
            //Log.i(TAG, "MainActivity.onTouch: Leavin on a jetplane.");
            return true;
        }

        if(!createLink) {
            /*if(paused) {
                onResume();
            } else {
                mOpenCvCameraView.disableView();
                Bitmap bitmap = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(mRgba, bitmap);
                mOpenCvCameraView.set
            }*/
            paused = !paused;
            Log.i(TAG, "MainActivity.onTouch: Paused -> # circles = " + Integer.toString(centers.size()));
        } else {
            if (centers.isEmpty()) {
                Log.i(TAG, "MainActivity.onTouch: Runnin on empty.");
                return true;
            }

            float maxDistance = 100;

            for (Point center : centers) {
                float xTouch = (event.getX() / width) * mRgba.cols();
                float yTouch = (event.getY() / height) * mRgba.rows();
                Log.i(TAG, "MainActivity.onTouch: Touch at (" + Float.toString(xTouch) + "," + Float.toString(yTouch) + ")");
                if (Math.abs(center.x - xTouch) < maxDistance && Math.abs(center.y - yTouch) < maxDistance) {
                    Imgproc.circle(mRgba, center, 8, new Scalar(240, 0, 0), -1);
                    lines.add(center);
                    Log.i(TAG, "*********************");
                    Log.i(TAG, "MainActivity.onTouch: Center at (" + Double.toString(center.x) + "," + Double.toString(center.y) + ")");
                    Log.i(TAG, "MainActivity.onTouch: I can go the distance");
                    Log.i(TAG, "*********************");
                    return true;
                }
            }
        }

        Log.i(TAG, "MainActivity.onTouch: I can go the distance");
        return true;
    }

    // https://stackoverflow.com/questions/31504366/opencv-for-java-houghcircles-finding-all-the-wrong-circles
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        if(!paused) {
            mRgba = inputFrame.rgba();
            centers.clear();
            Mat bwMat = new Mat();
            List<MatOfPoint> contours = new ArrayList<>();
            Mat threshImage = new Mat();

            long startTime = System.nanoTime();

            /*for (Point line : lines) {
                Imgproc.circle(mRgba, line, (int) 5, new Scalar(0, 0, 240), -1);
            }*/

            Imgproc.cvtColor(mRgba, bwMat, Imgproc.COLOR_BGR2GRAY);
            Core.inRange(bwMat, new Scalar(0, 0, 0), new Scalar(20, 20, 10), threshImage);
            Imgproc.blur(threshImage, threshImage, new Size(3, 3));
            Imgproc.threshold(threshImage, threshImage, 200, 255, Imgproc.THRESH_BINARY);

            Imgproc.findContours(threshImage, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            double minArea = 8;
            float[] radius = new float[1];


            for (int i = 0; i < contours.size(); i++) {
                MatOfPoint c1 = contours.get(i);
                if (Imgproc.contourArea(c1) > minArea) {
                    MatOfPoint2f c2f = new MatOfPoint2f(c1.toArray());
                    Point center = new Point();
                    Imgproc.minEnclosingCircle(c2f, center, radius);
                    centers.add(center);
                    Log.i(TAG, "MainActivity.onCameraFrame: center at (" + Double.toString(center.x) + "," + Double.toString(center.y) + ")");
                    Imgproc.circle(mRgba, center, (int) radius[0], new Scalar(0, 240, 0), 2);
                }
            }

            Log.i(TAG, "MainActivity.onCameraFrame: # contours = " + Long.toString(centers.size()));
       /* MyMath maths = new MyMath();
        ArrayList<Double[]> lines = maths.drawLines(centers);
        Log.i(TAG, "MainActivity.onCameraFrame: # lines = " + Long.toString(lines.size()));
        /*Imgproc.Canny(bwMat, canny, 80, 120);
        //Imgproc.HoughLinesP(canny, lines, 1, Math.PI / 180, 50, 20, 20);
        Imgproc.GaussianBlur(canny, canny, new Size(5, 5), 2, 2);
        Imgproc.HoughCircles(canny, lines, Imgproc.HOUGH_GRADIENT, 1.5, 5, 50, 30, 0, 30);
        houghLines.create(canny.rows(), canny.cols(), CvType.CV_8UC1);
        Log.i(TAG, "MainActivity.onCameraFrame: # circles = " + Integer.toString(lines.rows()));
        //MyMath myMath = new MyMath();
        //houghLines = myMath.combineLines(lines, canny);
        for (int i = 0; i < lines.size(); i++) {
            Double[] points = lines.get(i);

            Point point1 = new Point(points[0], points[1]);
            Point point2 = new Point(points[2], points[3]);

            Path linePath = new Path();
            RectF rectF = new RectF();

            linePath.moveTo((float) point1.x, (float) point1.y);
            linePath.lineTo((float) point2.x, (float) point2.y);

            Imgproc.line(mRgba, point1, point2, new Scalar(0, 0, 240), 2);

            Log.i(TAG, "MainActivity.onCameraFrame: line from (" + Double.toString(point1.x) + "," + Double.toString(point1.y) + ") to (" + Double.toString(point2.x) + "," + Double.toString(point2.y) + ")");
            linePath.computeBounds(rectF, true);
        }*/

            long stopTime = System.nanoTime();
            Log.i(TAG, "MainActivity.onCameraFrame: time elapsed = " + Long.toString(stopTime - startTime));
            try {
                Thread.sleep(500);
            } catch (InterruptedException ie) {
                Log.e(TAG, ie.getMessage());
            }
        }
        return mRgba;
    }

    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }
}

