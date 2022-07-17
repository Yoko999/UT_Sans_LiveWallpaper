/**  @author LiveWallpaper by Yoko Equilibrium (Yo Equilibrium)
             Undertale/Sans by Toby "Radiation" Fox
    Made for fan Fun and getting some EXP =)
    Send me message if you got bugs / requests / advices =)
    yoequilibrium @ gmail.com
*/
package com.yoequilibrium.sans;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class LiveWallpaperPainting extends Thread implements Runnable {

    private SurfaceHolder surfaceHolder;
    private Context context;

    // Состояние потоков
    private boolean wait;
    private boolean run;

    //Высота и ширина сцены
    public int width;
    public int height;

    private boolean visible;

    //Maybe someday....
    //public List<Bone> bones = new ArrayList<Bone>();

    // Позиция нажатия на экран
    private float posX;
    private float posY;

    //Фоновый рисунок
    private Movie bg;//текущее
    private Movie bgUnTouched;
    private Movie bgTouched;
    private Movie bgZzz;

    private Bitmap bonesHor,bonesVer;
    private Rect bonesCurRectLR=null
        ,bonesCurRectUD=null;
    private int curAction=MotionEvent.ACTION_UP;
    private final int bonesLength = 70;//show bones if screen was touched on lesser than 70px from its borders
    private float /*bgscalex,*/bgscaley;

    private boolean useSound=true;
    private SoundPool sound;
    private int soundId;

    private SansZzzTimerTask zzzTask;
    private long afterTime = 1000*30;//30 sec
    private int sleepTime=200;

    //private final int frameDuration = 20;//20 = 50 frames per second

    private Bitmap mHeart;//heart cursor

    //Конструктор
    public LiveWallpaperPainting(SurfaceHolder surfaceHolder, Context context, Display display,int zzzDelay) {
        this.surfaceHolder = surfaceHolder;
        this.context = context;

        /**Запускаем поток*/
        this.wait = true;

        //Вычисляю размер экрана, чтобы центрировать (ладно, это не помогает)
        Point point = new Point();
        display.getSize(point);
        width=point.x;
        height=point.y;
        Log.d("MY", "Width = " + width + "; Height = " + height);

        afterTime = 1000 * 60 * zzzDelay;
        Log.d("MY","Delay = "+afterTime);

        try {
            //Подготавливаем анимацию
            bgUnTouched = Movie.decodeStream(context.getResources().getAssets().open("sansanimated.gif"));
            bgTouched = Movie.decodeStream(context.getResources().getAssets().open("sansanimated2.gif"));
            bg=bgUnTouched;
            bgZzz=Movie.decodeStream(context.getResources().getAssets().open("zzz.gif"));
            //Достаём звук
            sound = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
            soundId = sound.load(context, R.raw.sans_eye, 1);

            /** UPD 08.04.16 */
            //bgscalex=(float)width/bg.width();
            bgscaley=(float)height/bg.height();
                Log.d("MY SCALE", " Height = " + bgscaley);

            bonesHor = BitmapFactory.decodeResource(context.getResources(), R.drawable.bones_hor650);
            bonesVer = BitmapFactory.decodeResource(context.getResources(), R.drawable.bones_ver);

            //Begin sleep after some time
            Timer zzzTimer=new Timer();
            zzzTask= new SansZzzTimerTask();
            zzzTimer.schedule(zzzTask, afterTime);

            mHeart = BitmapFactory.decodeResource(context.getResources(), R.drawable.heart);
        }catch (IOException ex){
            Log.e("My","Sans не открылся =С "+ex.getMessage());
        }
        this.visible=true;
    }

    //Ставим на паузу анимацию
    public void pausePainting() {
        this.wait = true;
        synchronized (this) {
            this.notify();
        }
    }

    //Запускаем поток когда сняли с паузы
    public void resumePainting() {
        this.wait = false;
        try {
            if (zzzTask == null) {
                Timer zzzTimer = new Timer();
                zzzTask = new SansZzzTimerTask();
                zzzTimer.schedule(zzzTask, afterTime);
            }
        }catch (Exception ex){
            Log.d("MY SansWP","on Resume error:"+ex.getMessage());
        }
        synchronized (this) {
            this.notify();
        }
    }

    // Останавливаем поток
    public void stopPainting() {
        this.run = false;
        sound.release();
        if(zzzTask!=null)
            zzzTask.cancel();
        synchronized (this) {
            this.notify();
        }
    }

    public void enableSound(boolean enable){
        this.useSound = enable;
    }

    //Рисуем в потоке все наши рисунки
    public void run() {
        this.run = true;
        Canvas c = null;
        while (run) {
            try {
                c = surfaceHolder.lockCanvas();
                synchronized (surfaceHolder) {
                    Thread.sleep(sleepTime);
                    doDraw(c);
                }
            } catch (InterruptedException e) {
                Log.e("MY",e.getMessage());
                e.printStackTrace();
            }finally{
                try {
                    if (c != null && surfaceHolder != null) {
                        surfaceHolder.unlockCanvasAndPost(c);
                    }
                }catch (Exception ex){
                    Log.e("My SansWP","Canvas unlock error:"+ex.getLocalizedMessage());//facepalm
                    c = surfaceHolder.lockCanvas();
                }

            }
            // pause if no need to animate
            synchronized (this) {
                if (wait) {
                    try {
                        wait();
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    /**
     * Растягиваем картинку под размер экрана
     * (Походу, оно само как-то растягивает)
     */
    public void setSurfaceSize(int width, int height) {
        this.width = width;
        this.height = height;
        synchronized (this) {
            this.notify();
        }
    }

    /**
     * Обрабатываем нажатия на экран
     */
    public boolean doTouchEvent(MotionEvent event) {
        //TODO:думала направлять кости в сторону тыка
        posX = event.getX();
        posY = event.getY();

        //Log.d("MY","Touch pos X="+posX+" Y="+posY);

        if(event.getAction()==MotionEvent.ACTION_DOWN) {
            bg = bgTouched;
            sleepTime=50;

            if(useSound)
                sound.play(soundId, 0.3f, 0.3f, 1, 0, 1);
            zzzTask.cancel();

            curAction=MotionEvent.ACTION_DOWN;
        }
        if(event.getAction()==MotionEvent.ACTION_UP){
            bg=bgUnTouched;
            sleepTime=200;

            curAction=MotionEvent.ACTION_UP;

            Timer zzzTimer=new Timer();
            zzzTask = new SansZzzTimerTask();
            zzzTimer.schedule(zzzTask,afterTime);
        }
        return true;
    }


    //Рисуем на сцене в потоке
    private void doDraw(Canvas canvas) {
       // canvas.drawBitmap(bg, 0, 0, null);
        if (visible) {
            //canvas = surfaceHolder.lockCanvas();
            //   canvas.save();
            //Регулируем масштаб и положение на экране нашей анимации

            /** UPD 08.04.16 */
            float drawY = 0;//offset y
            float drawX = -(bg.width()-canvas.getWidth()/2);//offset x
                //Log.d("MY","Остаток по краям = "+(-drawX));

            canvas.scale(bgscaley, bgscaley);//растягиваем только по высоте
            if(width<=height)
                drawX-=40;//offset to center ???(on screens with resolution ~ 720x1280 it's ok, but on others all is not ok =C )

            bg.draw(canvas, drawX, drawY);//TODO: надо как-то посерединке разместить

            //---Draw bones on screen borders on touch---
            if (curAction == MotionEvent.ACTION_DOWN) {
                //true visible canvas's size
                int clipW = canvas.getClipBounds().width()+5;
                int clipH = canvas.getClipBounds().height()+5;
                //TODO: maybe will be better to show bones on all touched borders...
                if ((posX) <= bonesLength) {//left
                    bonesCurRectLR = new Rect(-5,0,bonesLength,height);//TODO: если уменьшу картинку, то, возможно, нужно будет поменять height на clipH (с шириной аналогично)
                    canvas.drawBitmap(bonesVer,null,bonesCurRectLR,null);
                } else if ((width - posX) <= bonesLength) {//right
                    bonesCurRectLR = new Rect(clipW-bonesLength,0,clipW,height);//new Rect(width - bonesLength,0,width,height);
                    canvas.drawBitmap(bonesVer, null, bonesCurRectLR, null);
                }
                if ((posY) <= bonesLength) {//up
                    bonesCurRectUD = new Rect(0,5,width,bonesLength);
                    canvas.drawBitmap(bonesHor, null, bonesCurRectUD, null);
                } else if ((height - posY) <= bonesLength) {//down
                    bonesCurRectUD = new Rect(0,clipH-bonesLength,width,clipH);
                    canvas.drawBitmap(bonesHor, null, bonesCurRectUD, null);
                }

                //draw heart
                canvas.drawBitmap(mHeart,posX/bgscaley, posY/bgscaley,null);
            } else if (curAction == MotionEvent.ACTION_UP) {
                bonesCurRectLR=null;
                bonesCurRectUD=null;
            }


        //    canvas.restore();
            bg.setTime((int) (System.currentTimeMillis() % bg.duration()));
        }
    }

    private class SansZzzTimerTask extends TimerTask{
        @Override
        public void run() {
            bg=bgZzz;
            sleepTime=500;
        }
    }
}