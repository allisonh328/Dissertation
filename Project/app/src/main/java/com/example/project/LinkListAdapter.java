package com.example.project;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

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
    private List<Prototype> mPrototypes; // Cached copy of links

    LinkListAdapter(Context context) { mInflater = LayoutInflater.from(context); }

    @Override
    public LinkViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        return new LinkViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(LinkViewHolder holder, int position) {
        if (mPrototypes != null) {
            Prototype current = mPrototypes.get(position);
            holder.linkItemView.setText(current.getLinkName());
        } else {
            // Covers the case of data not being ready yet.
            holder.linkItemView.setText("No Word");
        }
    }

    void setLinks(List<Prototype> prototypes){
        mPrototypes = prototypes;
        notifyDataSetChanged();
    }

    // getItemCount() is called many times, and when it is first called,
    // mPrototypes has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (mPrototypes != null)
            return mPrototypes.size();
        else return 0;
    }
}
