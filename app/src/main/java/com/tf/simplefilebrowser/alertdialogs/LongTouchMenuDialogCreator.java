package com.tf.simplefilebrowser.alertdialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.tf.simplefilebrowser.R;

import java.io.File;

public class LongTouchMenuDialogCreator extends DialogCreator {
    private final File file;
    public static final String PRESSED_FILE_PATH_ARG = "PRESSED_FILE_ARG";
    public static final String FILE_NEW_NAME_ARG = "FILE_NEW_NAME_ARG";
    public LongTouchMenuDialogCreator(Context context, File pressedFile) {
        super(context);
        this.file = pressedFile;
    }

    @Override
    public void createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View v = LayoutInflater.from(getContext()).inflate(R.layout.long_touch_menu_items, null);
        builder.setView(v);
        final AlertDialog dialog = builder.create();
        v.findViewById(R.id.delete_button_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeleteConfirmationDialogCreator dialogCreator =
                        new DeleteConfirmationDialogCreator(getContext(), file.getAbsolutePath());
                dialogCreator.setActionListener(getListener());
                dialog.dismiss();
                dialogCreator.createDialog();
            }
        });
        v.findViewById(R.id.copy_button_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Bundle b = new Bundle();
                b.putString(PRESSED_FILE_PATH_ARG, file.getAbsolutePath());
                getListener().onDialogAction(AlertDialogResultIDs.COPY_FILE, b);
            }
        });
        v.findViewById(R.id.move_button_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Bundle b = new Bundle();
                b.putString(PRESSED_FILE_PATH_ARG, file.getAbsolutePath());
                getListener().onDialogAction(AlertDialogResultIDs.MOVE_FILE, b);
            }
        });
        v.findViewById(R.id.rename_button_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputTextDialog dialogCreator =
                        new InputTextDialog(getContext(), AlertDialogResultIDs.RENAME_FILE,
                                file, PRESSED_FILE_PATH_ARG, FILE_NEW_NAME_ARG);
                dialogCreator.setActionListener(getListener());
                dialog.dismiss();
                dialogCreator.createDialog();
            }
        });
        v.findViewById(R.id.compress_button_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Bundle b = new Bundle();
                b.putString(PRESSED_FILE_PATH_ARG, file.getAbsolutePath());
                getListener().onDialogAction(AlertDialogResultIDs.COMPRESS_FILE, b);
            }
        });
        v.findViewById(R.id.info_button_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FileInformationDialogCreator dialogCreator =
                        new FileInformationDialogCreator(getContext(), file);
                dialogCreator.setActionListener(getListener());
                dialog.dismiss();
                dialogCreator.createDialog();
            }
        });
        dialog.show();
    }
}
