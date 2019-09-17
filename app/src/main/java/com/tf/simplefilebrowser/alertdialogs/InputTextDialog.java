package com.tf.simplefilebrowser.alertdialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import com.tf.simplefilebrowser.R;

import java.io.File;

public class InputTextDialog extends DialogCreator {

    private final AlertDialogResultIDs resultID;
    private final String[] argName;
    private File file;

    InputTextDialog(Context context, AlertDialogResultIDs resultID, String argName){
        super(context);
        this.resultID = resultID;
        this.argName = new String[1];
        this.argName[0] = argName;
    }
    InputTextDialog(Context context, AlertDialogResultIDs resultID, File file, String... argNames){
        super(context);
        this.resultID = resultID;
        this.file = file;
        this.argName = argNames;
    }

    public void createDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.new_file);

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        if(file != null){
            input.setText(file.getName());
        }
        builder.setView(input);
        builder.setPositiveButton(R.string.DIALOG_OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!input.getText().toString().equals("")){
                    if(file == null){
                        Bundle b = new Bundle();
                        b.putString(argName[0], input.getText().toString());
                        getListener().onDialogAction(resultID, b);
                    }else{
                        Bundle b = new Bundle();
                        b.putString(argName[0], file.getAbsolutePath());
                        b.putString(argName[1], input.getText().toString());
                        getListener().onDialogAction(resultID, b);
                    }

                }
            }
        });
        builder.setNegativeButton(R.string.DIALOG_CANCEL, null);
        builder.create().show();
    }

}
