package com.protocapture.project;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ComponentCollectionAdapter extends FragmentStateAdapter{

    public static final String TAG = "ALLISON";

    Fragment parentFragment;

    public ComponentCollectionAdapter(Fragment fragment) {

        super(fragment);
        parentFragment = fragment;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Return a NEW fragment instance in createFragment(int)
        Fragment fragment;
        if(position == 0) {
            Log.d(TAG, "ComponentCollectionAdapter.createFragment: Making a link fragment");
            fragment = new LinkFragment();
        } else if(position == 1) {
            Log.d(TAG, "ComponentCollectionAdapter.createFragment: Making a joint fragment");
            fragment = new JointFragment();
        } else {
            Log.d(TAG, "ComponentCollectionAdapter.createFragment: something funky is going on with your positions");
            return null;
        }
        Bundle args = new Bundle();
        args.putInt("prototype_id", parentFragment.getArguments().getInt("prototype_id"));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
