package com.forphan.ftpclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
//import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.commons.net.ftp.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {

    EditText et_hostname;
    EditText et_port;
    EditText et_username;
    EditText et_password;
    Button bt_connect;
    String TAG  = "ftpClient";

    public void initWidget(){
        et_hostname = findViewById(R.id.et_host);
        et_port = findViewById(R.id.et_port);
        et_username = findViewById(R.id.et_username);
        et_password = findViewById(R.id.et_password);
        bt_connect = findViewById(R.id.bt_connect);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initWidget();

        bt_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(view.getId()) {
                    case R.id.bt_connect:{
                        String server = et_hostname.getText().toString();
                        String port = et_port.getText().toString();
                        String username = et_username.getText().toString();
                        String password = et_password.getText().toString();
                        Intent intentUpDown = new Intent();
                        intentUpDown.setComponent(new ComponentName(MainActivity.this,UpDown.class));
                        intentUpDown.putExtra("hostname",server);
                        intentUpDown.putExtra("port",port);
                        intentUpDown.putExtra("username",username);
                        intentUpDown.putExtra("password",password);
                        startActivity(intentUpDown);
                    }
                }
            }
        });
    }
}

