package com.example.project.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.project.ComponentCollectionAdapter;
import com.example.project.ComponentCollectionFragment;
import com.example.project.R;
import com.example.project.database.LinkListAdapter;
import com.example.project.database.Prototype;
import com.example.project.database.PrototypeViewModel;
import com.example.project.database.PrototypeWithComponents;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ViewPrototypeActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.example.project.activity.ViewPrototypeActivity.MESSAGE";
    public static final int PROTOTYPE_CAPTURE_ACTIVITY_REQUEST_CODE = 1;

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
