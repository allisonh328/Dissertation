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

public class PrototypeListAdapter extends RecyclerView.Adapter<PrototypeListAdapter.PrototypeViewHolder>{
    private final static String TAG = "ALLISON";

    class PrototypeViewHolder extends RecyclerView.ViewHolder {
        private final TextView prototypeItemView;
        private final Button deleteButton;

        private PrototypeViewHolder(View itemView) {
            super(itemView);
            prototypeItemView = itemView.findViewById(R.id.textView);
            deleteButton = itemView.findViewById(R.id.button_delete);
        }
    }

    //https://www.youtube.com/watch?v=69C1ljfDvl0
    public interface OnProtoListener {
        void onProtoClick(int position);
        void onDeleteClick(int position);
    }

    private final LayoutInflater mInflater;
    private List<Prototype> mPrototypes; // Cached copy of prototypes
    private OnProtoListener onProtoListener;

    public PrototypeListAdapter(Context context, OnProtoListener onProtoListener) {
        mInflater = LayoutInflater.from(context);
        this.onProtoListener = onProtoListener;
    }

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
            Log.d(TAG, "PrototypeListAdapter.onBindViewHolder: " + displayText);
            holder.prototypeItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onProtoListener.onProtoClick(position);
                }
            });
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onProtoListener.onDeleteClick(position);
                }
            });
        } else {
            // Covers the case of data not being ready yet.
            String displayText = "No prototype";
            Log.d(TAG, "PrototypeListAdapter.onBindViewHolder: " + displayText);
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
