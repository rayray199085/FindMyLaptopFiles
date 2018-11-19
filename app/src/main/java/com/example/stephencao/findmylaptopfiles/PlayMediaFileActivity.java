package com.example.stephencao.findmylaptopfiles;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayMediaFileActivity extends AppCompatActivity implements View.OnClickListener {
    private Button playBtn, stopBtn,deleteBtn;
    private TextView textView;
    private String path;
    private ProgressBar progressBar;
    private String fileName;
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String mp3FilePath = (String) msg.obj;
            try {
                mediaPlayer.setDataSource(mp3FilePath);
                mediaPlayer.prepare();
                mediaPlayer.start();
                progressBar.setMax(mediaPlayer.getDuration());
                Timer timer = new Timer();
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        if(mediaPlayer!=null&&mediaPlayer.isPlaying()){
                            new Thread(new SetProgressBarProgress()).start();
                        }
                    }
                };
                timer.schedule(timerTask,0,1000);


            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_view);

        initView();

    }

    private void initView() {
        mediaPlayer = new MediaPlayer();
        progressBar = findViewById(R.id.progress_bar);
        Intent intent = getIntent();
        path = intent.getStringExtra("path");
        String regex = "/[^/]+\\.mp3";
        fileName = null;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(path);
        while (matcher.find()) {
            fileName = matcher.group();
        }
        fileName = fileName.substring(1, fileName.length());
        textView = findViewById(R.id.music_text_view);
        textView.setText(fileName);

        playBtn = findViewById(R.id.music_play_button);
        playBtn.setOnClickListener(this);
        stopBtn = findViewById(R.id.music_stop_btn);
        stopBtn.setOnClickListener(this);
        deleteBtn = findViewById(R.id.music_delete_file_button);
        deleteBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.music_play_button: {
                new Thread(new GetMusicFile(path)).start();
                playBtn.setVisibility(View.GONE);
                break;
            }
            case R.id.music_stop_btn: {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    stopBtn.setText("Continue");
                    stopBtn.setTextColor(Color.YELLOW);
                } else {
                    int position = mediaPlayer.getCurrentPosition();
                    mediaPlayer.seekTo(position);
                    mediaPlayer.start();
                    stopBtn.setText("Stop");
                    stopBtn.setTextColor(Color.RED);
                }
                break;
            }
            case R.id.music_delete_file_button:{
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                }
                else{
                    mediaPlayer.stop();
                }
                File file = new File(Environment.getExternalStorageDirectory(),fileName);
                if(file.exists()){
                    file.delete();
                }
                Toast.makeText(getApplicationContext(),"Delete Successfully",Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }


    class GetMusicFile implements Runnable {
        private String urlPath;

        public GetMusicFile(String urlPath) {
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
                    File file = new File(Environment.getExternalStorageDirectory(), fileName);
                    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
                    int len = 0;
                    byte[] buffer = new byte[1024];
                    while ((len = inputStream.read(buffer)) != -1) {
                        bufferedOutputStream.write(buffer, 0, len);
                    }
                    System.out.println(file.exists());
                    bufferedOutputStream.close();
                    inputStream.close();
                    Message message = Message.obtain();
                    message.obj = file.getAbsolutePath();
                    handler.sendMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class SetProgressBarProgress implements Runnable {
        @Override
        public void run() {
            int location = mediaPlayer.getCurrentPosition();
            progressBar.setProgress(location);

        }
    }
}
