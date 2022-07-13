package com.yoequilibrium.sans;

import android.content.Context;
import android.content.SharedPreferences;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.WindowManager;

public class LiveWallpaperService extends WallpaperService {

    static final String PREFERENCES = "com.yoequilibrium.sans.preferences";

    //public static final String PREFERENCE_SLEEP = "preference_bedtime";
    private int sleepTime=1;

    @Override
    public Engine onCreateEngine() {
        return new SampleEngine();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public class SampleEngine extends Engine implements SharedPreferences.OnSharedPreferenceChangeListener {

        private LiveWallpaperPainting painting;
        private SharedPreferences prefs;
        SurfaceHolder holder;

        SampleEngine() {
            try {
                holder = getSurfaceHolder();
                Context context = getApplicationContext();
                Display display = ((WindowManager)context.getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

                prefs = LiveWallpaperService.this.getSharedPreferences(PREFERENCES, 0);
                prefs.registerOnSharedPreferenceChangeListener(this);
                onSharedPreferenceChanged(prefs, null);

                painting = new LiveWallpaperPainting(holder, getApplicationContext(),display,sleepTime);
            }catch (Exception ex){
                if(ex!=null)
                    Log.e("MY",ex.getMessage());
            }
        }

        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            Log.d("MY prefs","Changed in service");
            sleepTime=Integer.valueOf(prefs.getString("preference_sleeping","1"));
            Log.d("MY","sleep = "+sleepTime);
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            try {
                super.onCreate(surfaceHolder);
                holder = surfaceHolder;
                setTouchEventsEnabled(true);
            }catch (Exception ex){
                Log.e("MY","onCreate time: "+ex.getMessage());
            }
        }

        @Override
        public void onDestroy() {
            try {
                super.onDestroy();
                // remove listeners and callbacks here
                painting.stopPainting();
            }catch (Exception ex){
                Log.e("MY","onDestroy time: "+ex.getMessage());
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            try {
                super.onSurfaceChanged(holder, format, width, height);
                painting.setSurfaceSize(width, height);
            }catch (Exception ex){
                Log.e("MY","onSurfaceChanged time: "+ex.getMessage());
            }
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            try{
                super.onSurfaceCreated(holder);
                painting.start();
            }catch (Exception ex){
                Log.e("MY","onSurfaceCreated time: "+ex.getMessage());
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            try{
                if (visible) {
                    painting.resumePainting();
                } else {
                    // remove listeners and callbacks here
                    painting.pausePainting();
                }
            }catch (Exception ex){
                Log.e("MY","onVisibilityChanged time: "+ex.getMessage());
            }
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
             try{
                super.onSurfaceDestroyed(holder);
                boolean retry = true;
                painting.stopPainting();
                while (retry) {
                    try {
                        painting.join();
                        retry = false;
                    } catch (InterruptedException e) {}
                }
            }catch (Exception ex){
                Log.e("MY","onSurfaceDestoyed time: "+ex.getMessage());
            }
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
             try{
                super.onTouchEvent(event);
                painting.doTouchEvent(event);
            }catch (Exception ex){
                Log.e("MY","onTouch time: "+ex.getMessage());
            }
        }

    }
}