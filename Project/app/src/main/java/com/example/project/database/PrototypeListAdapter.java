package com.example.project.database;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;

import java.util.List;

public class PrototypeListAdapter extends RecyclerView.Adapter<PrototypeListAdapter.PrototypeViewHolder>{

    class PrototypeViewHolder extends RecyclerView.ViewHolder {
        private final TextView prototypeItemView;

        private PrototypeViewHolder(View itemView) {
            super(itemView);
            prototypeItemView = itemView.findViewById(R.id.textView);
        }
    }

    private final LayoutInflater mInflater;
    private List<Prototype> mPrototypes; // Cached copy of prototypes

    public PrototypeListAdapter(Context context) { mInflater = LayoutInflater.from(context); }

    @Override
    public PrototypeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        return new PrototypeListAdapter.PrototypeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PrototypeListAdapter.PrototypeViewHolder holder, int position) {
        if (mPrototypes != null) {
            Prototype current = mPrototypes.get(position);
            String displayText = current.getPrototypeName() + Integer.toString(current.getPrototypeId());
            holder.prototypeItemView.setText(displayText);
        } else {
            // Covers the case of data not being ready yet.
            String displayText = "No prototype";
            holder.prototypeItemView.setText(displayText);
        }
    }

    public void setPrototypes(List<Prototype> prototypes){
        mPrototypes = prototypes;
        notifyDataSetChanged();
    }

    // getItemCount() is called many times, and when it is first called,
    // mLinks has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (mPrototypes != null)
            return mPrototypes.size();
        else return 0;
    }
}
