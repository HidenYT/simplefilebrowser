package com.tf.simplefilebrowser.viewholders;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tf.simplefilebrowser.activities.ArchiveViewActivity;
import com.tf.simplefilebrowser.adapters.ArchiveExtractPathSelectionAdapter;
import com.tf.simplefilebrowser.helpers.FileFoldersLab;
import com.tf.simplefilebrowser.R;

import java.io.File;

public class ArchiveExtractPathSelectionVH extends RecyclerView.ViewHolder {
    private TextView mFileTitle;
    private ImageView mFileIcon;
    private File mFile;
    private View mItemView;
    public ArchiveViewActivity.PathSelectionWindow mWindow;
    public ArchiveExtractPathSelectionVH(LayoutInflater inflater, ViewGroup parent, ArchiveViewActivity.PathSelectionWindow window) {
        super(inflater.inflate(R.layout.list_item_files, parent, false));
        mFileTitle = (TextView) itemView.findViewById(R.id.file_title_view);
        mFileIcon = (ImageView) itemView.findViewById(R.id.file_icon_view);
        mItemView = itemView;
        mWindow = window;
    }
    public void bind(final File file, final ArchiveExtractPathSelectionAdapter adapter, final Activity context){
        mFileTitle.setText(file.getName());
        mItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(file.isDirectory()){
                    mWindow.pathSelectionCurPath = file.getAbsolutePath();
                    adapter.setFiles(FileFoldersLab.get(context)
                            .loadFilesFromPath(file.getAbsolutePath()));
                    adapter.notifyDataSetChanged();
                }
            }
        });
        if(file.isDirectory()){
            mFileIcon.setImageResource(R.drawable.ic_folder_icon);
        }else{
            mFileIcon.setImageResource(R.drawable.ic_file_icon);
        }
    }
}
