package com.example.project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

public class ComponentCollectionFragment extends Fragment {
    // When requested, this adapter returns a ObjectFragment,
    // representing an object in the collection.
    ComponentCollectionAdapter componentCollectionAdapter;
    ViewPager2 viewPager;
    String[] titles = new String[]{"Links", "Joints"};
    Integer prototypeID;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        prototypeID = getArguments().getInt("prototype_id");
        return inflater.inflate(R.layout.fragment_component_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle args = new Bundle();
        // Our object is just an integer :-P
        args.putInt("prototype_id", prototypeID);
        this.setArguments(args);
        componentCollectionAdapter = new ComponentCollectionAdapter(this);
        viewPager = view.findViewById(R.id.pager);
        viewPager.setAdapter(componentCollectionAdapter);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(titles[position])
        ).attach();
    }

}
