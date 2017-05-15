package com.example.zkp.gifdemo;

//This is a sample. In a real project, there'd be a Dialog or an Activity that hosts the MovieView.

import android.graphics.Bitmap;

import java.io.File;
import java.nio.ByteBuffer;

class MyClass {
    private MovieView m_mv; //initialized on loading
    private static boolean s_bHighColor = true;
    //Format; hard-coded here

    public MyClass(MovieView movieView) {
        this.m_mv = movieView;
    }

    //This native method's implementation in in MyClass.cpp
    private native boolean LoadGIF(String FileName, boolean bHighColor);

    //loadLibrary() is called elsewhere 
    //Called from JNI - do not mess with it. 
    private void AddFrame(int Delay, int l, int t, int w, int h, int Disp, byte[] Bits) {
        Bitmap bm = Bitmap.createBitmap(w, h,
                s_bHighColor ?
                        Bitmap.Config.ARGB_8888 :
                        Bitmap.Config.ARGB_4444);
        bm.copyPixelsFromBuffer(ByteBuffer.wrap(Bits).rewind());
        m_mv.AddFrame(bm, l, t, Delay, Disp);
    }

    //Called from JNI - do not mess with it. 
    private void StartImage(int FrameCount) {
        m_mv.Reset(FrameCount);
    }

    //////////////////////////////// The animation starts here
    public void StartMovie(File f) {
        if (LoadGIF(f.getAbsolutePath(), s_bHighColor))
            //This will call Reset(), AddFrames()
            m_mv.Start();
    }
}