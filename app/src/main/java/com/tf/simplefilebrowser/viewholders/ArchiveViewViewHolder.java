package com.tf.simplefilebrowser.viewholders;

import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tf.simplefilebrowser.ActionMenus.ZipExtractionMenu;
import com.tf.simplefilebrowser.R;
import com.tf.simplefilebrowser.activities.ArchiveViewActivity;
import com.tf.simplefilebrowser.helpers.FileFoldersLab;
import com.tf.simplefilebrowser.helpers.ZipArchiveHelper;

import java.io.File;
import java.util.zip.ZipEntry;

public class ArchiveViewViewHolder extends RecyclerView.ViewHolder {
    private TextView mFileTitle;
    private ImageView mFileIcon;
    private View mItemView;
    private ConstraintLayout mFileBG;
    private ArchiveViewActivity activity;
    public ArchiveViewViewHolder(LayoutInflater inflater, ViewGroup parent, ArchiveViewActivity activity) {
        super(inflater.inflate(R.layout.list_item_files, parent, false));
        mFileTitle = itemView.findViewById(R.id.file_title_view);
        mFileIcon = itemView.findViewById(R.id.file_icon_view);
        mFileBG = itemView.findViewById(R.id.file_background_view);
        mItemView = itemView;
        this.activity = activity;
    }
    public void bind(final ZipEntry entry){
        if(activity.selection.fileIsSelected(entry.getName())){
            mFileBG.setBackgroundResource(R.drawable.ripple_green);
        }else{
            mFileBG.setBackgroundResource(R.drawable.ripple_default);
        }
        if(entry.isDirectory()){
            mFileIcon.setImageResource(R.drawable.ic_folder_icon);
            String path = entry.getName().substring(0,entry.getName().length()-1);
            if(path.contains("/"))
                mFileTitle.setText(path.substring(path.lastIndexOf("/")+1));
            else
                mFileTitle.setText(path);
            mItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(activity.getActionMode() != null)
                        activity.getActionMode().finish();
                    activity.selection.getSelectedFiles().clear();
                    activity.CUR_ZIP_VIEW_PATH = entry.getName();
                    activity.updateUI();
                }
            });
        }else{
            mFileIcon.setImageResource(R.drawable.ic_file_icon);
            if(entry.getName().contains("/"))
                mFileTitle.setText(entry.getName().substring(entry.getName().lastIndexOf("/")+1));
            else
                mFileTitle.setText(entry.getName());
            mItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String tmpFold = FileFoldersLab.get(activity).TMP_FOLDER_PATH;
                    ZipArchiveHelper.get(activity, activity.getContentResolver()).unzipFile(
                            activity.archive.getAbsolutePath(),
                            tmpFold,entry.getName());
                    FileFoldersLab.get(activity).openFile(new File(tmpFold + File.separator +
                            entry.getName().substring(entry.getName().lastIndexOf("/")+1)));
                }
            });
        }
        mFileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(activity.selection.getSelectedFiles().size() == 0){
                    activity.startActionMode(new ZipExtractionMenu(activity));
                    mFileBG.setBackgroundResource(R.drawable.ripple_green);
                    activity.selection.addFileToSelection(entry.getName());
                    if(entry.isDirectory()){
                        for(ZipEntry i : activity.getEntries()){
                            if(i.getName().startsWith(entry.getName()) &&
                            !i.getName().equals(entry.getName())){
                                activity.selection.addFileToSelection(i.getName());
                            }
                        }
                    }
                }else{
                    if(activity.selection.fileIsSelected(entry.getName())){
                        mFileBG.setBackgroundResource(R.drawable.ripple_default);
                        activity.selection.removeFileFromSelection(entry.getName());
                        if(entry.isDirectory()){
                            for(ZipEntry i : activity.getEntries()){
                                if(i.getName().startsWith(entry.getName()) &&
                                !i.getName().equals(entry.getName())){
                                    activity.selection.removeFileFromSelection(i.getName());
                                }
                            }
                        }
                        if(activity.selection.getSelectedFiles().size() == 0){
                            activity.getActionMode().finish();
                        }
                    }else{
                        mFileBG.setBackgroundResource(R.drawable.ripple_green);
                        activity.selection.addFileToSelection(entry.getName());
                        if(entry.isDirectory()){
                            for(ZipEntry i : activity.getEntries()){
                                if(i.getName().startsWith(entry.getName())
                                && !i.getName().equals(entry.getName())){
                                    activity.selection.addFileToSelection(i.getName());
                                }
                            }
                        }
                    }
                }
                Log.d("TAG", "onClick: " + activity.selection.getSelectedFiles().size());
                for (String s:
                        activity.selection.getSelectedFiles()) {
                    Log.d("TAG", "onClick: " + s);
                }
            }
        });
    }
}
