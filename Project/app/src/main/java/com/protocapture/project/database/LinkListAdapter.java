package com.protocapture.project.database;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.protocapture.project.R;

import java.util.List;

public class LinkListAdapter extends RecyclerView.Adapter<LinkListAdapter.LinkViewHolder> {

    private final static String TAG = "ALLISON";

    class LinkViewHolder extends RecyclerView.ViewHolder {
        private final TextView linkItemView;
        private final Button deleteButton;

        private LinkViewHolder(View itemView) {
            super(itemView);
            linkItemView = itemView.findViewById(R.id.textView);
            deleteButton = itemView.findViewById(R.id.button_delete);
        }
    }

    //https://www.youtube.com/watch?v=69C1ljfDvl0
    public interface OnLinkListener {
        void onLinkClick(int position);
        void onDeleteClick(int position);
    }

    private final LayoutInflater mInflater;
    private List<Link> mLinks; // Cached copy of links
    private OnLinkListener onLinkListener;

    public LinkListAdapter(Context context, OnLinkListener onLinkListener) {
        mInflater = LayoutInflater.from(context);
        this.onLinkListener = onLinkListener;
    }

    @Override
    public LinkViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        return new LinkListAdapter.LinkViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(LinkViewHolder holder, int position) {
        if (mLinks != null) {
            Link current = mLinks.get(position);
            String displayText = current.getLinkName() + Integer.toString(current.getLinkId());
            holder.linkItemView.setText(displayText);
            Log.d(TAG, "LinkListAdapter.onBindViewHolder: " + displayText);
            holder.linkItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onLinkListener.onLinkClick(position);
                }
            });
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onLinkListener.onDeleteClick(position);
                }
            });
        } else {
            // Covers the case of data not being ready yet.
            String displayText = "No links";
            Log.d(TAG, "LinkListAdapter.onBindViewHolder: " + displayText);
            holder.linkItemView.setText(displayText);
        }
    }

    public void setLinks(List<Link> links){
        mLinks = links;
    }

    // getItemCount() is called many times, and when it is first called,
    // mLinks has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (mLinks != null)
            return mLinks.size();
        else return 0;
    }
}
