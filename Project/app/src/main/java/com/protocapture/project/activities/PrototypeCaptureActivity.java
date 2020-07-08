package com.protocapture.project.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.protocapture.project.R;
import com.protocapture.project.database.Link;
import com.protocapture.project.database.LinkViewModel;
import com.protocapture.project.database.Prototype;
import com.protocapture.project.database.PrototypeViewModel;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class PrototypeCaptureActivity extends AppCompatActivity implements View.OnTouchListener, CameraBridgeViewBase.CvCameraViewListener2{
    public static final int LINK_EDITOR_REQUEST_CODE = 1;
    private static final String TAG = "ALLISON";

    private boolean createLink = false;
    private boolean paused = false;
    private Mat mRgba;
    private PrototypeViewModel mPrototypeViewModel;
    private Prototype mPrototype;
    private ArrayList<org.opencv.core.Point> centers;
    private ArrayList<org.opencv.core.Point> lines;
    private Button createButton;
    private int height;
    private int width;

    private CameraBridgeViewBase mOpenCvCameraView;
    private final int MY_PERMISSIONS_REQUEST_USE_CAMERA = 0x00AF;

   /* private ArFragment arFragment;
    private ViewRenderable helloRenderable;
    private LayoutInflater mInflater;
    private LinkViewModel mLinkViewModel;
    private static Integer fakeID = 1;
    private View itemView;*/

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(PrototypeCaptureActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_prototype_capture);
        /*arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        mInflater = LayoutInflater.from(this);*/

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        // Set the Title of the screen to the Prototype name
        Intent intent = getIntent();
        String prototypeName = intent.getStringExtra(AddPrototypeActivity.EXTRA_MESSAGE);
        myToolbar.setTitle(prototypeName);

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

        mPrototypeViewModel = new ViewModelProvider(this).get(PrototypeViewModel.class);
        mPrototypeViewModel.getPrototype(prototypeName).observe(this, new Observer<Prototype>() {
            @Override
            public void onChanged(@Nullable final Prototype prototype) {
                // Update the cached copy of the words in the adapter.
                mPrototype = prototype;

                //getActionBar().setTitle(message);

            }
        });

        /*ViewGroup viewGroup = findViewById(R.id.renderable);
        itemView = mInflater.inflate(R.layout.hello_view, viewGroup, false);
        //TextView view = itemView.findViewById(R.id.textView2);

        ViewRenderable.builder()
                .setView(this, itemView)
                .build()
                .thenAccept(renderable -> helloRenderable = renderable);

        //https://www.freecodecamp.org/news/how-to-build-an-augmented-reality-android-app-with-arcore-and-android-studio-43e4676cb36f/
        arFragment.setOnTapArPlaneListener(
                (HitResult hitresult, Plane plane, MotionEvent motionevent) -> {
                    onHit(hitresult, plane, motionevent);
                });*/
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
            Toast.makeText(PrototypeCaptureActivity.this, "Select endpoints first, then any other joints on the link.\n" +
                    "Click 'Create' to create link.", Toast.LENGTH_LONG).show();
            createButton.setVisibility(View.VISIBLE);
            createButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(lines.size() < 2) {
                        Toast.makeText(PrototypeCaptureActivity.this, "Please select at least 2 joints", Toast.LENGTH_LONG).show();
                        createLink = false;
                        return;
                    } else {
                        Imgproc.line(mRgba, lines.get(0), lines.get(1), new Scalar(0, 0, 240), 5);
                        createLink = false;
                        lines.clear();
                        createButton.setVisibility(View.GONE);
                        return;
                    }
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
            paused = !paused;
            Log.i(TAG, "MainActivity.onTouch: Paused -> # circles = " + Integer.toString(centers.size()));
        } else {
            if (centers.isEmpty()) {
                Log.i(TAG, "MainActivity.onTouch: Runnin on empty.");
                return true;
            }

            float maxDistance = 100;

            for (org.opencv.core.Point center : centers) {
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
                    org.opencv.core.Point center = new Point();
                    Imgproc.minEnclosingCircle(c2f, center, radius);
                    centers.add(center);
                    Log.i(TAG, "MainActivity.onCameraFrame: center at (" + Double.toString(center.x) + "," + Double.toString(center.y) + ")");
                    Imgproc.circle(mRgba, center, (int) radius[0], new Scalar(0, 240, 0), 2);
                }
            }

            Log.i(TAG, "MainActivity.onCameraFrame: # contours = " + Long.toString(centers.size()));

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

    //https://developer.android.com/training/basics/firstapp/starting-activity
    /*public void editLink(View view) {
        Intent intent = new Intent(this, LinkEditorActivity.class);
        TextView textView = itemView.findViewById(R.id.textView2);
        String name = textView.getText().toString();
        intent.putExtra("link_name", name);
        startActivityForResult(intent, LINK_EDITOR_REQUEST_CODE);
    }

    private void onHit(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
        if (helloRenderable == null){ return; }
        ViewRenderable linkRenderable = helloRenderable.makeCopy();
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());
        TransformableNode hello = new TransformableNode(arFragment.getTransformationSystem());
        hello.getScaleController().setMaxScale(0.2f);
        hello.getScaleController().setMinScale(0.1f);
        mLinkViewModel = new ViewModelProvider(this).get(LinkViewModel.class);
        Link link = new Link();
        String linkName = "Link" + Integer.toString(fakeID);
        fakeID++;
        link.setLinkName(linkName);
        link.setParentID(mPrototype.getPrototypeId());
        Log.d(TAG, "onChanged: prototypeID = " + Integer.toString(mPrototype.getPrototypeId()));
        mLinkViewModel.insert(link);
        TextView view = itemView.findViewById(R.id.textView2);
        view.setText(link.getLinkName());
        hello.setParent(anchorNode);
        hello.setRenderable(linkRenderable);
        hello.select();
    }*/
}
