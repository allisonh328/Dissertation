package com.example.project;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ComponentCollectionAdapter extends FragmentStateAdapter{

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
            fragment = new LinkFragment();
        } else if(position == 1) {
            fragment = new JointFragment();
        } else {
            return null;
        }
        Bundle args = new Bundle();
        // Our object is just an integer :-P
        args.putInt("prototype_id", parentFragment.getArguments().getInt("prototype_id"));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
