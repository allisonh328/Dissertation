package com.protocapture.project.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.protocapture.project.HelpFragment;
import com.protocapture.project.JointFragment;
import com.protocapture.project.LinkFragment;
import com.protocapture.project.R;
import com.protocapture.project.database.Joint;
import com.protocapture.project.database.JointViewModel;
import com.protocapture.project.database.Link;
import com.protocapture.project.database.LinkViewModel;
import com.protocapture.project.database.Prototype;
import com.protocapture.project.database.PrototypeViewModel;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PrototypeCaptureActivity extends AppCompatActivity implements View.OnTouchListener, CameraBridgeViewBase.CvCameraViewListener2{
    public static final int VIEW_PROTOTYPE_ACTIVITY_REQUEST_CODE = 1;
    public static final String EXTRA_MESSAGE = "com.example.project.activity.MainActivity.MESSAGE";
    private static final String TAG = "ALLISON";

    private boolean createLinks = false;
    private boolean addJoints = false;
    private boolean paused = false;
    private Mat mRgba;
    private Mat drawable;
    private PrototypeViewModel mPrototypeViewModel;
    private LinkViewModel mLinkViewModel;
    private JointViewModel mJointViewModel;
    private Prototype mPrototype;
    private ArrayList<org.opencv.core.Point> centers;
    private ArrayList<Joint> lines;
    private Button createButton;
    private Button cancelButton;
    private int height;
    private int width;
    private Integer fakeID = 1;
    private List<Joint> mJoints;
    private List<Link> mLinks;
    private HashMap<String, List<Joint>> linkList = new HashMap();

    private CameraBridgeViewBase mOpenCvCameraView;
    private WindowManager windowManager;
    private final int MY_PERMISSIONS_REQUEST_USE_CAMERA = 0x00AF;

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
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_detection_activity_surface_view);
        windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
        // Set the Title of the screen to the Prototype name
        Intent intent = getIntent();
        String prototypeName = intent.getStringExtra(AddPrototypeActivity.EXTRA_MESSAGE);
        myToolbar.setTitle(prototypeName);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG,"Permission not available requesting permission");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_USE_CAMERA);
        } else {
            Log.d(TAG,"Permission has already been granted");
            mOpenCvCameraView.setCameraPermissionGranted();
            mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
            mOpenCvCameraView.setCvCameraViewListener(this);
        }

        createButton = (Button) findViewById(R.id.button_create);
        createButton.bringToFront();

        cancelButton = findViewById(R.id.button_cancel);
        cancelButton.bringToFront();

        height = Resources.getSystem().getDisplayMetrics().heightPixels;
        width = Resources.getSystem().getDisplayMetrics().widthPixels;

        mOpenCvCameraView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                mOpenCvCameraView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int top = mOpenCvCameraView.getTop();
                int bottom = mOpenCvCameraView.getBottom();
                int right = mOpenCvCameraView.getRight();
                int left = mOpenCvCameraView.getLeft();
                Log.i(TAG, "Top = " + top + ", bottom = " + bottom + ", left = " + left + ", right = " + right);
            }
        });

        mJointViewModel = new ViewModelProvider(this).get(JointViewModel.class);
        mLinkViewModel = new ViewModelProvider(this).get(LinkViewModel.class);
        mPrototypeViewModel = new ViewModelProvider(this).get(PrototypeViewModel.class);
        mPrototypeViewModel.getPrototype(prototypeName).observe(this, new Observer<Prototype>() {
            @Override
            public void onChanged(@Nullable final Prototype prototype) {
                // Update the cached copy of the words in the adapter.
                mPrototype = prototype;

                mJointViewModel.getAllProtoJoints(mPrototype.getPrototypeId()).observe(PrototypeCaptureActivity.this, new Observer<List<Joint>>() {
                    @Override
                    public void onChanged(@Nullable final List<Joint> joints) {
                        // Update the cached copy of the words in the adapter.
                        Log.i(TAG, "updating joints");
                        mJoints = joints;
                    }
                });

                mLinkViewModel.getAllProtoLinks(mPrototype.getPrototypeId()).observe(PrototypeCaptureActivity.this, new Observer<List<Link>>() {
                    @Override
                    public void onChanged(@Nullable final List<Link> links) {
                        // Update the cached copy of the words in the adapter.
                        Log.i(TAG, "updating links");
                        mLinks = links;
                    }
                });
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_USE_CAMERA) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG,"permission was granted! Do your stuff");
                mOpenCvCameraView.setCameraPermissionGranted();
                mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
                mOpenCvCameraView.setCvCameraViewListener(this);
            } else {
                Log.d(TAG,"permission denied! Disable the function related with permission.");
                Toast.makeText(this, "Camera permission required.", Toast.LENGTH_LONG).show();
            }
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
        if (id == R.id.action_add_joint) {
            if(!paused) {
                Toast.makeText(this, "Pause feed before adding joints", Toast.LENGTH_LONG).show();
                return true;
            }

            addJoints = true;
            createLinks = false;
            Toast.makeText(PrototypeCaptureActivity.this, "Select all joints, then click 'add'", Toast.LENGTH_LONG).show();

            createButton.setVisibility(View.VISIBLE);
            createButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addJoints = false;
                    mRgba = drawable.clone();
                    createButton.setVisibility(View.GONE);
                    cancelButton.setVisibility(View.GONE);
                }
            });

            cancelButton.setVisibility(View.VISIBLE);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addJoints = false;
                    drawable = mRgba.clone();
                    deleteJoints();
                    cancelButton.setVisibility(View.GONE);
                    createButton.setVisibility(View.GONE);
                }
            });

        } else if (id == R.id.action_add_link) {
            if(!paused) {
                Toast.makeText(this, "Pause feed before adding joints", Toast.LENGTH_LONG).show();
                return true;
            }
            if(mJoints == null) {
                Toast.makeText(this, "Please select joints first", Toast.LENGTH_LONG).show();
                return true;
            }

            createLinks = true;
            addJoints = false;
            Toast.makeText(this, "Select joints in order, beginning and ending with the endpoints.\n" +
                    "Click 'Add' to create link.", Toast.LENGTH_LONG).show();
            drawable = mRgba.clone();

            createButton.setVisibility(View.VISIBLE);
            createButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(createButton.getText().equals("Done")) {
                        clickDone();
                    } else {
                        clickAdd();
                        createButton.setText(R.string.done_button);
                    }
                }
            });

            cancelButton.setVisibility(View.VISIBLE);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    createLinks = false;
                    lines.clear();
                    drawable = mRgba.clone();
                    deleteLinks();
                    cancelButton.setVisibility(View.GONE);
                    createButton.setVisibility(View.GONE);
                }
            });
        } else if (id == R.id.action_help) {
            FragmentManager fm = getSupportFragmentManager();
            HelpFragment fragment = new HelpFragment();
            fragment.setStyle(HelpFragment.STYLE_NORMAL, R.style.CustomDialog);
            Bundle args = new Bundle();
            args.putString("key", "capture");
            fragment.setArguments(args);
            fragment.show(fm, "fragment_help");
        }

        return super.onOptionsItemSelected(item);
    }


    private void deleteJoints() {
        if(mJoints != null) {
            for (Joint joint: mJoints) {
                mJointViewModel.deleteJoint(joint.getJointId());
            }
        }
    }


    private void deleteLinks() {
        if(mLinks != null) {
            for (Link link : mLinks) {
                mLinkViewModel.deleteLink(link.getLinkId());
            }
        }
    }


    private void clickDone() {
        for(String linkName: linkList.keySet()) {
            for(Link link: mLinks) {
                if(link.getLinkName().equals(linkName)) {
                    List<Joint> joints = linkList.get(linkName);
                    for (Joint joint: joints) {
                        addLinkId(joint, link.getLinkId());
                    }
                }
            }
        }
        mJointViewModel.updateJoints(mJoints);
        saveBitmap();
        Intent intent = new Intent(PrototypeCaptureActivity.this, ViewPrototypeActivity.class);
        intent.putExtra(EXTRA_MESSAGE, mPrototype.getPrototypeName());
        startActivityForResult(intent, VIEW_PROTOTYPE_ACTIVITY_REQUEST_CODE);
    }


    private void clickAdd() {
        if(lines.size() < 2) {
            Toast.makeText(PrototypeCaptureActivity.this, "Please select at least 2 joints", Toast.LENGTH_LONG).show();
        } else {
            addLink();
            lines.clear();
        }
    }


   /* private void setJoints() {
        mJointViewModel.getAllProtoJoints(mPrototype.getPrototypeId()).observe(this, new Observer<List<Joint>>() {
            @Override
            public void onChanged(@Nullable final List<Joint> jointsList) {
                joints = jointsList;
            }
        });
    }*/


    private void addLink() {
        Log.i(TAG, "in addLink");
        Link link = new Link();
        link.setParentID(mPrototype.getPrototypeId());
        String linkName = mPrototype.getPrototypeName() + "Link" + fakeID;
        link.setLinkName(linkName);
        fakeID++;
        link.setEndpoint1(lines.get(0).getJointId());
        link.setEndpoint2(lines.get(lines.size() - 1).getJointId());
        Log.i(TAG, "there are " + lines.size() + " Joints in lines");
        mLinkViewModel.insert(link);
        Point endpoint1 = new Point(lines.get(0).getXCoord(), lines.get(0).getYCoord());
        Point endpoint2 = new Point(lines.get(lines.size() - 1).getXCoord(), lines.get(lines.size() - 1).getYCoord());
        linkList.put(linkName, new ArrayList<Joint>(lines));
        Imgproc.line(drawable, endpoint1, endpoint2, new Scalar(0, 0, 240), 5);
    }


    private void saveBitmap() {

        Bitmap bitmap = Bitmap.createBitmap(drawable.cols(), drawable.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(drawable, bitmap);
        String filename = getExternalCacheDir() + "/" + mPrototype.getPrototypeName() + ".png";
        File file = new File(filename);

        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch(Exception e) {
            Log.e(TAG, "New File exception", e);
        }

        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
            bitmap.recycle();
            mPrototypeViewModel.setPrototypeBitmap(filename, mPrototype.getPrototypeName());
        } catch (IOException e) {
            Log.e(TAG, "ERROR:", e);
        }
    }


    private Joint getJointbyId(int id) {
        for(Joint joint: mJoints) {
            if(joint.getJointId() == id) {
                return joint;
            }
        }
        return null;
    }


    private void addLinkId(Joint joint, int linkID) {
        Log.i(TAG, "Link id = " + linkID + ", joint id = " + joint.getJointId());
        if(joint.getLink1ID() == null) {
            Log.i(TAG, "Adding to link1");
            joint.setLink1ID(linkID);
        } else if (!joint.getLink1ID().equals(linkID)) {
            if(joint.getLink2ID() == null) {
                Log.i(TAG, "Adding to link2");
                joint.setLink2ID(linkID);
            } else if (!joint.getLink2ID().equals(linkID)) {
                if (joint.getLink3ID() == null) {
                    Log.i(TAG, "Adding to link3");
                    joint.setLink3ID(linkID);
                } else if (!joint.getLink3ID().equals(linkID) && joint.getLink4ID() == null) {
                    Log.i(TAG, "Adding to link4");
                    joint.setLink4ID(linkID);
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
        Toast.makeText(PrototypeCaptureActivity.this, "Tap screen to pause frame and capture prototype.\n" +
                "Tap again to resume feed and select a new frame.", Toast.LENGTH_LONG).show();
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

        float maxDistance = width/30;
        float xTouch = (event.getX() / width) * mRgba.cols();
        float yTouch = (event.getY() / height) * mRgba.rows();

        Log.i(TAG, "MainActivity.onTouch: Touch at (" + xTouch + "," + yTouch + ")");
        int index = -1;

        if (addJoints) {
            if (centers.isEmpty()) {
                Log.i(TAG, "MainActivity.onTouch: Runnin on empty.");
                return true;
            }

            for (org.opencv.core.Point center : centers) {

                if (Math.abs(center.x - xTouch) < maxDistance && Math.abs(center.y - yTouch) < maxDistance) {
                    Log.i(TAG, "*********************");
                    Log.i(TAG, "MainActivity.onTouch: Center at (" + center.x + "," + center.y + ")");
                    Log.i(TAG, "*********************");
                    index = centers.indexOf(center);
                    Imgproc.circle(drawable, center, 8, new Scalar(240, 0, 0), -1);
                    Joint joint = new Joint();
                    String jointName = mPrototype.getPrototypeName() + "Joint" + fakeID;
                    fakeID++;
                    joint.setJointName(jointName);
                    joint.setPrototypeID(mPrototype.getPrototypeId());
                    joint.setXCoord(center.x);
                    joint.setYCoord(center.y);
                    mJointViewModel.insert(joint);
                    break;
                }
            }
            if(index != -1) {
                centers.remove(index);
            }

        } else if(createLinks) {
            for (Joint joint: mJoints) {
                if(Math.abs(joint.getXCoord() - xTouch) < maxDistance && Math.abs(joint.getYCoord() - yTouch) < maxDistance) {
                    if(!lines.contains(joint)) {
                        Imgproc.circle(drawable, new Point(joint.getXCoord(), joint.getYCoord()), 10, new Scalar(240, 0, 0), -1);
                        lines.add(joint);
                        createButton.setText(R.string.add_button);
                    }
                }
            }

        }  else {
            drawable = mRgba.clone();
            deleteJoints();
            deleteLinks();
            paused = !paused;
            Log.i(TAG, "MainActivity.onTouch: Paused -> # circles = " + Integer.toString(centers.size()));
        }

        //Log.i(TAG, "MainActivity.onTouch: I can go the distance");
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
            Imgproc.blur(bwMat, threshImage, new Size(5, 5));
            Core.inRange(threshImage, new Scalar(0, 0, 0), new Scalar(30, 30, 30), threshImage);

            //Imgproc.threshold(threshImage, threshImage, 200, 255, Imgproc.THRESH_BINARY);

            Imgproc.findContours(threshImage, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            double minArea = 30;
            double maxArea = 300;
            float[] radius = new float[1];

            for (int i = 0; i < contours.size(); i++) {
                MatOfPoint c1 = contours.get(i);
                if (Imgproc.contourArea(c1) > minArea && Imgproc.contourArea(c1) < maxArea) {
                    MatOfPoint2f c2f = new MatOfPoint2f(c1.toArray());
                    org.opencv.core.Point center = new Point();
                    Imgproc.minEnclosingCircle(c2f, center, radius);
                    centers.add(center);
                    //Log.i(TAG, "MainActivity.onCameraFrame: center at (" + center.x + "," + center.y + ")");
                    Imgproc.circle(mRgba, center, (int) radius[0], new Scalar(0, 240, 0), 2);
                }
            }

            long stopTime = System.nanoTime();
            Log.i(TAG, "MainActivity.onCameraFrame: time elapsed = " + Long.toString(stopTime - startTime));
            try {
                Thread.sleep(300);
            } catch (InterruptedException ie) {
                return mRgba;
            }
            return mRgba;
        } else {
            return drawable;
        }
    }

}
