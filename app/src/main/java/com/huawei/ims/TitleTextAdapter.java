package com.huawei.ims;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class TitleTextAdapter extends RecyclerView.Adapter {
    public ArrayList<String> mTitles = new ArrayList<>();
    public ArrayList<String> mReady = new ArrayList<>();
    public ArrayList<String> mStatus = new ArrayList<>();

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        return new DataItem(LayoutInflater.from(parent.getContext()).inflate(R.layout.data_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        ((DataItem) viewHolder).setTitle(mTitles.get(i));
        ((DataItem) viewHolder).setReady(mReady.get(i));
    }

    @Override
    public int getItemCount() {
        return mTitles.size();
    }

    public static class DataItem extends RecyclerView.ViewHolder {

        public DataItem(@NonNull View itemView) {
            super(itemView);
        }

        public void setTitle(String s) {
            ((TextView) itemView.findViewById(R.id.title)).setText(s);
        }

        public void setReady(String s) {
            ((TextView) itemView.findViewById(R.id.ready)).setText(s);
        }

        public void setStatus(String s) {
            ((TextView) itemView.findViewById(R.id.status)).setText(s);
        }
    }
}
