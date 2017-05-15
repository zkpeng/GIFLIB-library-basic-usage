package com.example.zkp.gifdemo;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private MovieView movieView;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("giflib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        movieView = (MovieView) findViewById(R.id.mv);
        AssetManager am = getAssets();
        InputStream inputStream = null;
        try {
            inputStream = am.open("ddd.gif");
        } catch (IOException e) {
            e.printStackTrace();
        }
        File file = createFileFromInputStream(inputStream);
        MyClass myClass = new MyClass(movieView);
        myClass.StartMovie(file);
    }


    private File createFileFromInputStream(InputStream inputStream) {

        try {
            File f = new File(Environment.getExternalStorageDirectory(),"temp.gif");
            OutputStream fos = new FileOutputStream(f,true);
            byte buffer[] = new byte[1024];
            int length = 0;

            while ((length = inputStream.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            fos.close();
            inputStream.close();
            return f;
        } catch (IOException e) {
            Log.e("",e.getMessage());
        }
        return null;
    }
}
