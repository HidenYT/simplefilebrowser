package com.tf.simplefilebrowser.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v4.provider.DocumentFile;
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

import com.tf.simplefilebrowser.adapters.ArchiveViewAdapter;
import com.tf.simplefilebrowser.helpers.FileFoldersLab;
import com.tf.simplefilebrowser.R;
import com.tf.simplefilebrowser.helpers.NotificationsLab;
import com.tf.simplefilebrowser.helpers.ZipArchiveHelper;
import com.tf.simplefilebrowser.helpers.ZipFileSelection;
import com.tf.simplefilebrowser.helpers.archives.zip.ZipExtractor;
import com.tf.simplefilebrowser.helpers.archives.zip.ZipProcess;
import com.touchforce.pathselectiondialog.OnPathSelectedListener;
import com.touchforce.pathselectiondialog.PathSelectionDialog;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class ArchiveViewActivity extends Activity {
    private static final String EXTRA_ARCHIVE = "com.tf.simplefilebrowser.archive";
    private static final String EXTRA_ARCHIVE_URI = "com.tf.simplefilebrowser.archiveuri";
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
    public static Intent newIntent(Context packageContext, Uri archiveUri) {
        Intent intent = new Intent(packageContext, ArchiveViewActivity.class);
        intent.putExtra(EXTRA_ARCHIVE_URI, archiveUri.toString());
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        return intent;
    }
    public String CUR_ZIP_VIEW_PATH="";
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive_view);
        mContext = this;
        if(getIntent().getSerializableExtra(EXTRA_ARCHIVE) != null){
            archive = (File) getIntent().getSerializableExtra(EXTRA_ARCHIVE);
        }else if(getIntent().getStringExtra(EXTRA_ARCHIVE_URI) != null){
            Uri uri = Uri.parse(getIntent().getStringExtra(EXTRA_ARCHIVE_URI));
            String a = getRealUri(uri);
            archive = new File(a);
        }

        Log.d(TAG, "onCreate: " + archive.getAbsolutePath());
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
        mToolbar.setTitleTextColor(Color.WHITE);
        updateUI();
    }
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
                if(mActionMode != null)
                    mActionMode.finish();
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
                PathSelectionDialog psd = new PathSelectionDialog(mContext, "Choose extraction path");
                psd.setOnSelectedListener(new OnPathSelectedListener() {
                    @Override
                    public void onPathSelected(String curPath) {
                        startExtraction(curPath);
                    }
                });

                psd.createDialog();
            }
            if(item.getItemId() == R.id.menu_select_all){
                LinkedList<String> all = getNamesCurPath();
                selection.getSelectedFiles().clear();
                for(ZipEntry ze : mEntries){
                    if(all.contains(ze.getName())){
                        if(ze.isDirectory()){
                            for(ZipEntry i : mEntries){
                                if(i.getName().startsWith(ze.getName())
                                && !i.getName().equals(ze.getName())){
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
            ZipFile zipFile = new ZipFile(archive.getPath());
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

    private void startExtraction(final String curPath){
        new Thread(new Runnable() {
            @Override
            public void run() {
                ZipProcess r = new ZipExtractor(archive.getPath(),curPath,
                        selection.getSelectedFiles(), CUR_ZIP_VIEW_PATH);
                for (String e:
                     selection.getSelectedFiles()) {

                }
                NotificationsLab.get(mContext).createZipProgress(
                        Thread.currentThread().getId(), r,
                        "Extracting files",
                        "Extraction in progress");
                r.run();
            }
        }).start();
    }

    private String getRealUri(Uri uri){
        String result;
        Cursor cursor = getContentResolver().query(uri, null,null, null, null);
        if(cursor == null){
            result = uri.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }
}
