package com.protocapture.project.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.protocapture.project.R;
import com.protocapture.project.database.Link;
import com.protocapture.project.database.LinkViewModel;
import com.protocapture.project.database.Prototype;
import com.protocapture.project.database.PrototypeViewModel;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import org.opencv.core.Mat;

import java.util.ArrayList;

public class PrototypeCaptureActivity extends AppCompatActivity {
    public static final int LINK_EDITOR_REQUEST_CODE = 1;
    private static final String TAG = "ALLISON";

    private ArFragment arFragment;
    private ModelRenderable foxRenderable;
    private ViewRenderable helloRenderable;
    private LayoutInflater mInflater;
    private PrototypeViewModel mPrototypeViewModel;
    private LinkViewModel mLinkViewModel;
    private Prototype mPrototype;
    private static Integer fakeID = 1;
    private View itemView;

    private Mat mRgba;
    ArrayList<Point> centers;
    ArrayList<Point> lines;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_prototype_capture);
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        mInflater = LayoutInflater.from(this);

        // Set the Title of the screen to the Prototype name
        Intent intent = getIntent();
        String prototypeName = intent.getStringExtra(AddPrototypeActivity.EXTRA_MESSAGE);
        getSupportActionBar().setTitle(prototypeName);
        getSupportActionBar().show();

        mPrototypeViewModel = new ViewModelProvider(this).get(PrototypeViewModel.class);
        mPrototypeViewModel.getPrototype(prototypeName).observe(this, new Observer<Prototype>() {
            @Override
            public void onChanged(@Nullable final Prototype prototype) {
                // Update the cached copy of the words in the adapter.
                mPrototype = prototype;

                //getActionBar().setTitle(message);

            }
        });

        ViewGroup viewGroup = findViewById(R.id.renderable);
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
                });
    }

    //https://developer.android.com/training/basics/firstapp/starting-activity
    public void editLink(View view) {
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
    }
}
