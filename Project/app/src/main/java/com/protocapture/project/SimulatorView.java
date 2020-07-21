package com.protocapture.project;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SimulatorView extends SurfaceView implements
        SurfaceHolder.Callback {
    private final String TAG = "ALLISON";
    //private Bitmap mBitmap;
    final SurfaceHolder surfaceHolder;
    private Paint paint = new Paint();
    private final BlockingQueue<Runnable> mQueue = new LinkedBlockingQueue<>();
    private static final Runnable POISON = new Runnable() {
        @Override
        public void run() {}
    };
    private Boolean surfaceAvailable = false;

    public SimulatorView(Context context) {
        super(context);
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        setFocusable(true);
        paint.setFilterBitmap(true);
    }

    public SimulatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        setFocusable(true);
        paint.setFilterBitmap(true);
    }

    public void stopThread() {
        mQueue.clear();
        mQueue.add(POISON);
    }

    public void drawBitmap(Bitmap bitmap) {
        final Bitmap mBitmap = bitmap;

        Runnable nextFrame = new Runnable() {
            @Override
            public void run() {
                Canvas canvas = null;
                try {
                    canvas = surfaceHolder.lockCanvas(null);
                    canvas.drawBitmap(mBitmap, null, new RectF(0, 0, canvas.getWidth(), canvas.getHeight()), paint);
                } finally {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        };
        mQueue.add(nextFrame);
    }

   /* public void drawFrame(Canvas canvas) {
        canvas.drawBitmap(mBitmap, null, (Rect) null, paint);
    }*/

    // https://www.youtube.com/watch?v=UPq1LDxL5_w
    private void drawThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Runnable runnable;
                    try {
                        runnable = mQueue.take();
                    } catch(InterruptedException e) {
                        return;
                    }
                    if(runnable == POISON) {
                        return;
                    }
                    runnable.run();
                }
            }
        }).start();
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        surfaceAvailable = false;
        Log.i(TAG, "Surface Destroyed");
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width,
                               int height) {
        Log.i(TAG, "Surface changed");
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        surfaceAvailable = true;
        Log.i(TAG, "Surface Created");
        drawThread();
    }

}
