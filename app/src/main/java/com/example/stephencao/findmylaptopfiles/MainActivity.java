package com.example.stephencao.findmylaptopfiles;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {
    private Button sendBtn;
    private EditText editText;
    private Spinner spinner;
    private final String[] types = {".jpg", ".mp3", ".java", ".py", ".mp4", ".txt"};
    private ListView listView;
    private String fileType = null;
    private List<String> stringList;
    private MySqliteHelper mySqliteHelper;
    private SQLiteDatabase sqLiteDatabase;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Collections.sort(stringList);
            MyBaseAdapter myBaseAdapter = new MyBaseAdapter(getApplicationContext(), stringList);
            listView.setAdapter(myBaseAdapter);
        }
    };

    private Handler typeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String type = (String) msg.obj;
            fileType = type;
            Toast.makeText(getApplicationContext(), "Select " + type + " successfully!", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initView();
    }

    private void initView() {
        spinner = new Spinner(getApplicationContext());
        spinner = findViewById(R.id.spinner_for_type_options);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_items, types);
        spinner.setOnItemSelectedListener(this);
        spinner.setAdapter(arrayAdapter);

        stringList = new ArrayList<>();
        mySqliteHelper = new MySqliteHelper(getApplicationContext());
        editText = findViewById(R.id.edit_text);
        sendBtn = findViewById(R.id.send_button);
        listView = findViewById(R.id.list_view);
        listView.setOnItemClickListener(this);
        sendBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_button: {
                String keyword = editText.getText().toString();
                new Thread(new GetFilesThread(keyword)).start();
                Toast.makeText(getApplicationContext(), "Searching...", Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String text = (String) listView.getItemAtPosition(position);
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        Intent intent = null;
        if (".jpg".equals(fileType)) {
            intent = new Intent(getApplicationContext(), DisplayActivity.class);
        } else if (".mp3".equals(fileType)) {
            intent = new Intent(getApplicationContext(), PlayMediaFileActivity.class);
            intent.putExtra("type", fileType);
        } else if (".py".equals(fileType) || ".java".equals(fileType) || ".txt".equals(fileType)) {
            intent = new Intent(getApplicationContext(), DisplayScriptActivity.class);
            intent.putExtra("type", fileType);
        }
        if (intent != null) {
            intent.putExtra("path", text);
            startActivity(intent);
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        new Thread(new SendOutFileType(types[position])).start();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    class SendOutFileType implements Runnable {
        private String fileType;

        public SendOutFileType(String fileType) {
            this.fileType = fileType;
        }

        @Override
        public void run() {
            try {
                URL url = new URL("http://192.168.0.3:8080/local_files_searching/my_type?file_type=" + fileType);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setConnectTimeout(5000);
                if (httpURLConnection.getResponseCode() == 200) {
                    InputStream inputStream = httpURLConnection.getInputStream();
                    int len = 0;
                    byte[] buffer = new byte[1024];
                    StringBuffer stringBuffer = new StringBuffer();
                    while ((len = inputStream.read(buffer)) != -1) {
                        stringBuffer.append(new String(buffer, 0, len));
                    }
                    Message message = Message.obtain();
                    message.obj = stringBuffer.toString();
                    typeHandler.sendMessage(message);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class GetFilesThread implements Runnable {
        private String key;

        public GetFilesThread(String key) {
            this.key = key;
        }

        @Override
        public void run() {
            String path = "http://192.168.0.3:8080/local_files_searching/my_server?key=" + key;
            try {
                clearDatabaseRecord();
                stringList.clear(); // reset data
                URL url = new URL(path);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setConnectTimeout(5000);
                if (httpURLConnection.getResponseCode() == 200) {
                    InputStream inputStream = httpURLConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String line = null;
                    while ((line = bufferedReader.readLine()) != null) {
                        sqLiteDatabase = mySqliteHelper.getWritableDatabase();
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("location", line);
                        sqLiteDatabase.insert("path", null, contentValues);
                        stringList.add(line);
                    }
                    bufferedReader.close();
                    Message message = Message.obtain();
                    handler.sendMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void clearDatabaseRecord() {
        sqLiteDatabase = mySqliteHelper.getWritableDatabase();
        sqLiteDatabase.execSQL("DELETE FROM path");
    }
}
