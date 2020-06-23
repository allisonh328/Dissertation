package com.example.project.database;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;

import java.util.List;

public class VersionListAdapter extends RecyclerView.Adapter<VersionListAdapter.VersionViewHolder>{
    class VersionViewHolder extends RecyclerView.ViewHolder {
        private final TextView versionItemView;

        private VersionViewHolder(View itemView) {
            super(itemView);
            versionItemView = itemView.findViewById(R.id.textView);
        }
    }

    private final LayoutInflater mInflater;
    private List<ProtoVersion> mVersions; // Cached copy of links

    VersionListAdapter(Context context) { mInflater = LayoutInflater.from(context); }

    @Override
    public VersionListAdapter.VersionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        return new VersionListAdapter.VersionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(VersionListAdapter.VersionViewHolder holder, int position) {
        if (mVersions != null) {
            ProtoVersion current = mVersions.get(position);
            String displayText = current.getVersionName() + Integer.toString(current.getVersionId());
            holder.versionItemView.setText(displayText);
        } else {
            // Covers the case of data not being ready yet.
            String displayText = "No versions";
            holder.versionItemView.setText(displayText);
        }
    }

    void setVersions(List<ProtoVersion> versions){
        mVersions = versions;
        notifyDataSetChanged();
    }

    // getItemCount() is called many times, and when it is first called,
    // mLinks has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (mVersions != null)
            return mVersions.size();
        else return 0;
    }
}
