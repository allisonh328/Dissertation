package com.protocapture.project;

import android.content.Intent;
import android.os.Bundle;
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

import com.protocapture.project.activities.JointEditorActivity;
import com.protocapture.project.database.Joint;
import com.protocapture.project.database.JointListAdapter;
import com.protocapture.project.database.JointViewModel;

import java.util.List;

public class JointFragment extends Fragment implements JointListAdapter.OnJointListener {
    public static final int JOINT_EDITOR_REQUEST_CODE = 1;

    private JointViewModel mJointViewModel;
    private List<Joint> jointList;
    private Integer prototypeID;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        prototypeID = getArguments().getInt("prototype_id");
        View view = inflater.inflate(R.layout.fragment_component_page, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //((TextView) view.findViewById(android.R.id.text1))
        //.setText(Integer.toString(args.getInt(ARG_OBJECT)));
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        final JointListAdapter adapter = new JointListAdapter(this.getContext(), this);

        mJointViewModel = new ViewModelProvider(this.getActivity()).get(JointViewModel.class);
        mJointViewModel.setAllJoints(prototypeID);
        mJointViewModel.getAllProtoJoints(prototypeID).observe(this.getViewLifecycleOwner(), new Observer<List<Joint>>() {
            @Override
            public void onChanged(@Nullable final List<Joint> joints) {
                // Update the cached copy of the words in the adapter.
                adapter.setJoints(joints);
                jointList = joints;
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            }
        });
    }

    @Override
    public void onJointClick(int position) {
        Intent intent = new Intent(this.getContext(), JointEditorActivity.class);
        Integer jointID = jointList.get(position).getJointId();
        intent.putExtra("joint_id", jointID);
        startActivityForResult(intent, JOINT_EDITOR_REQUEST_CODE);
    }

    @Override
    public void onDeleteClick(int position) {
        Integer jointID = jointList.get(position).getJointId();
        String jointName = jointList.get(position).getJointName();
        mJointViewModel.deleteJoint(jointID);
        Toast toast =
                Toast.makeText(this.getContext(), "Deleted " + jointName, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
