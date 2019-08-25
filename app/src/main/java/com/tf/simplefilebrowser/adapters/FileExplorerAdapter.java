package com.tf.simplefilebrowser.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.tf.simplefilebrowser.fragments.FileExplorerFragment;
import com.tf.simplefilebrowser.viewholders.FileExplorerViewHolder;

import java.io.File;
import java.util.List;

public class FileExplorerAdapter extends RecyclerView.Adapter<FileExplorerViewHolder>  {
    List<File> mFiles;
    FileExplorerFragment mFragment;
    public FileExplorerAdapter(FileExplorerFragment fragment, List<File> files){
        mFiles = files;
        mFragment = fragment;
    }

    @Override
    public FileExplorerViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(mFragment.getActivity());
        FileExplorerViewHolder v = new FileExplorerViewHolder(layoutInflater, viewGroup, mFragment);
        return v;
    }

    @Override
    public void onBindViewHolder(FileExplorerViewHolder fileExplorerViewHolder, int i) {
        fileExplorerViewHolder.bind(mFiles.get(i));
    }

    @Override
    public void onViewRecycled(@NonNull FileExplorerViewHolder holder) {
        super.onViewRecycled(holder);
        holder.recycle();
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }
    public void setFiles(List<File> files){
        mFiles = files;
    }



}
