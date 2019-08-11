package com.tf.simplefilebrowser.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.tf.simplefilebrowser.activities.ArchiveViewActivity;
import com.tf.simplefilebrowser.viewholders.ArchiveViewViewHolder;

import java.util.LinkedList;
import java.util.zip.ZipEntry;

public class ArchiveViewAdapter extends RecyclerView.Adapter<ArchiveViewViewHolder> {
    private final ArchiveViewActivity activity;
    private LinkedList<ZipEntry> mFiles;
    public ArchiveViewAdapter(LinkedList<ZipEntry> files, ArchiveViewActivity activity){
        mFiles = files;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ArchiveViewViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        return new ArchiveViewViewHolder(inflater,viewGroup, activity);
    }

    @Override
    public void onBindViewHolder(@NonNull ArchiveViewViewHolder archiveViewViewHolder, int i) {
        archiveViewViewHolder.bind(mFiles.get(i));
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    public void setFiles(LinkedList<ZipEntry> files){
        mFiles = files;
    }
}
