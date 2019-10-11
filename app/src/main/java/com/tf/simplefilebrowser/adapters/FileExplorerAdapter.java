package com.tf.simplefilebrowser.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.tf.simplefilebrowser.fragments.FileExplorerFragment;
import com.tf.simplefilebrowser.helpers.PreferencesHelper;
import com.tf.simplefilebrowser.viewholders.FileExplorerViewHolder;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class FileExplorerAdapter extends RecyclerView.Adapter<FileExplorerViewHolder>  {
    private List<File> mFiles;
    private FileExplorerFragment mFragment;
    public FileExplorerAdapter(FileExplorerFragment fragment, List<File> files){
        mFiles = files;
        mFragment = fragment;
        mFiles = filterFiles();
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
        //filterFiles();
        return mFiles.size();
    }
    public void setFiles(List<File> files){
        mFiles = files;
        mFiles = filterFiles();
    }

    private List<File> filterFiles(){

        List<File> filesCopy = mFiles.subList(0, mFiles.size());
        LinkedList<File> newFiles = new LinkedList<>();
        if(!PreferencesHelper.getInstance(mFragment.getActivity()).isDisplayingHiddenFiles()){
            for (int i = 0; i < mFiles.size(); i++){
                Log.d("TAG", "filterFiles: " + mFiles.get(i).getName());
                if(!filesCopy.get(i).getName().startsWith(".")){
                    newFiles.add(filesCopy.get(i));
                }
            }
            return newFiles;
        }
        return filesCopy;
    }

}
