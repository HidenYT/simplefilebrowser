package com.tf.simplefilebrowser.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import com.tf.simplefilebrowser.adapters.ArchiveExtractPathSelectionAdapter;
import com.tf.simplefilebrowser.adapters.ArchiveViewAdapter;
import com.tf.simplefilebrowser.helpers.FileFoldersLab;
import com.tf.simplefilebrowser.R;
import com.tf.simplefilebrowser.helpers.ZipArchiveHelper;
import com.tf.simplefilebrowser.helpers.ZipFileSelection;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ArchiveViewActivity extends Activity {
    private static final String EXTRA_ARCHIVE = "com.tf.simplefilebrowser.archive";
    public RecyclerView mRecyclerView;
    private Activity mContext;
    public File archive;
    private ArchiveViewAdapter mAdapter;
    public ZipFileSelection selection;
    public ActionMode mActionMode;
    private final String TAG = "TAG";
    private Toolbar mToolbar;
    public LinkedList<ZipEntry> mEntries = new LinkedList<>();
    public static Intent newIntent(Context packageContext, File archive) {
        Intent intent = new Intent(packageContext, ArchiveViewActivity.class);
        intent.putExtra(EXTRA_ARCHIVE, archive);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        return intent;
    }
    public String CUR_ZIP_VIEW_PATH="";
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive_view);
        mContext = this;
        archive = (File) getIntent().getSerializableExtra(EXTRA_ARCHIVE);
        mRecyclerView = (RecyclerView) findViewById(R.id.archive_view_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        selection = new ZipFileSelection(this);
        mToolbar = findViewById(R.id.toolbar);
        setActionBar(mToolbar);
        getActionBar().setTitle("Archive preview");
        overridePendingTransition(0,0);
        try {
            mEntries = ZipArchiveHelper.get(this, getContentResolver()).getAllEntries(archive.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        updateUI();
    }
    /*public class ArchiveViewViewHolder extends RecyclerView.ViewHolder{
        TextView mFileTitle;
        ImageView mFileIcon;
        View mItemView;
        ConstraintLayout mFileBG;
        ArchiveViewViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_files, parent, false));
            mFileTitle = itemView.findViewById(R.id.file_title_view);
            mFileIcon = itemView.findViewById(R.id.file_icon_view);
            mFileBG = itemView.findViewById(R.id.file_background_view);
            mItemView = itemView;
        }
        public void bind(final ZipEntry entry){
            if(selection.fileIsSelected(entry.getName())){
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
                        CUR_ZIP_VIEW_PATH = entry.getName();
                        updateUI();
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
                        String tmpFold = FileFoldersLab.get(mContext).TMP_FOLDER_PATH;
                        ZipArchiveHelper.get(mContext, getContentResolver()).unzipFile(archive.getAbsolutePath(),
                                tmpFold,entry.getName());
                        FileFoldersLab.get(mContext).openFile(new File(tmpFold + File.separator +
                                entry.getName().substring(entry.getName().lastIndexOf("/")+1)));
                    }
                });
            }
            mFileIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(selection.getSelectedFiles().size() == 0){
                        startActionMode(new ExtractMenu());
                        mFileBG.setBackgroundResource(R.drawable.ripple_green);
                        selection.addFileToSelection(entry.getName());
                        if(entry.isDirectory()){
                            for(ZipEntry i : mEntries){
                                if(i.getName().startsWith(entry.getName())){
                                    selection.addFileToSelection(i.getName());
                                }
                            }
                        }
                    }else{
                        if(selection.fileIsSelected(entry.getName())){
                            mFileBG.setBackgroundResource(R.drawable.ripple_default);
                            selection.removeFileFromSelection(entry.getName());
                            if(entry.isDirectory()){
                                for(ZipEntry i : mEntries){
                                    if(i.getName().startsWith(entry.getName())){
                                        selection.removeFileFromSelection(i.getName());
                                    }
                                }
                            }
                            if(selection.getSelectedFiles().size() == 0){
                                mActionMode.finish();
                            }
                        }else{
                            mFileBG.setBackgroundResource(R.drawable.ripple_green);
                            selection.addFileToSelection(entry.getName());
                            if(entry.isDirectory()){
                                for(ZipEntry i : mEntries){
                                    if(i.getName().startsWith(entry.getName())){
                                        selection.addFileToSelection(i.getName());
                                    }
                                }
                            }
                        }
                    }

                }
            });
        }
    }
    public class ArchiveViewAdapter extends RecyclerView.Adapter<ArchiveViewViewHolder>{
        LinkedList<ZipEntry> mFiles;
        public ArchiveViewAdapter(LinkedList<ZipEntry> files){
            mFiles = files;
        }

        @Override
        public ArchiveViewViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            return new ArchiveViewViewHolder(inflater,viewGroup);
        }

        @Override
        public void onBindViewHolder(ArchiveViewViewHolder archiveViewViewHolder, int i) {
            archiveViewViewHolder.bind(mFiles.get(i));
        }

        @Override
        public int getItemCount() {
            return mFiles.size();
        }

        public void setFiles(LinkedList<ZipEntry> files){
            mFiles = files;
        }
    }*/
    public void updateUI() {
        LinkedList<ZipEntry> f = FileFoldersLab.sortZipEntries(getEntriesCurPath());
        if(mAdapter == null){
            mAdapter = new ArchiveViewAdapter(getEntriesCurPath(), this);
            mRecyclerView.setAdapter(mAdapter);
        }else{
            mAdapter.setFiles(f);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP){
            if(CUR_ZIP_VIEW_PATH.equals("")){
                FileFoldersLab.get(this).removeTmpFolder();
                FileFoldersLab.get(this).prepareEnvironment();
                finish();
                overridePendingTransition(0,0);
            } else{
                CUR_ZIP_VIEW_PATH = CUR_ZIP_VIEW_PATH.substring(0, CUR_ZIP_VIEW_PATH.length()-1);
                CUR_ZIP_VIEW_PATH = CUR_ZIP_VIEW_PATH.substring(0, CUR_ZIP_VIEW_PATH.lastIndexOf("/")+1);
                updateUI();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    public class ExtractMenu implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mActionMode = mode;
            getMenuInflater().inflate(R.menu.extract_action_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if(item.getItemId() == R.id.extract_action_button){
                PathSelectionWindow psw = new PathSelectionWindow();
                psw.create().show();
            }
            if(item.getItemId() == R.id.menu_select_all){
                LinkedList<String> all = getNamesCurPath();
                selection.getSelectedFiles().clear();
                for(ZipEntry ze : mEntries){
                    if(all.contains(ze.getName())){
                        if(ze.isDirectory()){
                            for(ZipEntry i : mEntries){
                                if(i.getName().startsWith(ze.getName())){
                                    selection.addFileToSelection(i.getName());
                                }
                            }
                        }
                        selection.addFileToSelection(ze.getName());
                    }
                }
                updateUI();
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            selection.getSelectedFiles().clear();
            updateUI();
        }
    };

    private LinkedList<ZipEntry> getEntriesCurPath(){
        LinkedList<ZipEntry> files = new LinkedList<>();
        try {
            ZipFile zipFile = new ZipFile(archive.getAbsoluteFile());
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            ZipEntry entry;
            while(entries.hasMoreElements()){
                entry = entries.nextElement();
                if(entry.isDirectory()){
                    String path = entry.getName().substring(0,entry.getName().length()-1);
                    if(path.contains("/")){
                        if((path.substring(0,path.lastIndexOf("/"))+File.separator).equals(CUR_ZIP_VIEW_PATH)){
                            files.add(entry);
                        }
                    }else if(CUR_ZIP_VIEW_PATH.equals("")){
                        files.add(entry);
                    }
                }else{
                    if(entry.getName().contains("/")){
                        if((entry.getName().substring(0,entry.getName()
                                .lastIndexOf("/"))+File.separator).equals(CUR_ZIP_VIEW_PATH)){
                            files.add(entry);
                        }
                    }else if(CUR_ZIP_VIEW_PATH.equals("")){
                        files.add(entry);
                    }
                }
            }
            return FileFoldersLab.sortZipEntries(files);
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    private LinkedList<String> getNamesCurPath(){
        LinkedList<ZipEntry> all = getEntriesCurPath();
        LinkedList<String> s = new LinkedList<>();
        for(ZipEntry ze : all){
            s.add(ze.getName());
        }
        return s;
    }

    public class PathSelectionWindow{
        ArchiveExtractPathSelectionAdapter mAdapter;
        AlertDialog.Builder builder;
        public String pathSelectionCurPath;
        PathSelectionWindow(){
            builder = new AlertDialog.Builder(mContext);
            builder.setTitle("Select extraction path");
            View v = LayoutInflater.from(mContext).inflate(R.layout.recycler_view, null);
            RecyclerView recyclerView = v.findViewById(R.id.file_explorer_recycler_view);
            recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
            mAdapter = new ArchiveExtractPathSelectionAdapter(FileFoldersLab.get(mContext)
                    .loadFilesFromPath(Environment.getExternalStorageDirectory().getAbsolutePath()),
                    mContext, this);
            builder.setPositiveButton("Here", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            ZipArchiveHelper.get(mContext, getContentResolver()).unzipFiles(archive.getAbsolutePath(),
                                    pathSelectionCurPath,
                                    selection.getSelectedFiles(), CUR_ZIP_VIEW_PATH);
                        }
                    }).start();
                }
            });
            builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                    if(keyEvent.getAction() == KeyEvent.ACTION_UP && keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK){
                        pathSelectionCurPath = pathSelectionCurPath.substring(0, pathSelectionCurPath.length()-1);
                        pathSelectionCurPath = pathSelectionCurPath.substring(0, pathSelectionCurPath.lastIndexOf("/")+1);
                        mAdapter.setFiles(FileFoldersLab.get(mContext)
                                .loadFilesFromPath(pathSelectionCurPath));
                        mAdapter.notifyDataSetChanged();
                    }
                    return true;
                }
            });
            recyclerView.setAdapter(mAdapter);
            builder.setView(v);
        }
        AlertDialog create(){
            return builder.create();
        }
    }
}
