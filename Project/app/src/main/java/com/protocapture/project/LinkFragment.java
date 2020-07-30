package com.protocapture.project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.protocapture.project.activities.LinkEditorActivity;
import com.protocapture.project.database.Link;
import com.protocapture.project.database.LinkListAdapter;
import com.protocapture.project.database.LinkViewModel;

import java.util.List;

public class LinkFragment extends Fragment implements LinkListAdapter.OnLinkListener {
    public static final int LINK_EDITOR_REQUEST_CODE = 1;
    public static final String TAG = "ALLISON";

    private LinkViewModel mLinkViewModel;
    private List<Link> linkList;
    private Integer prototypeID;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        prototypeID = getArguments().getInt("prototype_id");
        Log.d(TAG, "LinkFragment.onCreateView: prototypeID = " + Integer.toString(prototypeID));
        View view = inflater.inflate(R.layout.fragment_component_page, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //((TextView) view.findViewById(android.R.id.text1))
                //.setText(Integer.toString(args.getInt(ARG_OBJECT)));

        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        final LinkListAdapter adapter = new LinkListAdapter(this.getContext(), this);

        mLinkViewModel = new ViewModelProvider(this.getActivity()).get(LinkViewModel.class);
        //mLinkViewModel.setAllLinks(prototypeID);
        mLinkViewModel.getAllProtoLinks(prototypeID).observe(this.getViewLifecycleOwner(), new Observer<List<Link>>() {
            @Override
            public void onChanged(@Nullable final List<Link> links) {
                // Update the cached copy of the words in the adapter.
                adapter.setLinks(links);
                linkList = links;
                if(links.isEmpty()) {
                    Log.d(TAG, "LinkFragment.onChanged: Empty list :(");
                } else {
                    Log.d(TAG, "LinkFragment.onChanged: link_name = " + links.get(0).getLinkName());
                }
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            }
        });
    }

    @Override
    public void onLinkClick(int position) {
        Intent intent = new Intent(this.getContext(), LinkEditorActivity.class);
        Integer linkID = linkList.get(position).getLinkId();
        intent.putExtra("link_id", linkID);
        startActivityForResult(intent, LINK_EDITOR_REQUEST_CODE);
    }

    @Override
    public void onDeleteLink(int position) {
        Log.i(TAG, "position = " + position);
        Integer linkID = linkList.get(position).getLinkId();
        String linkName = linkList.get(position).getLinkName();
        Log.i(TAG, "link name = " + linkName);
        mLinkViewModel.deleteLink(linkID);
        Toast toast =
                Toast.makeText(this.getContext(), "Deleted " + linkName, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
