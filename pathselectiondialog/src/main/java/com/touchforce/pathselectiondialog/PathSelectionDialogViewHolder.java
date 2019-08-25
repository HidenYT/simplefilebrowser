package com.touchforce.pathselectiondialog;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

class PathSelectionDialogViewHolder extends RecyclerView.ViewHolder {
    private final PathSelectionDialog dialog;
    private TextView mFileName;
    private View mItemView;
    private ImageView mFileIcon;
    PathSelectionDialogViewHolder(@NonNull View itemView, PathSelectionDialog dialog) {
        super(itemView);
        mFileName = itemView.findViewById(R.id.file_title);
        mFileIcon = itemView.findViewById(R.id.file_icon);
        mItemView = itemView;
        this.dialog = dialog;
    }
    public void bind(final File file){
        mFileName.setText(file.getName());
        mItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(file.isDirectory()){
                    dialog.setCurPath(file.getAbsolutePath());
                    dialog.getAdapter().setFiles(FilesHelper.getFilesInPath(file.getAbsolutePath()));
                    dialog.getAdapter().notifyDataSetChanged();
                }
            }
        });
        if(file.isDirectory()){
            mFileIcon.setImageResource(R.drawable.ic_folder);
        }
    }
}
