package com.example.project.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.example.project.R;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class PrototypeCaptureActivity extends AppCompatActivity {

    ArFragment arFragment;
    ModelRenderable foxRenderable;
    ViewRenderable helloRenderable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prototype_capture);
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

        // Set the Title of the screen to the Prototype name
        Intent intent = getIntent();
        String message = intent.getStringExtra(AddPrototypeActivity.EXTRA_MESSAGE);
        //getActionBar().setTitle(message);
        getSupportActionBar().setTitle(message);
        getSupportActionBar().show();

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

        ViewRenderable.builder()
                .setView(this, R.layout.hello_view)
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
                    hello.setParent(anchorNode);
                    hello.setRenderable(helloRenderable);
                    hello.select();
                });
    }

    //https://developer.android.com/training/basics/firstapp/starting-activity
    public void editLink(View view) {
        Intent intent = new Intent(this, LinkEditorActivity.class);
        //EditText editText = (EditText) findViewById(R.id.editText);
        //String message = editText.getText().toString();
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }
}
