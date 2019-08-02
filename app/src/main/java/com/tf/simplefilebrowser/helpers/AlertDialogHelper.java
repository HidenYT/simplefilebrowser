package com.tf.simplefilebrowser.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.text.InputType;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.LinkedList;

import com.tf.simplefilebrowser.fragments.FileExplorerFragment;
import com.tf.simplefilebrowser.fragments.FileExplorerFragment.FileActions;
import com.tf.simplefilebrowser.R;

public class AlertDialogHelper extends DialogFragment {
    protected Activity mContext;
    public static final int REQUEST_FILE_NAME = 0;
    public static final int REQUEST_FOLDER_NAME = 1;
    public static final int  REQUEST_CONFIRM_DELETE = 2;
    public static final int REQUEST_FILE_ACTION = 3;
    public static final int REQUEST_RENAME = 4;
    public static final int REQUEST_INFO = 5;

    public static class ChooseFileType extends AlertDialogHelper {

        public static final String DIALOG_FILE_NAME = "FileName";
        public static final String DIALOG_FOLDER_NAME = "FolderName";
        private Context mContext;
        public static ChooseFileType newInstance(){
            return new ChooseFileType();
        }
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            mContext = getTargetFragment().getActivity();
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            View v = LayoutInflater.from(mContext).inflate(R.layout.new_file_type_choose,null);
            ConstraintLayout mFileType = (ConstraintLayout) v.findViewById(R.id.file_type_button);
            mFileType.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager manager = getTargetFragment().getFragmentManager();
                    AlertDialogHelper fragment = PromptTextInput.newInstance("Input file name:", REQUEST_FILE_NAME);
                    fragment.setTargetFragment(getTargetFragment(), REQUEST_FILE_NAME);
                    fragment.show(manager, DIALOG_FILE_NAME);
                    getDialog().dismiss();
                }
            });
            ConstraintLayout mFolderType = (ConstraintLayout) v.findViewById(R.id.folder_type_button);
            mFolderType.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager manager = getTargetFragment().getFragmentManager();
                    AlertDialogHelper fragment = PromptTextInput.newInstance("Input folder name:", REQUEST_FOLDER_NAME);
                    fragment.setTargetFragment(getTargetFragment(), REQUEST_FOLDER_NAME);
                    fragment.show(manager, DIALOG_FOLDER_NAME);
                    getDialog().dismiss();
                }
            });
            builder.setView(v);
            return builder.create();
        }
    }
    public static class LongTouchMenu extends AlertDialogHelper {
        public final static String ARG_FILE_NAME = "FileName";
        public static final String  DIALOG_CONFIRM_DELETE = "confirmdelete";
        public static final String  DIALOG_RENAME = "renamefile";
        public static final String  DIALOG_INFO = "infofile";
        public static final String  DIALOG_COMPRESS = "compressfile";

        public static String RESULT_ACTION = "ResultAction";
        public static LongTouchMenu newInstance(String filename){
            Bundle args = new Bundle();
            LongTouchMenu fragment = new LongTouchMenu();
            args.putString(ARG_FILE_NAME, filename);
            fragment.setArguments(args);
            return fragment;
        }
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            mContext = getTargetFragment().getActivity();
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            View v = LayoutInflater.from(mContext).inflate(R.layout.long_touch_menu_items, null);
            builder.setView(v);
            ConstraintLayout deleteButton = v.findViewById(R.id.delete_button_menu);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getDialog().dismiss();
                    mContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            confirmFilesDelete.createForOne(getArguments().getString(ARG_FILE_NAME), mContext, (FileExplorerFragment) getTargetFragment()).show();
                        }
                    });
                }
            });
            ConstraintLayout copyButton = v.findViewById(R.id.copy_button_menu);
            copyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.putExtra(ARG_FILE_NAME,getArguments().getString(ARG_FILE_NAME));
                    intent.putExtra(RESULT_ACTION, FileActions.COPY);
                    getTargetFragment().onActivityResult(getTargetRequestCode(),
                            Activity.RESULT_OK,
                            intent);
                    getDialog().dismiss();
                }
            });
            ConstraintLayout moveButton = v.findViewById(R.id.move_button_menu);
            moveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.putExtra(ARG_FILE_NAME,getArguments().getString(ARG_FILE_NAME));
                    intent.putExtra(RESULT_ACTION, FileActions.MOVE);
                    getTargetFragment().onActivityResult(getTargetRequestCode(),
                            Activity.RESULT_OK,
                            intent);
                    getDialog().dismiss();
                }
            });
            ConstraintLayout renameButton = v.findViewById(R.id.rename_button_menu);
            renameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager manager = getTargetFragment().getFragmentManager();
                    AlertDialogHelper fragment = PromptTextInput.newInstance("Rename file",
                            REQUEST_RENAME, getArguments().getString(ARG_FILE_NAME));
                    fragment.setTargetFragment(getTargetFragment(), REQUEST_RENAME);
                    fragment.show(manager, DIALOG_RENAME);
                    getDialog().dismiss();
                }
            });
            ConstraintLayout infoButton = v.findViewById(R.id.info_button_menu);
            infoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager manager = getTargetFragment().getFragmentManager();
                    AlertDialogHelper fragment = InformationDialogFragment.newInstance(getArguments().getString(ARG_FILE_NAME));
                    fragment.setTargetFragment(getTargetFragment(), REQUEST_INFO);
                    fragment.show(manager, DIALOG_INFO);
                    getDialog().dismiss();
                }
            });
            ConstraintLayout compress = v.findViewById(R.id.compress_button_menu);
            compress.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.putExtra(ARG_FILE_NAME, getArguments().getString(ARG_FILE_NAME));
                    intent.putExtra(RESULT_ACTION, FileActions.COMPRESS);
                    getTargetFragment().onActivityResult(getTargetRequestCode(),
                            Activity.RESULT_OK,
                            intent);
                    getDialog().dismiss();
                }
            });
            return builder.create();
        }

    }

    public static class confirmFilesDelete{
        public static AlertDialog createForMultiple(LinkedList files, final Activity activity, final FileExplorerFragment fragment){
            final LinkedList<String> mFiles = files;
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Deleting files");
            builder.setMessage("Are you sure you want to delete " + mFiles.size() + " files?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    for(String i : mFiles){
                        if(FileFoldersLab.get(activity).getCurPath().startsWith(
                                FileFoldersLab.get(activity).getSDCardPath())){
                            FileFoldersLab.removeFileSD(i);
                        }else{
                            FileFoldersLab.removeFile(i);
                        }
                    }

                    SelectionHelper.get(activity).getSelectedFiles().clear();
                    fragment.updateUI();
                    if(fragment.mActionMode != null){
                        fragment.mActionMode.finish();
                        fragment.mActionMode = null;
                    }
                }
            });
            builder.setNegativeButton("No", null);
            return builder.create();
        }
        public static AlertDialog createForOne(final String filePath, final Activity activity, final FileExplorerFragment fragment){
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Deleting file");
            builder.setMessage("Are you sure you want to delete " + filePath + "?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(FileFoldersLab.get(activity).getCurPath()
                            .startsWith(FileFoldersLab.get(activity).getSDCardPath())){
                        FileFoldersLab.removeFileSD(filePath);
                    }else{
                        FileFoldersLab.removeFile(filePath);
                    }
                    SelectionHelper.get(activity).getSelectedFiles().clear();
                    fragment.updateUI();
                    if(fragment.mActionMode != null){
                        fragment.mActionMode.finish();
                        fragment.mActionMode = null;
                    }
                }
            });
            builder.setNegativeButton("No", null);
            return builder.create();
        }
    }

    public static class PromptTextInput extends AlertDialogHelper {
        public final static String ARG_TITLE = "stringkey";
        public final static String ARG_REQUEST_CODE = "requestkodekey";
        public final static String ARG_FILE_PATH = "filepathkey";
        public final static String EXTRA_TITLE = "com.tf.simplefilebrowser.extra_title";
        public final static String EXTRA_PATH = "com.tf.simplefilebrowser.file_path_new_file_name";
        private int requestCode;
        private String filePath = null;
        public static PromptTextInput newInstance(String title, int requestCode){
            Bundle args = new Bundle();
            args.putString(ARG_TITLE, title);
            args.putInt(ARG_REQUEST_CODE, requestCode);
            PromptTextInput fragment = new PromptTextInput();
            fragment.setArguments(args);
            return fragment;
        }
        public static PromptTextInput newInstance(String title, int requestCode, String filePath){
            Bundle args = new Bundle();
            args.putString(ARG_TITLE, title);
            args.putInt(ARG_REQUEST_CODE, requestCode);
            args.putString(ARG_FILE_PATH, filePath);
            PromptTextInput fragment = new PromptTextInput();
            fragment.setArguments(args);
            return fragment;
        }
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            mContext = getTargetFragment().getActivity();
            requestCode = getArguments().getInt(ARG_REQUEST_CODE);
            if(getArguments().getString(ARG_FILE_PATH) != null)
                filePath = getArguments().getString(ARG_FILE_PATH);
            String title = getArguments().getString(ARG_TITLE);
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(title);
            final EditText input = new EditText(mContext);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            if(getArguments().getString(ARG_FILE_PATH) != null){
                input.setText(new File(getArguments().getString(ARG_FILE_PATH)).getName());
            }
            builder.setView(input);
            builder.setPositiveButton(R.string.DIALOG_OK, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(filePath != null)
                        sendResult(Activity.RESULT_OK, input.getText().toString(), filePath);
                    else
                        sendResult(Activity.RESULT_OK, input.getText().toString());
                }
            });
            builder.setNegativeButton(R.string.DIALOG_CANCEL, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    sendResult(Activity.RESULT_CANCELED, "");
                }
            });
            return builder.create();
        }
        private void sendResult(int resultCode, String title){
            if (getTargetFragment() == null) {
                return;
            }
            Intent intent = new Intent();
            intent.putExtra(EXTRA_TITLE, title);
            Log.d("TAG", getTargetFragment().toString());
            getTargetFragment().onActivityResult(requestCode, resultCode, intent);
        }
        //For rename
        private void sendResult(int resultCode, String title, String path){
            if (getTargetFragment() == null) {
                return;
            }
            Intent intent = new Intent();
            intent.putExtra(EXTRA_TITLE, title);
            intent.putExtra(EXTRA_PATH, path);
            Log.d("TAG", getTargetFragment().toString());
            getTargetFragment().onActivityResult(requestCode, resultCode, intent);
        }
    }
    public static class InformationDialogFragment extends AlertDialogHelper {
        private static final String ARGS_FILE_PATH = "FilePath";
        public static InformationDialogFragment newInstance(String filePath){
            Bundle args = new Bundle();
            InformationDialogFragment fragment = new InformationDialogFragment();
            args.putString(ARGS_FILE_PATH, filePath);
            fragment.setArguments(args);
            return fragment;
        }
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            mContext = getTargetFragment().getActivity();
            File file = new File(getArguments().getString(ARGS_FILE_PATH));
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            View v = LayoutInflater.from(mContext).inflate(R.layout.file_information_fragment,null);
            TextView fileNameView = v.findViewById(R.id.information_file_name);
            TextView filePath = v.findViewById(R.id.information_file_path);
            fileNameView.setText(file.getName());
            TextView fileSizeView = v.findViewById(R.id.information_file_size);
            long fileSize;
            if(file.isDirectory()){
                fileSize = FileUtils.sizeOfDirectory(file);
            }else{
                fileSize = file.length();
            }
            fileSizeView.setText(String.valueOf(fileSize/1024/1024) + " mb (" + fileSize + "B)");
            filePath.setText(file.getPath());
            TextView fileCheckSum = v.findViewById(R.id.information_file_md5_checksum);
            /*TextView fileCheckSum = v.findViewById(R.id.information_file_md5_checksum);
            try {
                fileCheckSum.setText(Long.toString(FileUtils.checksumCRC32(file)));
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            try{
                FileInputStream fis = new FileInputStream(file);
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] buffer = new byte[8192];
                int numOfBytesRead;
                while((numOfBytesRead = fis.read(buffer)) > 0){
                    md.update(buffer, 0, numOfBytesRead);
                }
                byte[] hash = md.digest();
                String result = "";
                for(int i = 0; i < hash.length; i++){
                    result += Integer.toString( ( hash[i] & 0xff ) + 0x100, 16).substring( 1 );
                }

                fileCheckSum.setText(result);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Date lastModified = new Date(file.lastModified());
            TextView fileLastModified = v.findViewById(R.id.information_file_modification_date);
            fileLastModified.setText(DateFormat.format("EEE, MMM dd, yyyy", lastModified));
            builder.setView(v);
            return builder.create();
        }
    }

}
