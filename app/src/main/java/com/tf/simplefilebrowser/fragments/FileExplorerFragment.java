package com.tf.simplefilebrowser.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

import com.tf.simplefilebrowser.ActionMenus.ActionSelectMenu;
import com.tf.simplefilebrowser.activities.SettingsActivity;
import com.tf.simplefilebrowser.alertdialogs.AlertDialogResultIDs;
import com.tf.simplefilebrowser.alertdialogs.ChooseFileTypeDialogCreator;
import com.tf.simplefilebrowser.alertdialogs.DeleteConfirmationDialogCreator;
import com.tf.simplefilebrowser.alertdialogs.DialogActionListener;
import com.tf.simplefilebrowser.R;
import com.tf.simplefilebrowser.StorageTypes;
import com.tf.simplefilebrowser.activities.InformationActivity;
import com.tf.simplefilebrowser.adapters.FileExplorerAdapter;
import com.tf.simplefilebrowser.ActionMenus.CopyActionMenu;
import com.tf.simplefilebrowser.ActionMenus.MoveActionMenu;
import com.tf.simplefilebrowser.alertdialogs.LongTouchMenuDialogCreator;
import com.tf.simplefilebrowser.helpers.FileFoldersLab;
import com.tf.simplefilebrowser.helpers.NotificationsLab;
import com.tf.simplefilebrowser.helpers.SelectionHelper;
import com.tf.simplefilebrowser.helpers.StorageHelper;
import com.tf.simplefilebrowser.helpers.ThumbnailsHelper;
import com.tf.simplefilebrowser.helpers.archives.ZipCompressor;
import com.tf.simplefilebrowser.helpers.archives.zip.ZipProcess;
import com.tf.simplefilebrowser.viewholders.FileExplorerViewHolder;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class FileExplorerFragment extends Fragment implements AdapterView.OnItemSelectedListener,
        DialogActionListener {
    public RecyclerView mRecyclerView;
    private FileExplorerAdapter mAdapter;
    private TextView mCurrentPathTextView;
    private TextView mNoFilesTextView;
    public boolean mFilesActionActive;
    private ActionMode actionSelectActionMode;
    public Spinner mSpinner;
    private final int REQUEST_FILES_ACCESS = 6;
    public static final int REQUEST_SD_CARD_PATH = 7;
    private final String TAG = "TAG";
    private FileExplorerFragment mFileExplorerFragment;
    private ContentResolver mContentResolver;
    public ContentResolver getContentResolver(){
        return mContentResolver;
    }

    public enum SortModes{
        ByName,
        ByDate,
        BySize
    }
    private SortModes sortMode = SortModes.ByName;
    private boolean descendingSortMode = false;
    private StorageTypes curStorage = StorageTypes.InternalStorage;
    private final String SAVED_INSTANCE_CURRENT_PATH = "CurrentPath";

    public StorageTypes getCurStorage(){
        return curStorage;
    }

    public void setActionSelectActionMode(ActionMode actionSelectActionMode) {
        this.actionSelectActionMode = actionSelectActionMode;
    }
    public ActionMode getActionSelectActionMode() {
        return this.actionSelectActionMode;
    }

    public void setCurStorage(StorageTypes storage){
        this.curStorage = storage;
    }

    ArrayAdapter<CharSequence> mSpinnerAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        if(!hasStoragePermissions()){
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
                ChooseFileTypeDialogCreator dialog = new ChooseFileTypeDialogCreator(getActivity());
                dialog.setActionListener(mFileExplorerFragment);
                dialog.createDialog();
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
        if(hasStoragePermissions()){
            FileFoldersLab.get(getActivity()).prepareEnvironment();
            updateUI(true);
        }
        return view;
    }
    public void setSortMode(SortModes mode){
        this.sortMode = mode;
    }
    public void setDescendingSortMode(boolean descendingSortMode) {
        this.descendingSortMode = descendingSortMode;
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
        if(hasStoragePermissions()){
            updateUI(true);
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(hasStoragePermissions()) {
            FileFoldersLab.get(getActivity()).removeTmpFolder();
        }
    }
    @Override
    public void onDetach() {
        super.onDetach();
        if(hasStoragePermissions()) {
            FileFoldersLab.get(getActivity()).removeTmpFolder();
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(hasStoragePermissions()){
            FileFoldersLab.get(getActivity()).removeTmpFolder();
        }
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
            getActivity().startActionMode(new ActionSelectMenu(this));
        }
        if(item.getItemId() == R.id.about_donate){
            Intent i = new Intent(getActivity(), InformationActivity.class);
            startActivity(i);
        }
        if(item.getItemId() == R.id.settings_button){
            Intent i = new Intent(getActivity(), SettingsActivity.class);
            startActivity(i);
        }
        if(item.getItemId() == R.id.menu_sort_desc){
            if(item.isChecked()){
                descendingSortMode = false;
                item.setChecked(false);
                updateUI(false);
            }else{
                descendingSortMode = true;
                item.setChecked(true);
                updateUI(false);
            }
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != Activity.RESULT_OK){
            return;
        }
        if(requestCode == REQUEST_FILES_ACCESS){
            updateUI(false);
        }
        if(requestCode == REQUEST_SD_CARD_PATH){
            Log.d(TAG, "onActivityResult: ---------------------------------------" );
            if(data != null && takePermission(getActivity(), data.getData())){
                Uri uri = data.getData();
                assert uri != null;
                FileFoldersLab.get(getActivity()).setSDCardUri(uri.toString());
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_FILES_ACCESS){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                updateUI(false);
            }else{
                Toast t = Toast.makeText(getActivity(),
                        R.string.files_access_permission_denied,Toast.LENGTH_LONG);
                t.show();
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(position == 0){
            curStorage = StorageTypes.InternalStorage;
            FileFoldersLab.get(getActivity()).
                    setCurPath(FileFoldersLab.get(getActivity()).getINTERNAL_STORAGE_PATH());
            if(hasStoragePermissions()){
                updateUI(false);
            }
        }else{
            Log.d(TAG, "onItemSelected: " + FileFoldersLab.get(getActivity()).getSDCardUri());
            if(StorageHelper.get(getActivity()).getAllPaths().size() > 1){
                if(FileFoldersLab.get(getActivity()).getSDCardUri().equals("not_found")){
                    FileFoldersLab.getSDCardAccess(mFileExplorerFragment);
                    mSpinner.setSelection(0);
                }else{
                    curStorage = StorageTypes.SDCard;
                    FileFoldersLab.get(getActivity()).
                            setCurPath(FileFoldersLab.get(getActivity()).getSDCardPath());
                    if(hasStoragePermissions()){
                        updateUI(false);
                    }
                }
            }else{
                mSpinner.setSelection(0);
            }
        }
        mRecyclerView.getLayoutManager().scrollToPosition(0);
    }
    @Override
    public void onDialogAction(AlertDialogResultIDs resultID, Bundle result) {
        if(resultID == AlertDialogResultIDs.NEW_FILE_TITLE){
            FileFoldersLab.get(getActivity()).createFile(
                    result.getString(ChooseFileTypeDialogCreator.FILE_NAME_ARG));
        }
        if(resultID == AlertDialogResultIDs.NEW_FOLDER_TITLE){
            FileFoldersLab.get(getActivity()).createFolder(
                    result.getString(ChooseFileTypeDialogCreator.FILE_NAME_ARG));
        }
        if(resultID == AlertDialogResultIDs.DELETE_ONE_FILE){
            String r = result.getString(DeleteConfirmationDialogCreator.DELETE_FILE_PATH_ARG);
            if(r.startsWith(FileFoldersLab.get(getActivity()).getINTERNAL_STORAGE_PATH())){
                FileFoldersLab.removeFile(r);
            }else if(r.startsWith(FileFoldersLab.get(getActivity()).getSDCardPath())){
                FileFoldersLab.removeFileSD(r);
            }
        }
        if(resultID == AlertDialogResultIDs.DELETE_MULTIPLE_FILES){
            LinkedList<String> l = (LinkedList<String>)
                    result.getSerializable(DeleteConfirmationDialogCreator.DELETE_MULTIPLE_ARG);
            for (String s: l) {
                if(s.startsWith(FileFoldersLab.get(getActivity()).getINTERNAL_STORAGE_PATH())){
                    FileFoldersLab.removeFile(s);
                }else if(s.startsWith(FileFoldersLab.get(getActivity()).getSDCardPath())){
                    FileFoldersLab.removeFileSD(s);
                }
            }
            if(actionSelectActionMode != null){
                actionSelectActionMode.finish();
            }
        }
        if(resultID == AlertDialogResultIDs.COPY_FILE){
            getActivity().startActionMode(new CopyActionMenu(this,
                    result.getString(LongTouchMenuDialogCreator.PRESSED_FILE_PATH_ARG)));
        }
        if(resultID == AlertDialogResultIDs.MOVE_FILE){
            getActivity().startActionMode(new MoveActionMenu(this,
                    result.getString(LongTouchMenuDialogCreator.PRESSED_FILE_PATH_ARG)));
        }
        if(resultID == AlertDialogResultIDs.COMPRESS_FILE){
            LinkedList<String > l = new LinkedList<>();
            l.add(result.getString(LongTouchMenuDialogCreator.PRESSED_FILE_PATH_ARG));
            compressSelectedFiles(l, null);
        }
        if(resultID == AlertDialogResultIDs.RENAME_FILE){
            File f = new File(result.getString(LongTouchMenuDialogCreator.PRESSED_FILE_PATH_ARG));
            if(f.getAbsolutePath().startsWith(FileFoldersLab.get(getActivity()).getINTERNAL_STORAGE_PATH())){
                f.renameTo(new File(f.getParent(), result.getString(LongTouchMenuDialogCreator.FILE_NEW_NAME_ARG)));
            }else if (f.getAbsolutePath().startsWith(FileFoldersLab.get(getActivity()).getSDCardPath())){
                DocumentFile doc = StorageHelper.get(getActivity()).getDocumentFile(f);
                doc.renameTo(result.getString(LongTouchMenuDialogCreator.FILE_NEW_NAME_ARG));
            }
        }
        updateUI(false);
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    public void updateUI(final boolean refresh, final String... lastOpenedPath) {
        Log.d(TAG, "updateUI: started");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<File> files = new LinkedList<>();
                    if(sortMode == SortModes.ByName){
                        files = FileFoldersLab.get(getActivity()).loadFilesFromPath();
                    }else if(sortMode == SortModes.ByDate){
                        List<File> f = FileFoldersLab.loadFilesNoSort(FileFoldersLab.get(getActivity()).getCurPath());
                        files = FileFoldersLab.sortFilesByDate(f);
                    }
                    if(descendingSortMode){
                        Collections.reverse(files);
                    }
                    final String curPath = FileFoldersLab.get(getActivity()).getCurPath();

                    final List<File> finalFiles = files;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(finalFiles.size() == 0){
                                mNoFilesTextView.setVisibility(View.VISIBLE);
                            }else{
                                mNoFilesTextView.setVisibility(View.GONE);
                            }
                            mCurrentPathTextView.setText(curPath);
                            if(refresh){
                                mAdapter = new FileExplorerAdapter(mFileExplorerFragment, finalFiles);
                                mRecyclerView.setAdapter(mAdapter);
                            } else {
                                mAdapter.setFiles(finalFiles);
                                mAdapter.notifyDataSetChanged();
                            }

                            if(FileFoldersLab.get(getActivity()).getCurPath().startsWith(
                                    FileFoldersLab.get(getActivity()).getSDCardPath())){
                                mSpinner.setSelection(1);
                                curStorage = StorageTypes.SDCard;
                            }
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                    if(lastOpenedPath.length>0){
                        FileFoldersLab.get(getActivity()).setCurPath(lastOpenedPath[0]);
                        updateUI(true);
                    }
                }
            }
        }).start();
    }

    public void updateUInoDataChanged(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    public static boolean takePermission(Context context,Uri uri){
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

    public void compressSelectedFiles(final LinkedList<String> selectedFiles, final ActionSelectMenu menu){
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
                            }else{
                                format = ".zip";
                            }
                            ZipProcess r = new ZipCompressor(
                                    getActivity(), selectedFiles, curPath + File.separator +
                                    v.getText() + format, "",
                                    null, curPath, mContentResolver, null);
                            NotificationsLab.get(getActivity()).createZipProgress(
                                    Thread.currentThread().getId(), r,
                                    getString(R.string.compression_files),
                                    getString(R.string.compression_in_progress));
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
                                if(actionSelectActionMode != null && menu != null){
                                    menu.setDeleteSelection(false);
                                    actionSelectActionMode.finish();
                                }
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
                if(actionSelectActionMode != null && menu != null){
                    menu.setDeleteSelection(false);
                    actionSelectActionMode.finish();
                }
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

    public void deleteSelectedFiles(){
        LinkedList<String> arr = SelectionHelper.get(getActivity()).getSelectedFiles();
        DeleteConfirmationDialogCreator dialogCreator =
                new DeleteConfirmationDialogCreator(getActivity(), arr);
        dialogCreator.setActionListener(this);
        dialogCreator.createDialog();
    }


    public void onBackButtonPressed() {
        String s = FileFoldersLab.get(getActivity()).getCurPath();
        try {
            if(!mFilesActionActive){
                SelectionHelper.get(getActivity()).getSelectedFiles().clear();
                if(actionSelectActionMode != null){
                    actionSelectActionMode.finish();
                }
            }
            updateUI(true);
            FileFoldersLab.get(getActivity()).
                    setCurPath(FileFoldersLab.
                            get(getActivity()).prevPath());
            Log.d(TAG, "onBackButtonPressed: " + FileFoldersLab.get(getActivity()).getCurPath());
            updateUI(true, s);
            mRecyclerView.getLayoutManager().scrollToPosition(0);
        }catch (Exception e){
            Log.d(TAG, "onBackButtonPressed: " + "HERRRRRRRRRRRRRRRRRRREEEEEEEEEEEEE");
            e.printStackTrace();
            FileFoldersLab.get(getActivity()).setCurPath(s);
            updateUI(true);
        }
    }

    private boolean hasStoragePermissions(){
        return ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }
}
