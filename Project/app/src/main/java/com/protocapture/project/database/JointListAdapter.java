package com.protocapture.project.database;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.protocapture.project.R;

import java.util.List;

public class JointListAdapter extends RecyclerView.Adapter<JointListAdapter.JointViewHolder> {

    class JointViewHolder extends RecyclerView.ViewHolder {
        private final TextView jointItemView;
        private final Button deleteButton;

        private JointViewHolder(View itemView) {
            super(itemView);
            jointItemView = itemView.findViewById(R.id.textView);
            deleteButton = itemView.findViewById(R.id.button_delete);
        }
    }

    //https://www.youtube.com/watch?v=69C1ljfDvl0
    public interface OnJointListener {
        void onJointClick(int position);
        void onDeleteJoint(int position);
    }

    private final LayoutInflater mInflater;
    private List<Joint> mJoints; // Cached copy of links
    private JointListAdapter.OnJointListener onJointListener;

    public JointListAdapter(Context context, JointListAdapter.OnJointListener onJointListener) {
        mInflater = LayoutInflater.from(context);
        this.onJointListener = onJointListener;
    }

    @Override
    public JointListAdapter.JointViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        return new JointListAdapter.JointViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(JointListAdapter.JointViewHolder holder, int position) {
        if (mJoints != null) {
            Joint current = mJoints.get(position);
            String displayText = current.getJointName();
            holder.jointItemView.setText(displayText);
            holder.jointItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onJointListener.onJointClick(position);
                }
            });
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onJointListener.onDeleteJoint(position);
                }
            });
        } else {
            // Covers the case of data not being ready yet.
            String displayText = "No joints";
            holder.jointItemView.setText(displayText);
        }
    }

    public void setJoints(List<Joint> joints){
        mJoints = joints;
    }

    // getItemCount() is called many times, and when it is first called,
    // mLinks has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (mJoints!= null)
            return mJoints.size();
        else return 0;
    }
}
