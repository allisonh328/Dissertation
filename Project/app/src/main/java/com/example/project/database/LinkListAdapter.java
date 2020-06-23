package com.example.project.database;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;

import java.util.List;

public class LinkListAdapter extends RecyclerView.Adapter<LinkListAdapter.LinkViewHolder> {

    class LinkViewHolder extends RecyclerView.ViewHolder {
        private final TextView linkItemView;

        private LinkViewHolder(View itemView) {
            super(itemView);
            linkItemView = itemView.findViewById(R.id.textView);
        }
    }

    private final LayoutInflater mInflater;
    private List<Link> mLinks; // Cached copy of links

    LinkListAdapter(Context context) { mInflater = LayoutInflater.from(context); }

    @Override
    public LinkViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        return new LinkViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(LinkViewHolder holder, int position) {
        if (mLinks != null) {
            Link current = mLinks.get(position);
            String displayText = current.getLinkName() + Integer.toString(current.getLinkId());
            holder.linkItemView.setText(displayText);
        } else {
            // Covers the case of data not being ready yet.
            String displayText = "No links";
            holder.linkItemView.setText(displayText);
        }
    }

    void setLinks(List<Link> links){
        mLinks = links;
        notifyDataSetChanged();
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
