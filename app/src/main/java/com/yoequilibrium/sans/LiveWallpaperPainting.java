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

    // Состояние потоков //UPD ???
    private boolean wait;
    private boolean run;

    //Высота и ширина сцены
    public int width;
    public int height;


    private boolean visible;
    //private Handler handler;
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
    private final int bonesLength = 100;

    private SoundPool sound;
    private int soundId;

    //Костяшки
    //private Bitmap bone;

    private SansZzzTimerTask zzzTask;
    private long afterTime = 1000*30;//30 sec
    private int sleepTime=200;

    //private final int frameDuration = 20;//20 = 50 frames per second

    private float bgscalex,bgscaley;

    //Конструктор
    public LiveWallpaperPainting(SurfaceHolder surfaceHolder, Context context, Display display,int zzzDelay/*int radius*/) {
        this.surfaceHolder = surfaceHolder;
        //this.handler=handler;

        /**Запускаем поток*/
        this.wait = true;

        //Вычисляю размер экрана, чтобы центрировать
        Point point = new Point();
        display.getSize(point);
        width=point.x;
        height=point.y;
        Log.d("MY", "Width = " + width + "; Height = " + height);


        afterTime = 1000 * 60 * zzzDelay;
        Log.d("MY","Delay = "+afterTime);
                /*Рисуем всякое разное*/
        // bone = BitmapFactory.decodeResource(context.getResources(), R.drawable.bone);
        //blood = BitmapFactory.decodeResource(context.getResources(), R.drawable.blood1);
        try {
            //Подготавливаем анимацию
            bgUnTouched = Movie.decodeStream(context.getResources().getAssets().open("sansanimated.gif"));//BitmapFactory.decodeResource(context.getResources(), R.drawable.sansanimated);
            bgTouched = Movie.decodeStream(context.getResources().getAssets().open("sansanimated2.gif"));//BitmapFactory.decodeResource(context.getResources(), R.drawable.sansanimated);
            bg=bgUnTouched;
            bgZzz=Movie.decodeStream(context.getResources().getAssets().open("zzz.gif"));

            Log.d("MY BG", "Width = " + bg.width() + "; Height = " + bg.height());
            bgscalex=(float)width/bg.width();
            bgscaley=(float)height/bg.height();
            Log.d("MY SCALE", "Width = " + bgscalex + "; Height = " + bgscaley);

            //Достаём звук
            sound = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
            soundId = sound.load(context, R.raw.sans_eye, 1);

            bonesHor = BitmapFactory.decodeResource(context.getResources(), R.drawable.bones_hor);
            bonesVer = BitmapFactory.decodeResource(context.getResources(), R.drawable.bones_ver);
/*
            boneHorLeft = new ImageView(context);
            boneHorLeft.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.bones_hor));
            boneHorLeft.setX(0);boneHorLeft.setY(height / 2);

            boneVerUp = new ImageView(context);
            boneVerUp.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.bones_ver));
            boneVerUp.setX(width/2);boneVerUp.setY(0);

            boneHorRight = new ImageView(context);
            boneHorRight.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.bones_hor));
            boneHorRight.setX(width);boneHorRight.setY(height / 2);

            boneVerDown = new ImageView(context);
            boneVerDown.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.bones_ver));
            boneVerDown.setX(width/2);boneVerDown.setY(height);

            slideFromHorUp = new TranslateAnimation(0, boneHorLeft.getWidth(), 0,0);
            slideAwayHorUp = new TranslateAnimation(boneHorLeft.getWidth(), 0, 0, 0);

            slideFromHorDown = new TranslateAnimation(width, width-boneHorRight.getWidth(), 0, 0);
            slideAwayHorDown = new TranslateAnimation(width-boneHorRight.getWidth(), width, 0,0);

            slideFromHorUp.setDuration(10);
            slideAwayHorUp.setDuration(10);
            slideFromHorDown.setDuration(10);
            slideAwayHorDown.setDuration(10);

            slideFromVerUp = new TranslateAnimation(0,0, 0, boneVerUp.getHeight());
            slideAwayVerUp = new TranslateAnimation(0,0, boneVerUp.getHeight(), 0);

            slideFromVerDown = new TranslateAnimation(0,0, height, height-boneVerDown.getWidth());
            slideAwayVerDown = new TranslateAnimation(0,0, height-boneVerDown.getWidth(), height);

            slideFromVerUp.setDuration(10);
            slideAwayVerUp.setDuration(10);
            slideFromVerDown.setDuration(10);
            slideAwayVerDown.setDuration(10);
*/

            //Begin sleep after some time
            Timer zzzTimer=new Timer();
            zzzTask= new SansZzzTimerTask();
            zzzTimer.schedule(zzzTask, afterTime);//TODO:надо потом это время из настроек вытаскивать
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
        synchronized (this) {
            this.notify();
        }
    }

    // Останавливаем поток
    public void stopPainting() {
        this.run = false;
        sound.release();
        zzzTask.cancel();
        synchronized (this) {
            this.notify();
        }
    }

    //Рисуем в потоке все наши рисунки
    public void run() {
        this.run = true;
        Canvas c = null;
        while (run) {
            try {
                c = surfaceHolder.lockCanvas();//(null);
                synchronized (surfaceHolder) {
                    Thread.sleep(sleepTime);
                    doDraw(c);
                }
            } catch (InterruptedException e) {
                Log.e("MY",e.getMessage());
                e.printStackTrace();
            }finally{
                if (c != null && surfaceHolder!=null) {
                    surfaceHolder.unlockCanvasAndPost(c);
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
     */
    public void setSurfaceSize(int width, int height) {
        this.width = width;
        this.height = height;
        synchronized (this) {
            this.notify();
           // bg = Bitmap.createScaledBitmap(bg, width, height, true);
            /*try {
                bg = Movie.decodeStream(context.getResources().getAssets().open("sansanimated.gif"));//BitmapFactory.decodeResource(context.getResources(), R.drawable.sansanimated);
            }catch (IOException ex){
                Log.e("My","Sans не открылся =С "+ex.getMessage());
            }*/
        }
    }

    /**
     * Обрабатываем нажатия на экран
     */
    public boolean doTouchEvent(MotionEvent event) {
        //TODO:думала направлять кости в сторону тыка
        posX = event.getX();
        posY = event.getY();

        if(event.getAction()==MotionEvent.ACTION_DOWN) {
            bg = bgTouched;//Movie.decodeStream(context.getResources().getAssets().open("sansanimated2.gif"));//BitmapFactory.decodeResource(context.getResources(), R.drawable.sansanimated);
            sleepTime=50;

            sound.play(soundId, 0.3f, 0.3f, 1, 0, 1);
            zzzTask.cancel();
            //bones.add(new Bone(this, bone, event.getX(), event.getY()));


            //doDrawBones(MotionEvent.ACTION_DOWN, posX, posY);
            curAction=MotionEvent.ACTION_DOWN;

            /*if((posX)<=100){//слева
                boneVer.startAnimation(slideFromHorUp);
            }else if((width-posX)<=100){//справа
                boneVer.startAnimation(slideFromHorDown);
            }
            if((posY)<=100){//сверху
                boneHor.startAnimation(slideFromVerUp);
            }else if((height-posY)<=100){//снизу
                boneHor.startAnimation(slideFromVerDown);
            }*/

        }
        if(event.getAction()==MotionEvent.ACTION_UP){
            bg=bgUnTouched;
            /*try {
                bg = Movie.decodeStream(context.getResources().getAssets().open("sansanimated.gif"));//BitmapFactory.decodeResource(context.getResources(), R.drawable.sansanimated);
            }catch (IOException ex){
                Log.e("My","Sans не открылся =С "+ex.getMessage());
            }*/
            sleepTime=200;

            /*if((posX)<=100){//слева
                boneVer.startAnimation(slideAwayHorUp);
            }else if((width-posX)<=100){//справа
                boneVer.startAnimation(slideAwayHorDown);
            }
            if((posY)<=100){//сверху
                boneHor.startAnimation(slideAwayVerUp);
            }else if((height-posY)<=100){//снизу
                boneHor.startAnimation(slideAwayVerDown);
            }*/

            //doDrawBones(MotionEvent.ACTION_UP, posX, posY);
            curAction=MotionEvent.ACTION_UP;

            Timer zzzTimer=new Timer();
            zzzTask = new SansZzzTimerTask();
            zzzTimer.schedule(zzzTask,afterTime);
        }
        /*synchronized (surfaceHolder) {
            for (int i = bubble.size() - 1; i >= 0; i--) {
                Bone sprite = bubble.get(i);
                if (sprite.isCollition(posX, posY)) {
                    bubble.remove(sprite);
                   // score++;
                    //temps.add(new Boms(temps, this, blood, posX, posY));
                    break;
                }
            }
        }*/
        return true;
    }

    //Рисуем на сцене в потоке
    /*private void doDrawBones(int action,float posX,float posY) {
        Canvas canvas=null;
        try {
            if (visible) {
                canvas = surfaceHolder.lockCanvas();
                canvas.save();
                //Регулируем масштаб и положение на экране нашей анимации
                canvas.scale(2.5f, 2.5f);

                if (action == MotionEvent.ACTION_DOWN) {
                    if ((posX) <= 100) {//слева
                        boneHorLeft.startAnimation(slideFromHorUp);
                    } else if ((width - posX) <= 100) {//справа
                        boneHorRight.startAnimation(slideFromHorDown);
                    }
                    if ((posY) <= 100) {//сверху
                        boneVerUp.startAnimation(slideFromVerUp);
                    } else if ((height - posY) <= 100) {//снизу
                        boneVerDown.startAnimation(slideFromVerDown);
                    }
                } else if (action == MotionEvent.ACTION_UP) {
                    if ((posX) <= 100) {//слева
                        boneHorLeft.startAnimation(slideAwayHorUp);
                    } else if ((width - posX) <= 100) {//справа
                        boneHorRight.startAnimation(slideAwayHorDown);
                    }
                    if ((posY) <= 100) {//сверху
                        boneVerUp.startAnimation(slideAwayVerUp);
                    } else if ((height - posY) <= 100) {//снизу
                        boneVerDown.startAnimation(slideAwayVerDown);
                    }
                }
                canvas.restore();
            }
        }catch (Exception ex){
            Log.e("MY",ex.getMessage());
        }finally {
            if (canvas != null && surfaceHolder != null)
                surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }*/

    /*private void doDrawBones(int action,float posX,float posY) {
        Canvas canvas=null;
        try {
            if (visible) {
                canvas = surfaceHolder.lockCanvas();

                if (action == MotionEvent.ACTION_DOWN) {
                    canvas.save();
                    if ((posX) <= 100) {//слева
                        Rect rect = new Rect(0,0,100,height);
                        //canvas.clipRect(rect);
                        canvas.drawBitmap(bonesVer,null,rect,null);

                    } else if ((width - posX) <= 100) {//справа
                        Rect rect = new Rect(width - 100,0,width,height);
                        //canvas.clipRect(rect);
                        canvas.drawBitmap(bonesVer, null, rect, null);
                    }
                    if ((posY) <= 100) {//сверху
                        Rect rect = new Rect(0,0,width,100);
                        //canvas.clipRect(rect);
                        canvas.drawBitmap(bonesHor, null, rect, null);
                    } else if ((height - posY) <= 100) {//снизу
                        Rect rect = new Rect(0,height - 100,width,height);
                        //canvas.clipRect(rect);
                        canvas.drawBitmap(bonesHor, null, rect, null);
                    }
                } else if (action == MotionEvent.ACTION_UP) {
                    if ((posX) <= 100) {//слева
                    } else if ((width - posX) <= 100) {//справа
                    }
                    if ((posY) <= 100) {//сверху
                    } else if ((height - posY) <= 100) {//снизу
                    }
                }
                canvas.restore();
            }
        }catch (Exception ex){
            Log.e("MY",ex.getMessage());
        }finally {
            if (canvas != null && surfaceHolder != null)
                surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }*/


    //Рисуем на сцене в потоке
    private void doDraw(Canvas canvas) {
       // canvas.drawBitmap(bg, 0, 0, null);
        if (visible) {
            //canvas = surfaceHolder.lockCanvas();
            //canvas.drawColor(Color.WHITE);
            canvas.save();
                //Регулируем масштаб и положение на экране нашей анимации
               // canvas.scale(2.5f, 2.6f);

            float ostatok = (2*bg.width()-canvas.getWidth())/2;
            Log.d("MY","Остаток по краям = "+ostatok);

            float drawY = 0;
            float drawX = -ostatok;//Math.abs(canvas.getWidth()/2-bgscaley*bg.width()/2);


            if(width<=height) {
                canvas.scale(/*bgscalex*/bgscaley, bgscaley);//TODO: надо как-то по-другому рассчитывать, упоротая херня выходит в широком
                drawX-=40;
            }else {//TODO
                //drawX=bg.width()/4-surfaceHolder.getSurfaceFrame().width()/2;
                Log.d("MY","SCALE y = "+bgscaley);
                canvas.scale(/*bgscalex*/bgscaley, bgscaley);
                /*bgscalex=(float)height/bg.height();
                bgscaley=(float)width/bg.width();
                canvas.scale(bgscalex, bgscaley);*/
            }

            /*
            Log.d("MY","CANVAS RECT = "+canvas.getClipBounds().toString());
            Log.d("MY","SURGACE RECT = "+surfaceHolder.getSurfaceFrame().toString());

            Log.d("MY","CANVAS: clip w="+canvas.getClipBounds().width()+" h="+ canvas.getClipBounds().height());
            Log.d("MY", "CANVAS: w=" + canvas.getWidth()+ " h=" + canvas.getHeight());
            Log.d("MY", "CANVAS: cx=" + canvas.getClipBounds().exactCenterX() + " cy=" + canvas.getClipBounds().exactCenterY());
            */


            //растягиваем только (!) по высоте

            /*if(bg.equals(bgZzz))
                drawX=-70;*/

            bg.draw(canvas,drawX,drawY);//TODO: надо как-то посерединке разместить



            int clipW = canvas.getClipBounds().width();
            int clipH = canvas.getClipBounds().height();

            if (curAction == MotionEvent.ACTION_DOWN) {
                Log.d("MY", "Cur pos: x=" + posX + "   y=" + posY);

                if ((posX) <= bonesLength) {//слева
                    bonesCurRectLR = new Rect(-5,0,bonesLength-30,height);//TODO: если уменьшу картинку, то, возможно, нужно будет поменять height на clipH (с шириной аналогично)
                    canvas.drawBitmap(bonesVer,null,bonesCurRectLR,null);

                } else if ((width - posX) <= bonesLength) {//справа
                    bonesCurRectLR = new Rect((clipW+5)-(bonesLength-30),0,clipW+5,height);//new Rect(width - bonesLength,0,width,height);
                    canvas.drawBitmap(bonesVer, null, bonesCurRectLR, null);
                }
                if ((posY) <= bonesLength) {//сверху
                    bonesCurRectUD = new Rect(0,5,width,bonesLength-30);
                    canvas.drawBitmap(bonesHor, null, bonesCurRectUD, null);
                } else if ((height - posY) <= bonesLength) {//снизу
                    bonesCurRectUD = new Rect(0,(clipH+5)-(bonesLength-30),width,clipH+5);
                    canvas.drawBitmap(bonesHor, null, bonesCurRectUD, null);
                }
                /*if ((posX) <= bonesLength) {//слева
                    bonesCurRectLR = new Rect(-5,0,bonesLength-30,height);
                    canvas.drawBitmap(bonesVer,null,bonesCurRectLR,null);

                } else if ((width - posX) <= bonesLength) {//справа
                    bonesCurRectLR = new Rect(width - width+230,0,width-420,height);//new Rect(width - bonesLength,0,width,height);
                    canvas.drawBitmap(bonesVer, null, bonesCurRectLR, null);
                }
                if ((posY) <= bonesLength) {//сверху
                    bonesCurRectUD = new Rect(0,0,width,bonesLength);
                    canvas.drawBitmap(bonesHor, null, bonesCurRectUD, null);
                } else if ((height - posY) <= bonesLength) {//снизу
                    bonesCurRectUD = new Rect(0,height - height + 400,width,height-600);
                    canvas.drawBitmap(bonesHor, null, bonesCurRectUD, null);
                }*/
            } else if (curAction == MotionEvent.ACTION_UP) {
                bonesCurRectLR=null;
                bonesCurRectUD=null;
            }


            canvas.restore();
            //surfaceHolder.unlockCanvasAndPost(canvas);
            bg.setTime((int) (System.currentTimeMillis() % bg.duration()));

            //handler.removeCallbacks(this);
           // handler.postDelayed(this, frameDuration);

                /*for (Bone bon : bones) {
                    if ((/*bon.y > 0 &&*//* bon.y < height) && (/*bon.x > 0 &&*/ /*bon.x < width))
                        bon.onDraw(canvas);
                    else
                        bones.remove(this);
                }*/
        }
                        /*for (int i = temps.size() - 1; i >= 0; i--) {
                    temps.get(i).onDraw(canvas);
                        }*/

        //canvas.drawText("Score: " + score, 50, 70, mScorePaint);
    }


    private class SansZzzTimerTask extends TimerTask{
        @Override
        public void run() {
            bg=bgZzz;
            sleepTime=500;
        }
    }
}