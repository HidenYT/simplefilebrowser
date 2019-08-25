package com.tf.simplefilebrowser.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.provider.DocumentFile;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.tf.simplefilebrowser.R;
import com.tf.simplefilebrowser.activities.InformationActivity;
import com.tf.simplefilebrowser.adapters.FileExplorerAdapter;
import com.tf.simplefilebrowser.ActionMenus.CopyActionMenu;
import com.tf.simplefilebrowser.ActionMenus.CopyMultipleActionMenu;
import com.tf.simplefilebrowser.ActionMenus.MoveActionMenu;
import com.tf.simplefilebrowser.ActionMenus.MoveMultipleActionMenu;
import com.tf.simplefilebrowser.helpers.AlertDialogHelper;
import com.tf.simplefilebrowser.helpers.FileFoldersLab;
import com.tf.simplefilebrowser.helpers.NotificationsLab;
import com.tf.simplefilebrowser.helpers.SelectionHelper;
import com.tf.simplefilebrowser.helpers.StorageHelper;
import com.tf.simplefilebrowser.helpers.archives.ZipCompressor;
import com.tf.simplefilebrowser.helpers.archives.zip.ZipExtractor;
import com.tf.simplefilebrowser.helpers.archives.zip.ZipProcess;
import com.tf.simplefilebrowser.viewholders.FileExplorerViewHolder;

import java.io.File;
import java.util.LinkedList;
import java.util.List;


public class FileExplorerFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    public RecyclerView mRecyclerView;
    private FileExplorerAdapter mAdapter;
    private TextView mCurrentPathTextView;
    private TextView mNoFilesTextView;
    public boolean mFilesActionActive;
    public ActionMode mainActionMode;
    public Spinner mSpinner;
    private static final int REQUEST_NEW_FILE_TYPE = 0;
    private static final String DIALOG_NEW_FILE_TYPE = "NewFileType";
    public static final String DIALOG_FILE_ACTION = "FileAction";
    private final int REQUEST_FILES_ACCESS = 6;
    private final int REQUEST_SD_CARD_PATH = 7;
    private final String TAG = "TAG";
    private FileExplorerFragment mFileExplorerFragment;
    public ContentResolver mContentResolver;


    public enum FileActions{
        COPY,
        MOVE,
        COMPRESS
    }
    public enum SortModes{
        ByName,
        ByDate,
        BySize
    }
    public SortModes sortMode = SortModes.ByName;
    private final int INTERNAL_STORAGE = 0;
    private final int SD_CARD = 1;
    public int curStorage = 0;
    private final String SAVED_INSTANCE_CURRENT_PATH = "CurrentPath";
    private boolean layoutIsDragging = false;

    private Thread thumbnailThread;

    ArrayAdapter<CharSequence> mSpinnerAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        if(ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(),
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_FILES_ACCESS);
        }
        mSpinnerAdapter = ArrayAdapter.createFromResource(
                getActivity(),
                R.array.spinner_options,
                R.layout.spinner_item);
        mFileExplorerFragment = this;
        mContentResolver = getActivity().getContentResolver();
        FileFoldersLab.get(getActivity()).prepareEnvironment();
        mLayoutManager = new LinearLayoutManager(getActivity());
        final View view = inflater.inflate(R.layout.file_explorer_fragment, container,false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.file_explorer_recycler_view);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mCurrentPathTextView = (TextView) view.findViewById(R.id.cur_path_text_view);
        mNoFilesTextView = (TextView) view.findViewById(R.id.no_files_text_view);
        FloatingActionButton mFloatingActionButton = (FloatingActionButton) view.findViewById(R.id.floating_action_button);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                AlertDialogHelper fragment = AlertDialogHelper.ChooseFileType.newInstance();
                fragment.setTargetFragment(FileExplorerFragment.this, REQUEST_NEW_FILE_TYPE);
                fragment.show(manager, DIALOG_NEW_FILE_TYPE);
            }
        });
        mSpinner = view.findViewById(R.id.path_selection_spinner);

        mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mSpinner.setOnItemSelectedListener(this);
        mSpinner.setAdapter(mSpinnerAdapter);
        Toolbar mToolbar = view.findViewById(R.id.toolbar);
        getActivity().setActionBar(mToolbar);
        getActivity().getActionBar().setDisplayShowTitleEnabled(false);
        setHasOptionsMenu(true);
        //mRecyclerView.setItemViewCacheSize(20);
        //mRecyclerView.setDrawingCacheEnabled(true);
        //mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, final int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Log.d(TAG, "onScrollStateChanged: ");
                stopThumbnailLoad();
                thumbnailThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(newState == RecyclerView.SCROLL_STATE_IDLE){
                            layoutIsDragging = false;

                            createThumbnails();

                            /*for(int i = 0; i < viewHolders.size(); i++){
                                viewHolders.get(i).createThumbnail();
                                if(thumbnailThread.isInterrupted() || layoutIsDragging){
                                    break;
                                }
                            }*/
                        }
                        if(newState == RecyclerView.SCROLL_STATE_DRAGGING){
                            layoutIsDragging = true;
                        }
                    }
                });
                thumbnailThread.start();

            }
        });

        /*mRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(!layoutIsDragging){
                            for(int i = 0; i < viewHolders.size(); i++){
                                viewHolders.get(i).createThumbnail();
                            }
                        }
                    }
                }).start();
            }
        });*/
        mRecyclerView.setHasFixedSize(true);
        updateUI(true);
        return view;
    }

    public void stopThumbnailLoad(){
        if(thumbnailThread != null){
            thumbnailThread.interrupt();
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null && savedInstanceState.getString(SAVED_INSTANCE_CURRENT_PATH) != null){
            FileFoldersLab.get(getActivity())
                    .setCurPath(savedInstanceState.
                            getString(SAVED_INSTANCE_CURRENT_PATH));
        }
        setRetainInstance(true);
    }
    @Override
    public void onResume(){
        super.onResume();
        updateUI(true);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        FileFoldersLab.get(getActivity()).removeTmpFolder();
    }
    @Override
    public void onDetach() {
        super.onDetach();
        FileFoldersLab.get(getActivity()).removeTmpFolder();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FileFoldersLab.get(getActivity()).removeTmpFolder();
    }
    @Override
    public void onSaveInstanceState(Bundle save){
        save.putString(SAVED_INSTANCE_CURRENT_PATH, FileFoldersLab.get(getActivity()).getCurPath());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.three_dots_menu_file_explorer_fragment, menu);
    }

    public void onBackButtonPressed() {
        layoutIsDragging = false;
        try {
            FileFoldersLab.get(getActivity()).
                    setCurPath(FileFoldersLab.
                            get(getActivity()).prevPath());
            if(!mFilesActionActive){
                SelectionHelper.get(getActivity()).getSelectedFiles().clear();
                if(mainActionMode != null){
                    mainActionMode.finish();
                }
            }
            stopThumbnailLoad();
            updateUI(true);
            mRecyclerView.getLayoutManager().scrollToPosition(0);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(!item.isChecked()){
            if(item.getItemId() == R.id.menu_sort_name){
                sortMode = SortModes.ByName;
                item.setChecked(true);
                updateUI(false);
            }
            if(item.getItemId() == R.id.menu_sort_date){
                sortMode = SortModes.ByDate;
                item.setChecked(true);
                updateUI(false);
            }
        }
        if(item.getItemId() == R.id.menu_select_all){
            LinkedList<File> f = FileFoldersLab.loadFilesNoSort(FileFoldersLab.get(getActivity()).getCurPath());
            SelectionHelper.get(getActivity()).getSelectedFiles().clear();
            for(File i : f){
                SelectionHelper.get(getActivity()).addSelectedFile(i.getAbsolutePath());
            }
            mAdapter.notifyDataSetChanged();
            getActivity().startActionMode(new multipleFilesActionMenu());
        }
        if(item.getItemId() == R.id.about_donate){
            Intent i = new Intent(getActivity(), InformationActivity.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateUI(final boolean refresh) {
        Log.d(TAG, "updateUI: started");
        stopThumbnailLoad();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try{
                    List files = new LinkedList();
                    if(sortMode == SortModes.ByName){
                        files = FileFoldersLab.get(getActivity()).loadFilesFromPath();
                    }else if(sortMode == SortModes.ByDate){
                        List<File> f = FileFoldersLab.loadFilesNoSort(FileFoldersLab.get(getActivity()).getCurPath());
                        files = FileFoldersLab.sortFilesByDate(f);
                    }
                    if(files.size() == 0){
                        mNoFilesTextView.setVisibility(View.VISIBLE);
                    }else{
                        mNoFilesTextView.setVisibility(View.GONE);
                    }
                    String curPath = FileFoldersLab.get(getActivity()).getCurPath();
                    mCurrentPathTextView.setText(curPath);
                    if(refresh){
                        mAdapter = new FileExplorerAdapter(mFileExplorerFragment, files);
                        mRecyclerView.setAdapter(mAdapter);
                    } else {
                        mAdapter.setFiles(files);
                        mAdapter.notifyDataSetChanged();
                    }

                    if(FileFoldersLab.get(getActivity()).getCurPath().startsWith(
                            FileFoldersLab.get(getActivity()).getSDCardPath())){
                        mSpinner.setSelection(1);
                        curStorage = 1;
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
                Log.d(TAG, "updateUI: preEnded");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        thumbnailThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                createThumbnails();
                            }
                        });
                        thumbnailThread.start();
                    }
                }, 50);
            }
        });
        Log.d(TAG, "updateUI: ended");
    }
    public void updateUInoDataChanged(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                thumbnailThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        createThumbnails();
                    }
                });
                thumbnailThread.start();
            }
        }, 50);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != Activity.RESULT_OK){
            return;
        }
        if(requestCode == AlertDialogHelper.REQUEST_FILE_NAME){
            FileFoldersLab.get(getActivity()).createFile(data.
                    getStringExtra(AlertDialogHelper.PromptTextInput.EXTRA_TITLE));
            updateUI(false);
        }
        if(requestCode == AlertDialogHelper.REQUEST_FOLDER_NAME){
            FileFoldersLab.get(getActivity()).
                    createFolder(data.
                            getStringExtra(AlertDialogHelper.PromptTextInput.EXTRA_TITLE));
            updateUI(false);
        }
        if(requestCode == AlertDialogHelper.REQUEST_FILE_ACTION){
            if(data.getSerializableExtra(AlertDialogHelper.LongTouchMenu.RESULT_ACTION) == FileActions.COPY){
                getActivity().startActionMode(new CopyActionMenu(this,
                        data.getStringExtra(AlertDialogHelper.LongTouchMenu.ARG_FILE_NAME)));
            }
            if(data.getSerializableExtra(AlertDialogHelper.LongTouchMenu.RESULT_ACTION) == FileActions.MOVE){
                getActivity().startActionMode(new MoveActionMenu(this,
                        data.getStringExtra(AlertDialogHelper.LongTouchMenu.ARG_FILE_NAME)));
            }
            if(data.getSerializableExtra(AlertDialogHelper.LongTouchMenu.RESULT_ACTION) == FileActions.COMPRESS){
                LinkedList<String> l = new LinkedList<>();
                l.add(data.getStringExtra(AlertDialogHelper.LongTouchMenu.ARG_FILE_NAME));
                compressSelectedFiles(l, null);
            }
        }
        if(requestCode == AlertDialogHelper.REQUEST_RENAME){
            File file = new File(data.getStringExtra(AlertDialogHelper.PromptTextInput.EXTRA_PATH));
            if(curStorage == 0){
                file.renameTo(new File(FileFoldersLab.get(getActivity()).getCurPath(),
                        data.getStringExtra(AlertDialogHelper.PromptTextInput.EXTRA_TITLE)));
            }else{
                DocumentFile doc = StorageHelper.get(getActivity()).getDocumentFile(file);
                doc.renameTo(data.getStringExtra(AlertDialogHelper.PromptTextInput.EXTRA_TITLE));
            }
            updateUI(false);
        }
        if(requestCode == REQUEST_FILES_ACCESS){
            updateUI(false);
        }
        if(requestCode == REQUEST_SD_CARD_PATH){
            if(data != null && takePermission(getActivity(), data.getData())){
                Uri uri = data.getData();
                FileFoldersLab.get(getActivity()).setSDCardUri(uri.toString());
            }
        }
    }

//    private void compressSelectedFiles(final LinkedList<String> selectedFiles){
//        final String curPath = FileFoldersLab.get(getActivity()).getCurPath();
//        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setTitle(getString(R.string.archive_name));
//        final EditText v = new EditText(getActivity());
//        v.setInputType(InputType.TYPE_CLASS_TEXT);
//        builder.setView(v);
//        builder.setPositiveButton(R.string.DIALOG_OK, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if(!new File(FileFoldersLab.get(getActivity()).
//                                getCurPath() + File.separator + v.getText()).exists() &&
//                                !new File(FileFoldersLab.get(getActivity()).
//                                        getCurPath() + File.separator + v.getText() + ".zip").exists()){
//                            String format;
//                            if(v.getText().toString().matches("^.*\\.zip$")){
//                                format = "";
//                                /*ZipArchiveHelper.get(getActivity(),mContentResolver).createZipFromFiles(
//                                        selectedFiles,
//                                        curPath + File.separator + v.getText(),
//                                        "",null, curPath);*/
//                            }else{
//                                format = ".zip";
//                                /*ZipArchiveHelper.get(getActivity(),mContentResolver).createZipFromFiles(
//                                        selectedFiles,
//                                        curPath + File.separator + v.getText() + ".zip",
//                                        "",null, curPath);*/
//                            }
//                            Runnable r = new ZipCompressor(
//                                    getActivity(), selectedFiles, curPath + File.separator +
//                                    v.getText() + format, "",
//                                    null, curPath, mContentResolver);
//                            r.run();
//                        }else{
//                            getActivity().runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Toast t = Toast.makeText(getActivity(),
//                                            getString(R.string.archive_exists), Toast.LENGTH_SHORT);
//                                    t.show();
//                                }
//                            });
//                        }
//                        getActivity().runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                updateUI();
//                                if(mainActionMode != null)
//                                    mainActionMode.finish();
//                            }
//                        });
//
//                    }
//                }).start();
//            }
//        });
//        builder.setNegativeButton(R.string.DIALOG_CANCEL, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                SelectionHelper.get(getActivity()).getSelectedFiles().clear();
//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        updateUI();
//                        if(mainActionMode != null)
//                            mainActionMode.finish();
//                    }
//                });
//            }
//        });
//        getActivity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                builder.create();
//                builder.show();
//            }
//        });
//    }
    private void compressSelectedFiles(final LinkedList<String> selectedFiles, final multipleFilesActionMenu menu){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateUI(false);
                if(mainActionMode != null && menu != null){
                    menu.mDeleteSelection = false;
                    mainActionMode.finish();
                }
            }
        });
        final String curPath = FileFoldersLab.get(getActivity()).getCurPath();
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.archive_name));
        final EditText v = new EditText(getActivity());
        v.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(v);
        builder.setPositiveButton(R.string.DIALOG_OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(!new File(FileFoldersLab.get(getActivity()).
                                getCurPath() + File.separator + v.getText()).exists() &&
                                !new File(FileFoldersLab.get(getActivity()).
                                        getCurPath() + File.separator + v.getText() + ".zip").exists()){
                            String format;
                            if(v.getText().toString().matches("^.*\\.zip$")){
                                format = "";
                                /*ZipArchiveHelper.get(getActivity(),mContentResolver).createZipFromFiles(
                                        selectedFiles,
                                        curPath + File.separator + v.getText(),
                                        "",null, curPath);*/
                            }else{
                                format = ".zip";
                                /*ZipArchiveHelper.get(getActivity(),mContentResolver).createZipFromFiles(
                                        selectedFiles,
                                        curPath + File.separator + v.getText() + ".zip",
                                        "",null, curPath);*/
                            }
                            ZipProcess r = new ZipCompressor(
                                    getActivity(), selectedFiles, curPath + File.separator +
                                    v.getText() + format, "",
                                    null, curPath, mContentResolver, null);
                            NotificationsLab.get(getActivity()).createZipProgress(
                                    Thread.currentThread().getId(), r,
                                    "Compressing files",
                                    "Compression in progress");
                            r.run();
                        }else{
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast t = Toast.makeText(getActivity(),
                                            getString(R.string.archive_exists), Toast.LENGTH_SHORT);
                                    t.show();
                                }
                            });
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                SelectionHelper.get(getActivity()).getSelectedFiles().clear();
                                updateUI(false);
                            }
                        });
                    }
                }).start();
            }
        });
        builder.setNegativeButton(R.string.DIALOG_CANCEL, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SelectionHelper.get(getActivity()).getSelectedFiles().clear();
                updateUI(false);
            }
        });
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                builder.create();
                builder.show();
            }
        });
    }

    private void deleteSelectedFiles(){
        LinkedList<String> arr = SelectionHelper.get(getActivity()).getSelectedFiles();
        AlertDialogHelper.confirmFilesDelete.createForMultiple(arr, getActivity(), mFileExplorerFragment).show();
    }

    public class multipleFilesActionMenu implements ActionMode.Callback {
        Activity mActivity;
        MenuItem mMenuItem = null;
        boolean mDeleteSelection = true;
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mActivity = getActivity();
            mainActionMode = mode;
            mMenuItem = null;
            mActivity.getMenuInflater().inflate(R.menu.multiple_files_action_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if(item.getItemId() == R.id.copy_button_action_menu){
                mMenuItem = item;
                mActivity.startActionMode(new CopyMultipleActionMenu(mFileExplorerFragment));
            }
            if(item.getItemId() == R.id.move_button_action_menu) {
                mMenuItem = item;
                mActivity.startActionMode(new MoveMultipleActionMenu(mFileExplorerFragment));
            }
            if(item.getItemId() == R.id.compress_button_action_menu){
                compressSelectedFiles(SelectionHelper.get(getActivity()).getSelectedFiles(), this);
            }
            if(item.getItemId() == R.id.delete_button_action_menu){
                deleteSelectedFiles();
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mainActionMode = null;
            if(mMenuItem == null && mDeleteSelection)
                SelectionHelper.get(getActivity()).getSelectedFiles().clear();
            mMenuItem = null;
            updateUI(false);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemSelected: " + position);
        if(position != curStorage){
            if(position == 0){
                curStorage = 0;
                FileFoldersLab.get(getActivity()).
                        setCurPath(FileFoldersLab.get(getActivity()).getINTERNAL_STORAGE_PATH());
                updateUI(false);
            }else{
                if(FileFoldersLab.get(getActivity()).getSDCardUri().equals("not_found")){
                    getSDCardPath();
                    mSpinner.setSelection(0);
                }else{
                    curStorage = 1;
                    FileFoldersLab.get(getActivity()).
                            setCurPath(FileFoldersLab.get(getActivity()).getSDCardPath());
                    updateUI(false);
                }
            }
            mRecyclerView.getLayoutManager().scrollToPosition(0);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    private void getSDCardPath(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        startActivityForResult(intent, REQUEST_SD_CARD_PATH);
    }

    private boolean takePermission(Context context,Uri uri){
        try {
            if(uri == null)
                return false;
            context.getContentResolver().takePersistableUriPermission(uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    private void createThumbnails(){
        final int firstVisible = ((LinearLayoutManager) mLayoutManager).findFirstVisibleItemPosition();
        final int lastVisible = ((LinearLayoutManager) mLayoutManager).findLastVisibleItemPosition();
        for (int i = firstVisible; i <= lastVisible; i++) {
            FileExplorerViewHolder vh = (FileExplorerViewHolder)
                    mRecyclerView.findViewHolderForAdapterPosition(i);
            if(vh != null)
                vh.createThumbnail();
            if(thumbnailThread.isInterrupted() || layoutIsDragging){
                break;
            }
        }
    }
}
