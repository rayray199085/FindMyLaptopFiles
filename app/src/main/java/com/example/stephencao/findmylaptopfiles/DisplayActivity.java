package com.example.stephencao.findmylaptopfiles;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

public class DisplayActivity extends AppCompatActivity {
    private ImageView imageView;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bitmap bitmap = (Bitmap) msg.obj;
            imageView.setImageBitmap(bitmap);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_view);
        imageView = findViewById(R.id.image_view);
        getPicPath();
    }

    private void getPicPath() {
        Intent intent = getIntent();
        String path = intent.getStringExtra("path");
        new Thread(new SetPicture(path)).start();
    }

    class SetPicture implements Runnable {
        private String originalPath;

        public SetPicture(String originalPath) {
            String regex = "/";
            this.originalPath = originalPath.replaceAll(regex, "%2F");
        }

        @Override
        public void run() {
            String path = "http://192.168.0.3:8080/local_files_searching/my_delivery?path=" + originalPath;
            HttpURLConnection httpURLConnection = null;
            try {
                URL url = new URL(path);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setConnectTimeout(5000);
                if (httpURLConnection.getResponseCode() == 200) {
                    InputStream inputStream = httpURLConnection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    Message message = Message.obtain();
                    message.obj = bitmap;
                    handler.sendMessage(message);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
