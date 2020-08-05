package com.protocapture.project.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.protocapture.project.ComponentCollectionFragment;
import com.protocapture.project.HelpFragment;
import com.protocapture.project.R;
import com.protocapture.project.database.JointListAdapter;
import com.protocapture.project.database.LinkListAdapter;
import com.protocapture.project.database.Prototype;
import com.protocapture.project.database.PrototypeViewModel;

public class ViewPrototypeActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.example.project.activity.ViewPrototypeActivity.MESSAGE";
    public static final int SIMULATOR_ACTIVITY_REQUEST_CODE = 1;
    public static final String TAG = "ALLISON";

    private PrototypeViewModel mPrototypeViewModel;
    private Prototype mPrototype;
    FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_prototype);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

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

                // Set the Title of the screen to the Prototype name
                myToolbar.setTitle(mPrototype.getPrototypeName());

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.prototype_view_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_view_prototype) {
            if(mPrototype.getPrototypeBitmap() == null) {
                Toast.makeText(this, "No image has been saved for this prototype.", Toast.LENGTH_LONG).show();
                return true;
            }
            Intent intent = new Intent(this, SimulatorActivity.class);
            intent.putExtra(EXTRA_MESSAGE, mPrototype.getPrototypeName());
            startActivityForResult(intent, SIMULATOR_ACTIVITY_REQUEST_CODE);
        } else if (id == R.id.action_help) {
            FragmentManager fm = getSupportFragmentManager();
            HelpFragment fragment = new HelpFragment();
            fragment.setStyle(HelpFragment.STYLE_NORMAL, R.style.CustomDialog);
            Bundle args = new Bundle();
            args.putString("key", "view");
            fragment.setArguments(args);
            fragment.show(fm, "fragment_help");
        }

        return super.onOptionsItemSelected(item);
    }
 }
