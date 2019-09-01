package com.tf.simplefilebrowser.alertdialogs;

import android.content.Context;

public abstract class DialogCreator {
    private final Context context;
    private DialogActionListener listener;

    DialogActionListener getListener(){
        return listener;
    }
    protected Context getContext(){
        return context;
    }
    DialogCreator(Context context){
        this.context = context;

    }

    public void setActionListener(DialogActionListener listener){
        this.listener = listener;
    }

    public abstract void createDialog();
}
