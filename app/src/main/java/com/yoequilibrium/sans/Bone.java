package com.yoequilibrium.sans;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;

import java.util.Random;

public class Bone {
    /**Позиция по Х и У*/
    public int x;
    public int y;

    /**Точка направления*/
    public float toX;
    public float toY;

    /**Выоста и ширина*/
    public int widht;
    public int height;
    //или просто масштаб?

    /**Скорость*/
    public int speed;

    /**Угол полета*/
    public double angle;

    /**Угол вращения изображения?*/
    //public double boneAngle;

    Bitmap bmp;
    LiveWallpaperPainting pm;

    /**Конструктор*/
    public Bone(LiveWallpaperPainting pm, Bitmap bmp, float toX, float toY) {
        this.pm = pm;
        this.bmp = bmp;

        this.toX = toX;
        this.toY = toY;

        /**По "х" у нас будем появляться рандомно*/
        Random rnd = new Random(System.currentTimeMillis());
        this.y = 0;


        Log.d("MY","LW width = "+pm.width+"; heigh = "+pm.height);
        Log.d("MY","ToX = "+toX+"; ToY = "+toY);

        if(toX>pm.width/2)
            this.x = Math.round(toX)+rnd.nextInt(200);//угол < 90
        else if(toX<pm.width/2)
            this.x = Math.round(toX)-rnd.nextInt(200);//угол > 90
        else
            this.x=Math.round(toX);//угол = 90
       // this.x = rnd.nextInt(900);

        /**Скорость рандомная*/
        this.speed = rnd.nextInt(200) + 15;

        /**Задаем размер*/
        this.widht = 20;
        this.height = 20;

        angle = Math.tan(toX/toY);

        Log.d("MY","Угол = "+angle);
        //angle = getRandomAngle();
        //boneAngle=angle;

        //здесь посложнее математика будет....
        //TODO:вращение изображения зависит от х. Как и угол полёта  - надо высчитвывать
        //Угол полёта тоже
    }

    /**Движение объектов*/
    public void update() {
        if(toX>x){
            x += speed * Math.cos(angle)/10;
        }else if(toX<x){
            x -= speed * Math.cos(angle)/10;
        }

        y += Math.abs(speed * Math.sin(angle));

        Log.d("MY","UPDATED  x="+x+" y="+y);
        //по х  -/+ по делению на 2
    }

    /**Задаем рандомный угол полета*/
    private int getRandomAngle() {
        /*Random rnd = new Random(System.currentTimeMillis());
        return rnd.nextInt(1) * 90 + 90 / 2 + rnd.nextInt(15) + 5;*/
        if(x<800/2)
            return 300;
        else if(x>800/2)
            return 200;
        else
            return 270;
    }

    /**Рисуем*/
    public void onDraw(Canvas c) {
        update();
        c.drawBitmap(bmp, x, y, null);
    }

    /**Проверка на столкновения*/
    public boolean isCollition(float x2, float y2) {
        return x2 > x && x2 < x + widht && y2 > y && y2 < y + height;
    }
}
