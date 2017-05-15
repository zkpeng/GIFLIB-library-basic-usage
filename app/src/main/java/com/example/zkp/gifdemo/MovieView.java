package com.example.zkp.gifdemo;

import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

//Shows bitmaps on timer. Can be a part of an Android layout.

public class MovieView extends View {
    long m_ts;
    Handler m_h = new Handler();
    Runnable m_Inval = new Runnable() {
        public void run() {
            invalidate();
        }
    };

    static class Frame {
        private Bitmap bm;
        private int l, t;
        private int Delay;
        @SuppressWarnings("unused")
        private int Disp;
    }

    private ArrayList<Frame> m_Frames = null;
    private int m_Pos;
    private Paint m_Paint = new Paint();
    private Canvas m_Canvas;
    private Bitmap m_Surface;
    private Rect m_SrcRc, m_DestRc;

    public MovieView(Context Ctxt) {
        super(Ctxt);
    }

    public MovieView(Context Ctxt, AttributeSet a) {
        super(Ctxt, a);
    }

    @Override
    public void draw(Canvas c) {
        super.draw(c);
        if (m_Frames != null) {
            Frame f = m_Frames.get(m_Pos);
            m_Canvas.drawBitmap(f.bm, f.l, f.t, m_Paint);
            c.drawBitmap(m_Surface, m_SrcRc, m_DestRc, m_Paint);
            m_Pos = (m_Pos + 1) % m_Frames.size();
            if (f.Delay > 0) {
                m_h.removeCallbacks(m_Inval);
                m_h.postDelayed(m_Inval, f.Delay);
            } else
                invalidate();
        }
    }

    public void Reset(int n) {
        m_Frames = new ArrayList<Frame>(n);
    }

    public void AddFrame(Bitmap bm, int l, int t, int Delay, int Disp) {
        Frame f = new Frame();
        f.bm = bm;
        f.Delay = Delay;
        f.Disp = Disp;
        f.l = l;
        f.t = t;
        m_Frames.add(f);
    }

    //Assumes that the frames have been built
    public void Start() {
        m_Pos = 0;
        Bitmap bm = m_Frames.get(0).bm; //First frame
        m_Surface = bm.copy(bm.getConfig(), true);
        m_Canvas = new Canvas(m_Surface);
        int bw, bh;
        m_SrcRc = new Rect(0, 0, (bw = m_Surface.getWidth()) - 1, (bh = m_Surface.getHeight()) - 1);
        int w = getWidth(), h = getHeight();
        //Letterboxing logic conditional on large size???
//        if (w * bh > bw * h) //Needs letterboxing on width
//        {
//            int dx = (w - (h * bw) / bh) / 2;
//            m_DestRc = new Rect(dx, 0, w - 2 * dx - 1, h - 1);
//        } else {
//            int dy = (h - (w * bh) / bw) / 2;
//            m_DestRc = new Rect(0, dy, w - 1, h - 2 * dy - 1);
//        }
        m_DestRc = new Rect(0,0,400,400);
        m_ts = new Date().getTime();
        invalidate();
    }

    public void Stop() {
        m_Frames = null;
        m_Surface = null;
        m_Canvas = null;
        m_h.removeCallbacks(m_Inval);
        invalidate();
    }
}