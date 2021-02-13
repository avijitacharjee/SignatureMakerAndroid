package com.avijit.signaturemaker.Views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.ColorInt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Avijit Acharjee on 2/12/2021 at 6:36 PM.
 * Email: avijitach@gmail.com.
 */
public class SignatureView extends View {
    private static float TOUCH_TOLERANCE = 4;
    private Bitmap bitmap;
    private Path path;
    private Paint bitmapPaint;
    private Paint paint;
    private float pensize = 10;
    private boolean drawMode;
    private float x, y;
    private Canvas canvas;

    public SignatureView(Context context, AttributeSet attributeSet, int defStyles) {
        super(context, attributeSet, defStyles);
        init();
    }

    public SignatureView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public SignatureView(Context context) {
        this(context, null);
    }

    public void init() {
        path = new Path();
        bitmapPaint = new Paint(Paint.DITHER_FLAG);
        initializePen();
    }
    public void clear() {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        invalidate();
    }
    public void setPenColor(@ColorInt int color) {
        paint.setColor(color);
    }
    private void initializePen(){
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(pensize);
        drawMode = true;
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(bitmap, 0, 0, bitmapPaint);
        canvas.drawPath(path, paint);
    }

    private void touchStart(float x, float y) {
        path.reset();
        path.moveTo(x, y);
        this.x = x;
        this.y = y;
        canvas.drawPath(path, paint);
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - this.x);
        float dy = Math.abs(y - this.y);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            path.quadTo(this.x, this.y, (x + this.x) / 2, (y + this.y) / 2);
            this.x = x;
            this.y = y;
        }
    }

    private void touchUp() {
        path.lineTo(x, y);
        canvas.drawPath(path, paint);
        path.reset();
        if (drawMode) {
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
        } else {
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!drawMode) {
                    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                } else {
                    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
                }
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x,y);
                if(!drawMode){
                    path.lineTo(this.x,this.y);
                    path.reset();
                    path.moveTo(x,y);
                }
                canvas.drawPath(path,paint);
                invalidate();
                break;
            case  MotionEvent.ACTION_UP :
                touchUp();
                invalidate();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if(bitmap == null){
            bitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
        }
        canvas = new Canvas(bitmap);
        canvas.drawColor(Color.TRANSPARENT);
    }
    public void saveToDevice() {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                File myDir = new File(root + "/Signature");
                if (!myDir.exists()) {
                    myDir.mkdirs();
                }
                // Create imageDir
                File mypath = new File(myDir, "Signature_" + System.currentTimeMillis() + ".jpg");

                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(mypath);
                    // Use the compress method on the BitMap object to write image to the OutputStream
                    FileOutputStream finalFos = fos;
                    takeScreenShot(SignatureView.this).compress(Bitmap.CompressFormat.PNG, 100, finalFos);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        fos.flush();
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    public Bitmap takeScreenShot(View view) {
        view.setDrawingCacheEnabled(true);
        view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
        view.buildDrawingCache();

        if (view.getDrawingCache() == null) return null;
        Bitmap snapshot = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        view.destroyDrawingCache();

        return snapshot;
    }
}
