package com.protocapture.project.activities;

import com.protocapture.project.database.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.protocapture.project.R;
import com.protocapture.project.database.PrototypeListAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity implements PrototypeListAdapter.OnProtoListener {
    public static final String EXTRA_MESSAGE = "com.example.project.activity.MainActivity.MESSAGE";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;
    public static final int NEW_PROTOTYPE_ACTIVITY_REQUEST_CODE = 1;
    public static final int VIEW_PROTOTYPE_ACTIVITY_REQUEST_CODE = 2;

    private PrototypeViewModel mPrototypeViewModel;
    private List<Prototype> protoList;

    @Override@SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!checkIsSupportedDeviceOrFinish(this)) { return; }

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setTitle("ProtoCapture");
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        final PrototypeListAdapter adapter = new PrototypeListAdapter(this, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mPrototypeViewModel = new ViewModelProvider(this).get(PrototypeViewModel.class);
        mPrototypeViewModel.getAllPrototypes().observe(this, new Observer<List<Prototype>>() {
            @Override
            public void onChanged(@Nullable final List<Prototype> prototypes) {
                // Update the cached copy of the words in the adapter.
                adapter.setPrototypes(prototypes);
                protoList = prototypes;
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddPrototypeActivity.class);
                startActivityForResult(intent, NEW_PROTOTYPE_ACTIVITY_REQUEST_CODE);
            }
        });
    }

    //https://www.freecodecamp.org/news/how-to-build-an-augmented-reality-android-app-with-arcore-and-android-studio-43e4676cb36f/
    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later");
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        }
        String openGlVersionString =
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }

    //https://www.youtube.com/watch?v=69C1ljfDvl0
    @Override
    public void onProtoClick(int position) {
        Intent intent = new Intent(this, ViewPrototypeActivity.class);
        String prototypeName = protoList.get(position).getPrototypeName();
        intent.putExtra(EXTRA_MESSAGE, prototypeName);
        startActivityForResult(intent, VIEW_PROTOTYPE_ACTIVITY_REQUEST_CODE);
    }

    @Override
    public void onDeleteClick(int position) {
        Integer prototypeID = protoList.get(position).getPrototypeId();
        String prototypeName = protoList.get(position).getPrototypeName();
        mPrototypeViewModel.deletePrototype(prototypeID);
        Toast toast =
                Toast.makeText(this, "Deleted " + prototypeName, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }


    /*public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == NEW_WORD_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            Prototype prototype = new Prototype(data.getStringExtra(NewPrototypeActivity.EXTRA_REPLY));
            mWordViewModel.insert(word);
        } else {
            Toast.makeText(
                    getApplicationContext(),
                    R.string.empty_not_saved,
                    Toast.LENGTH_LONG).show();
        }
    }*/
}
