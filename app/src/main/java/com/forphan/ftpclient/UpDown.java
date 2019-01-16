package com.forphan.ftpclient;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class UpDown extends AppCompatActivity {

    TextView tv_list;
    EditText et_target;
    Button bt_download;
    Button bt_upload;
    Button bt_disconnect;
    Button bt_forward;
    Button bt_backward;

    FTPClient ftpClient = new FTPClient();
    String server;
    int port;
    String username;
    String password;

    String TAG = "UpDown";
    String targetFile;
    String localPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                tv_list.setText((String)msg.obj);
            }else {
                Toast.makeText(UpDown.this, "Get none", Toast.LENGTH_SHORT).show();
            }
        }
    };



    public  void initWidget(){
        tv_list = findViewById(R.id.tv_local_and_remote);
        et_target = findViewById(R.id.et_filename);
        bt_download = findViewById(R.id.bt_download);
        bt_upload = findViewById(R.id.bt_upload);
        bt_disconnect = findViewById(R.id.bt_disconnect);
        bt_forward = findViewById(R.id.bt_forward);
        bt_backward = findViewById(R.id.bt_backward);
    }

    public boolean download(String targetFile){
        try{
            // retrieve file from remote
            String remoteFileName = targetFile;
            File localFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/"+remoteFileName);
            OutputStream outputStream = new FileOutputStream(localFile);
            Log.i(TAG, "download: start download");
            boolean check = ftpClient.retrieveFile(remoteFileName,outputStream);
            outputStream.close();
            return true;
        }catch(Exception e){
            // catch error
            Log.e(TAG, "download " + e.toString());
            return false;
        }
    }

    public boolean upload(String targetFile){
        try{
            String localFileName = targetFile;
            File localFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/"+localFileName);
            InputStream inputStream = new FileInputStream(localFile);
            boolean check = ftpClient.storeFile(localFile.getName(),inputStream);
            inputStream.close();
            return true;
        }catch(Exception e){
            // catch error
            Log.e(TAG, "download " + e.toString());
            return false;
        }
    }
    public void changeShowDir(String dir){
        String listDir = "";
        try{
            Log.i(TAG, "changeShowDir: " + ftpClient.printWorkingDirectory()+"/"+dir);
            ftpClient.changeWorkingDirectory(ftpClient.printWorkingDirectory()+"/"+dir);
            Log.i(TAG, "changeShowDir: " + ftpClient.printWorkingDirectory());
        }catch(Exception e){
            Log.e(TAG, "onClick: "+ e.toString());
            //return false;
        }finally{
            try{
                FTPFile ftpFiles[] = ftpClient.listFiles();
                listDir = "Remote Total " + ftpFiles.length + " files\n";
                Log.i(TAG, "changeShowDir: test" + listDir);
                for (FTPFile file : ftpFiles){
                    listDir = listDir + file.getName() + "\n";
                    Log.i(TAG, "changeShowDir: " + file.getName());
                }
            }catch (Exception e){
                Log.e(TAG, "onClick: " + e.toString() );
            }
        }
        Message message = new Message();
        message.what = 1;
        message.obj = listDir;
        Log.i(TAG, "changeShowDir: " + listDir);
        handler.sendMessage(message);
        //return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.updown);

        initWidget();

        // get info from mainActivity
        Intent intentGetMain = getIntent();
        server = intentGetMain.getStringExtra("hostname");
        port = Integer.parseInt(intentGetMain.getStringExtra("port"));
        username = intentGetMain.getStringExtra("username");
        password = intentGetMain.getStringExtra("password");
        Log.i(TAG, "onCreate: " + server + port + username + password);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    // try to connect
                    ftpClient.connect(server);
                    ftpClient.login(username,password);
                    Log.i(TAG, "onCreate: " + ftpClient.getReplyCode());
                    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                    ftpClient.enterLocalPassiveMode();
                    // check connection
                    if(!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())){
                        ftpClient.disconnect();
                        System.exit(1);
                    }


                    // local directory
                    File localDir = new File(localPath);
                    String DirList = "Local total " + localDir.listFiles().length + " files\n";
                    for (File file : localDir.listFiles()){
                        DirList = DirList + file.getName() + "\n";
                    }
                    // remote directory
                    FTPFile remoteDirFiles[] = ftpClient.listFiles(".");
                    DirList = DirList + "\nRemote total " + remoteDirFiles.length + " files\n";
                    for (FTPFile file : remoteDirFiles){
                        DirList = DirList + file.getName() + "\n";
                    }
                    Log.i(TAG, "run: " + DirList);
                    Message message = new Message();
                    message.what = 1;
                    message.obj = DirList;
                    handler.sendMessage(message);
                }catch(Exception e){
                    // catch error
                    Log.e(TAG, "func: " + e.toString());
                }
            }
        }).start();

        bt_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick: " + "click download");
                targetFile = et_target.getText().toString();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(download(targetFile)){
                            //Toast.makeText(UpDown.this, "Downloaded", Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "run: " + "Downloaded");
                        } else {
                            //Toast.makeText(UpDown.this, "Failed", Toast.LENGTH_SHORT).show();
                            Log.w(TAG,"run: " + "Failed");
                        }
                    }
                }).start();
            }
        });
        bt_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick: " + "click upload");
                targetFile = et_target.getText().toString();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(upload(targetFile)){
                            //Toast.makeText(UpDown.this, "Uploaded", Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "run: " + "Uploaded");
                        } else {
                            Log.e(TAG, "run: " + "Failed");
                        }
                    }
                }).start();
            }
        });
        bt_disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick: " + "click disconnect");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            ftpClient.logout();
                            if (ftpClient.isConnected()){
                                ftpClient.disconnect();
                            }
                        }catch(Exception e){
                            Log.e(TAG, "func: " + e.toString());
                        }finally{
                            finish();
                        }
                    }
                }).start();
            }
        });
        bt_forward.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Log.i(TAG, "onClick: " + "click forward");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String childDir = et_target.getText().toString();
                        changeShowDir(childDir);
                    }
                }).start();
            }
        });
        bt_backward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick: " + "click backward");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        changeShowDir("..");
                    }
                }).start();
            }
        });
    }
}
