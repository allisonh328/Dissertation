package com.protocapture.project.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.protocapture.project.ComponentCollectionFragment;
import com.protocapture.project.R;
import com.protocapture.project.database.Prototype;
import com.protocapture.project.database.PrototypeViewModel;

public class ViewPrototypeActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.example.project.activity.ViewPrototypeActivity.MESSAGE";
    public static final int PROTOTYPE_CAPTURE_ACTIVITY_REQUEST_CODE = 1;
    public static final String TAG = "ALLISONTAG";

    private PrototypeViewModel mPrototypeViewModel;
    private Prototype mPrototype;
    FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_prototype);

        // Set the Title of the screen to the Prototype name
        Intent intent = getIntent();
        String prototypeName = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        mPrototypeViewModel = new ViewModelProvider(this).get(PrototypeViewModel.class);
        mPrototypeViewModel.getPrototype(prototypeName).observe(this, new Observer<Prototype>() {
            @Override
            public void onChanged(@Nullable final Prototype prototype) {
                // Update the cached copy of the words in the adapter.
                mPrototype = prototype;
                Log.d(TAG, "onChanged: prototypeID = " + Integer.toString(mPrototype.getPrototypeId()));

                //getActionBar().setTitle(message);
                getSupportActionBar().setTitle(mPrototype.getPrototypeName());
                getSupportActionBar().show();

                ComponentCollectionFragment fragment = new ComponentCollectionFragment();
                Bundle args = new Bundle();
                // Our object is just an integer :-P
                args.putInt("prototype_id", mPrototype.getPrototypeId());
                fragment.setArguments(args);
                fragmentTransaction.add(R.id.fragment_container_view_tag, fragment);
                fragmentTransaction.commit();
            }
        });

    }

 }
