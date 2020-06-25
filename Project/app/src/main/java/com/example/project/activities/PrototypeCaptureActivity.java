package com.example.project.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.project.ComponentCollectionFragment;
import com.example.project.R;
import com.example.project.database.Link;
import com.example.project.database.LinkViewModel;
import com.example.project.database.Prototype;
import com.example.project.database.PrototypeViewModel;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import org.w3c.dom.Text;

public class PrototypeCaptureActivity extends AppCompatActivity {
    public static final int LINK_EDITOR_REQUEST_CODE = 1;
    private static final String TAG = "ALLISON_COMMENT";

    private ArFragment arFragment;
    private ModelRenderable foxRenderable;
    private ViewRenderable helloRenderable;
    //private LayoutInflater mInflater;
    private PrototypeViewModel mPrototypeViewModel;
    private LinkViewModel mLinkViewModel;
    private Prototype mPrototype;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prototype_capture);
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        //mInflater = LayoutInflater.from(this);

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

             /* ModelRenderable.builder()
                .setSource(this, Uri.parse("fox.sfb"))
                .build()
                .thenAccept(renderable -> foxRenderable = renderable)
                .exceptionally(throwable -> {
                    Toast toast =
                            Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return null;
                }); */

        //ViewGroup viewGroup = findViewById(R.id.renderable);
        //View itemView = mInflater.inflate(R.layout.hello_view, viewGroup, false);
        TextView view = findViewById(R.id.textView2);

        ViewRenderable.builder()
                .setView(this, view)
                .build()
                .thenAccept(renderable -> helloRenderable = renderable);

        //https://www.freecodecamp.org/news/how-to-build-an-augmented-reality-android-app-with-arcore-and-android-studio-43e4676cb36f/
        arFragment.setOnTapArPlaneListener(
                (HitResult hitresult, Plane plane, MotionEvent motionevent) -> {
                    if (helloRenderable == null){ return; }
                    Anchor anchor = hitresult.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());
                    TransformableNode hello = new TransformableNode(arFragment.getTransformationSystem());
                    mLinkViewModel = new ViewModelProvider(this).get(LinkViewModel.class);
                    Link link = new Link();
                    String linkName = "Link" + Integer.toString((link.getLinkId()));
                    link.setLinkName(linkName);
                    link.setParentID(mPrototype.getPrototypeId());
                    mLinkViewModel.insert(link);
                    view.setText(linkName);
                    hello.setParent(anchorNode);
                    hello.setRenderable(helloRenderable);
                    hello.select();
                });
    }

    //https://developer.android.com/training/basics/firstapp/starting-activity
    public void editLink(View view) {
        Intent intent = new Intent(this, LinkEditorActivity.class);
        TextView textView = (TextView) findViewById(R.id.textView2);
        String name = textView.getText().toString();
        intent.putExtra("link_name", name);
        startActivityForResult(intent, LINK_EDITOR_REQUEST_CODE);
    }
}
