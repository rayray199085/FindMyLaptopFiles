package com.example.stephencao.findmylaptopfiles;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class DisplayScriptActivity extends AppCompatActivity {
    private String filePath;
    private String fileType;
    private TextView textView;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String content = (String) msg.obj;
            textView.setText(content);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.script_view);

        Intent intent = getIntent();
        filePath = intent.getStringExtra("path");
        fileType = intent.getStringExtra("type");
        initView();
    }

    private void initView() {
        textView = findViewById(R.id.script_text_view);
        new Thread(new GetScript(filePath)).start();
    }
    class GetScript implements Runnable{
        private String urlPath;

        public GetScript(String urlPath) {
            this.urlPath = urlPath;
            String regex = "/";
            this.urlPath = urlPath.replaceAll(regex, "%2F");
        }
        @Override
        public void run() {
            String linkPath = "http://192.168.0.3:8080/local_files_searching/my_delivery?path=" + urlPath;
            HttpURLConnection httpURLConnection = null;
            try {
                URL url = new URL(linkPath);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setConnectTimeout(5000);
                if (httpURLConnection.getResponseCode() == 200) {
                    InputStream inputStream = httpURLConnection.getInputStream();
                    StringBuffer stringBuffer = new StringBuffer();
                    int len = 0;
                    byte[] buffer = new byte[1024];
                    while ((len = inputStream.read(buffer)) != -1) {
                        stringBuffer.append(new String(buffer,0,len));
                    }
                    Message message = Message.obtain();
                    message.obj = stringBuffer.toString();
                    handler.sendMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
