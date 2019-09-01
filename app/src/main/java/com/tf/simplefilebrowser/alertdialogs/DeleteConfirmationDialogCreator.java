package com.tf.simplefilebrowser.alertdialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import com.tf.simplefilebrowser.R;

import java.io.File;
import java.util.LinkedList;

public class DeleteConfirmationDialogCreator extends DialogCreator {
    private final boolean deleteOne;
    private String filePath;
    private LinkedList<String> files;
    public final static String DELETE_FILE_PATH_ARG = "DELETE_FILE_PATH_ARG";
    public final static String DELETE_MULTIPLE_ARG = "DELETE_MULTIPLE_ARG";
    private final AlertDialogResultIDs DELETE_ONE_ID = AlertDialogResultIDs.DELETE_ONE_FILE;
    private final AlertDialogResultIDs DELETE_MULTIPLE_ID = AlertDialogResultIDs.DELETE_MULTIPLE_FILES;

    DeleteConfirmationDialogCreator(Context context, String filePath) {
        super(context);
        this.filePath = filePath;
        this.deleteOne = true;
    }
    public DeleteConfirmationDialogCreator(Context context, LinkedList<String> files) {
        super(context);
        this.files = files;
        this.deleteOne = false;
    }

    @Override
    public void createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.delete_warning_title);
        if(deleteOne){
            File f = new File(filePath);
            builder.setMessage(getContext().getString(R.string.delete_one_warning_text,
                    f.getAbsolutePath()));
        }else{
            builder.setMessage(getContext().getString(R.string.delete_multiple_warning_text,
                    Integer.toString(files.size())));
        }
        builder.setPositiveButton(R.string.DIALOG_OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Bundle b = new Bundle();
                if(deleteOne){
                    b.putString(DELETE_FILE_PATH_ARG, filePath);
                    getListener().onDialogAction(DELETE_ONE_ID, b);
                }else{
                    b.putSerializable(DELETE_MULTIPLE_ARG, files);
                    getListener().onDialogAction(DELETE_MULTIPLE_ID, b);
                }
            }
        });
        builder.setNegativeButton(R.string.DIALOG_NO, null);
        builder.create().show();
    }
}
