package com.example.tony.downloadfileprogress;

import android.Manifest;

import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {

    private Button button;
    private Handler handler;
    private static String file_url = "http://172.26.40.136:8000/";
    private static String file_name = "Pallivallu.mp4";
    private ProgressDialog progressDialog;

    private static final String TAG = "Tony Message : ";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permission_check();

    }

    private void permission_check(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},100);
                return;
            }

        }
        initialize();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 100 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            initialize();
        }else{
            permission_check();
        }
    }

    private void initialize() {
        button = (Button) findViewById(R.id.btnProgress);
        handler = new Handler();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Downloading File...");
        progressDialog.setMax(100);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);


        button.setOnClickListener(
                new View.OnClickListener() {
                      @Override
                      public void onClick(View v) {
                            Thread t = new Thread(new Runnable() {
                                @Override
                                public void run() {

                                    String url_file = file_url + file_name;
                                    Log.v(TAG,"Button Clicked ");
                                    Log.v(TAG,url_file);
                                    OkHttpClient client = new OkHttpClient();
                                    Request request = new Request.Builder().url(file_url + file_name).build();

                                    Response response = null;
                                    try {
                                        response = client.newCall(request).execute();

                                        float file_size = response.body().contentLength();
                                        BufferedInputStream inputStream = new BufferedInputStream(response.body().byteStream());
                                        OutputStream stream = new FileOutputStream(Environment.getExternalStorageDirectory() + "/Download/"+file_name);

                                        byte[] data = new byte[8192];
                                        float total = 0;
                                        int read_bytes = 0;

                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                progressDialog.show();
                                            }
                                        });

                                        while( (read_bytes = inputStream.read(data)) != -1){
                                            total = total + read_bytes;
                                            stream.write(data,0,read_bytes);
                                            progressDialog.setProgress((int)((total/file_size)*100));
                                        }


                                        progressDialog.dismiss();
                                        stream.flush();
                                        stream.close();
                                        response.body().close();

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });
                          t.start();
                      }
                }
        );
    }


}
