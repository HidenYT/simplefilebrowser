package com.tf.simplefilebrowser.viewholders;

import android.graphics.Bitmap;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tf.simplefilebrowser.ActionMenus.ActionSelectMenu;
import com.tf.simplefilebrowser.alertdialogs.LongTouchMenuDialogCreator;
import com.tf.simplefilebrowser.fragments.FileExplorerFragment;
import com.tf.simplefilebrowser.R;
import com.tf.simplefilebrowser.helpers.FileFoldersLab;
import com.tf.simplefilebrowser.helpers.SelectionHelper;
import com.tf.simplefilebrowser.helpers.ThumbnailsHelper;

import java.io.File;

public class FileExplorerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
    private TextView mFileTitle;
    private ImageView mFileIcon;
    public File mFile;
    private ConstraintLayout mFileBackground;
    private FileExplorerFragment mFragment;
    private boolean loadedImage = false;
    private FileExplorerViewHolder fevh;
    public FileExplorerViewHolder(LayoutInflater inflater, ViewGroup parent, FileExplorerFragment fragment) {
        super(inflater.inflate(R.layout.list_item_files, parent, false));
        mFileTitle = (TextView) itemView.findViewById(R.id.file_title_view);
        mFileBackground = itemView.findViewById(R.id.file_background_view);
        mFileIcon = (ImageView) itemView.findViewById(R.id.file_icon_view);
        mFragment = fragment;
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
        fevh = this;
    }

    public void recycle(){
        final String mimeType = FileFoldersLab.getFileMimeType(mFile);
        if(mimeType != null){
            if(mimeType.startsWith("video/") || mimeType.startsWith("image/")){
                loadedImage = false;
            }
        }
    }

    public void createThumbnail(){
        final String mimeType = FileFoldersLab.getFileMimeType(mFile);
        if(mimeType != null){
            final Bitmap[] btm = {null};
            if((mimeType.startsWith("video/") || mimeType.startsWith("image/")) && !loadedImage){
                if(mimeType.startsWith("video/")){
                    btm[0] = ThumbnailsHelper.createThumbForVideo(mFile.getAbsolutePath());
                }else if(mimeType.startsWith("image/")){
                    btm[0] = ThumbnailsHelper.createThumbForPic(mFile.getAbsolutePath());
                }
                mFragment.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mFileIcon.setImageBitmap(btm[0]);
                        loadedImage = true;
                    }
                });
            }
        }
    }
    public void bind(final File file) {
        mFile = file;
        mFileTitle.setText(file.getName());
        if (file.isFile()){
            mFileIcon.setImageResource(R.drawable.ic_file_icon);
            mFileIcon.setContentDescription(mFragment.getString(R.string.file_type_sting));
        }else{
            mFileIcon.setImageResource(R.drawable.ic_folder_icon);
            mFileIcon.setContentDescription(mFragment.getString(R.string.folder_type_sting));
        }
        if(SelectionHelper.get(mFragment.getActivity()).isSelected(file.getAbsolutePath())){
            mFileBackground.setBackgroundResource(R.drawable.ripple_green);
        }else{
            mFileBackground.setBackgroundResource(R.drawable.ripple_default);
        }
        mFileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mFragment.mFilesActionActive){
                    SelectionHelper sh = SelectionHelper.get(mFragment.getActivity());
                    if(sh.getSelectedFiles().size() == 0){
                        mFileBackground.setBackgroundResource(R.drawable.ripple_green);
                        sh.addSelectedFile(file.getAbsolutePath());
                        mFragment.getActivity().startActionMode(new ActionSelectMenu(mFragment));
                    }else{
                        if(sh.isSelected(file.getAbsolutePath())){
                            mFileBackground.setBackgroundResource(R.drawable.ripple_default);
                            sh.removeSelectedFile(file.getAbsolutePath());
                            if(sh.getSelectedFiles().size() == 0){
                                if(mFragment.getActionSelectActionMode() !=null)
                                    mFragment.getActionSelectActionMode().finish();
                            }
                        }else{
                            mFileBackground.setBackgroundResource(R.drawable.ripple_green);
                            sh.addSelectedFile(file.getAbsolutePath());
                        }
                    }
                }
            }
        });
    }
    @Override
    public void onClick(View v) {
        if(mFile.isFile()){
            FileFoldersLab.get(mFragment.getActivity()).openFile(mFile);
        }else{
            FileFoldersLab.get(mFragment.getActivity()).setCurPath(mFile.getAbsolutePath());
            if(!mFragment.mFilesActionActive){
                SelectionHelper.get(mFragment.getActivity()).getSelectedFiles().clear();
                if(mFragment.getActionSelectActionMode() != null){
                    mFragment.getActionSelectActionMode().finish();
                }
            }
            mFragment.updateUI(true);
            mFragment.mRecyclerView.getLayoutManager().scrollToPosition(0);
        }
    }
    @Override
    public boolean onLongClick(View v) {
        /*FragmentManager manager = mFragment.getFragmentManager();
        AlertDialogHelper fragment = AlertDialogHelper.LongTouchMenu.newInstance(mFile.getAbsolutePath());
        fragment.setTargetFragment(mFragment, AlertDialogHelper.LongTouchMenu.REQUEST_FILE_ACTION);
        fragment.show(manager, FileExplorerFragment.DIALOG_FILE_ACTION);*/
        LongTouchMenuDialogCreator dialogCreator =
                new LongTouchMenuDialogCreator(mFragment.getActivity(),mFile);
        dialogCreator.setActionListener(mFragment);
        dialogCreator.createDialog();
        if(mFragment.getActionSelectActionMode() != null){
            mFragment.getActionSelectActionMode().finish();
            mFragment.setActionSelectActionMode(null);
        }
        SelectionHelper.get(mFragment.getActivity()).getSelectedFiles().clear();
        mFragment.updateUInoDataChanged();
        return true;
    }
}
