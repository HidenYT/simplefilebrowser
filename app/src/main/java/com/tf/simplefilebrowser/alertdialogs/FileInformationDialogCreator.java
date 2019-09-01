package com.tf.simplefilebrowser.alertdialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.tf.simplefilebrowser.R;
import com.tf.simplefilebrowser.helpers.FileFoldersLab;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Date;

public class FileInformationDialogCreator extends DialogCreator{
    private final File file;

    FileInformationDialogCreator(Context context, File file) {
        super(context);
        this.file = file;
    }

    @Override
    public void createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View v = LayoutInflater.from(getContext()).inflate(R.layout.file_information_fragment, null);
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
        fileCheckSum.setText(FileFoldersLab.getFileMD5(file));
        Date lastModified = new Date(file.lastModified());
        TextView fileLastModified = v.findViewById(R.id.information_file_modification_date);
        fileLastModified.setText(DateFormat.format("EEE, MMM dd, yyyy", lastModified));
        builder.setView(v);
        builder.create().show();
    }
}
