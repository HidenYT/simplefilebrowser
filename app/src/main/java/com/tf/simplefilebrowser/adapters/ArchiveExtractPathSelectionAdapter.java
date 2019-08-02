package com.tf.simplefilebrowser.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.tf.simplefilebrowser.viewholders.ArchiveExtractPathSelectionVH;
import com.tf.simplefilebrowser.activities.ArchiveViewActivity;

import java.io.File;
import java.util.List;

public class ArchiveExtractPathSelectionAdapter extends RecyclerView.Adapter<ArchiveExtractPathSelectionVH> {
    List<File> mFiles;
    Activity mContext;
    ArchiveViewActivity.PathSelectionWindow mWindow;
    public ArchiveExtractPathSelectionAdapter(List<File> files, Activity context, ArchiveViewActivity.PathSelectionWindow window){
        mFiles = files;
        mContext = context;
        mWindow = window;
    }
    @Override
    public ArchiveExtractPathSelectionVH onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        return new ArchiveExtractPathSelectionVH(inflater, viewGroup, mWindow);
    }

    @Override
    public void onBindViewHolder(ArchiveExtractPathSelectionVH archiveExtractPathSelectionVH, int i) {
        archiveExtractPathSelectionVH.bind(mFiles.get(i), this, mContext);
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }
    public void setFiles(List<File > files){
        mFiles = files;
    }
}
