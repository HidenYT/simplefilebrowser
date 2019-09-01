package com.tf.simplefilebrowser.alertdialogs;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.tf.simplefilebrowser.R;

public class ChooseFileTypeDialogCreator extends DialogCreator{
    public static final String FILE_NAME_ARG = "NEW_FILE_NAME";
    private static final AlertDialogResultIDs ACTION_FILE = AlertDialogResultIDs.NEW_FILE_TITLE;
    private static final AlertDialogResultIDs ACTION_FOLDER = AlertDialogResultIDs.NEW_FOLDER_TITLE;
    public ChooseFileTypeDialogCreator(Context context){
        super(context);
    }

    public void createDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.new_file);
        View v = LayoutInflater.from(getContext()).inflate(R.layout.new_file_type_choose, null);
        builder.setView(v);
        final AlertDialog alertDialog = builder.create();
        v.findViewById(R.id.file_type_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputTextDialog dialog = new InputTextDialog(getContext(), ACTION_FILE, FILE_NAME_ARG);
                dialog.setActionListener(getListener());
                alertDialog.dismiss();
                dialog.createDialog();
            }
        });
        v.findViewById(R.id.folder_type_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputTextDialog dialog = new InputTextDialog(getContext(), ACTION_FOLDER, FILE_NAME_ARG);
                dialog.setActionListener(getListener());
                alertDialog.dismiss();
                dialog.createDialog();
            }
        });
        alertDialog.show();
    }
}
