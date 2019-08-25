package com.touchforce.pathselectiondialog;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.io.File;
import java.util.LinkedList;

class PathSelectionDialogAdapter extends RecyclerView.Adapter<PathSelectionDialogViewHolder> {
    private LinkedList<File> files;
    private PathSelectionDialog dialog;
    PathSelectionDialogAdapter(LinkedList<File> files, PathSelectionDialog dialog){
        this.files = files;
        this.dialog = dialog;
    }
    @NonNull
    @Override
    public PathSelectionDialogViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ConstraintLayout v = (ConstraintLayout)LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_element, viewGroup, false);
        return new PathSelectionDialogViewHolder(v, dialog);
    }

    @Override
    public void onBindViewHolder(@NonNull PathSelectionDialogViewHolder pathSelectionDialogViewHolder, int i) {
        pathSelectionDialogViewHolder.bind(files.get(i));
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public void setFiles(LinkedList<File> files){this.files = files;}
}
