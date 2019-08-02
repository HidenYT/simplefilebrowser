package com.tf.simplefilebrowser.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.support.v7.app.AppCompatActivity;

import com.tf.simplefilebrowser.R;

public class InformationActivity extends AppCompatActivity {
    private TextView mVkLinkView;
    private final String VK_LINK = "https://vk.com/touchforce";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);
        getSupportActionBar().setTitle("About app");
        mVkLinkView = findViewById(R.id.vk_link);
        mVkLinkView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(VK_LINK));
                startActivity(browserIntent);
            }
        });
    }
}
