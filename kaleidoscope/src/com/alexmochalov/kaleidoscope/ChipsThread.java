package com.alexmochalov.kaleidoscope;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;

public class ChipsThread extends Thread{
	private boolean runFlag = false;
    private SurfaceHolder surfaceHolder;

    private ArrayList<Sprite> objects = new ArrayList<Sprite>();
    private Scene scene;
    
    private Bitmap bitmap;
    private Canvas canvas;
    private Paint paint;
    
    private DrawThread drawThread;
    
//	private int height;
//	private int width;
    
	private Context context;
	
    public ChipsThread(Context context, SurfaceHolder surfaceHolder, DrawThread drawThread){
    	this.context = context;
        this.surfaceHolder = surfaceHolder;
        this.drawThread = drawThread;
        
        bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        paint = new Paint();
        
    	scene = new Scene(200);
        
    }


	public void setRunning(boolean run) {
        runFlag = run;
        if (run){
        	reset();
        } else {
        	drawThread.setGlassBitmap(null);
        }
    }

    @Override
    public void run() {
        Canvas canvas;
        
        while (runFlag) {
    		  //synchronized (surfaceHolder) {
    			   drawSomething();
    		 //  }
    	   try {
    	    sleep(10);
    	   } catch (InterruptedException e) {
    	    // TODO Auto-generated catch block
    	    e.printStackTrace();
    	   }
        }
    }


	private synchronized void drawSomething() {
		if (!runFlag) return;
		
		for (Sprite o: objects){
			 o.step(objects, scene);
		 }
	 
		canvas.drawColor(0, PorterDuff.Mode.CLEAR);
//		canvas.drawColor(Color.BLACK);
	 
		paint.setColor(Color.WHITE);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
	 
		scene.paint(canvas, paint);
	 
		for (Sprite o: objects)
			o.paint(canvas,  paint);
	 		
		if (drawThread != null)
			drawThread.setGlassBitmap(bitmap);
	 }
    
	public synchronized void  reset() {
		//runFlag = false;
		objects.clear();
		int maxCount = 6;
		int index = 0;
		for (int i = 0; i <= maxCount; i++){
			
			int set = (int)(Math.random()*2+1);
			
			if (set == 0)
				index = Math.min((int)(Math.random()*6),6);
			else if (set == 1)
				index = Math.min((int)(Math.random()*13),13);
			else if (set == 2)
				index = Math.min((int)(Math.random()*6+7),13);
			
			
			int size = (int) (Math.random()*40+10);
			switch(index){
			case 0:
				objects.add(new Sprite(context, size, R.drawable.chip0, objects,scene));
				break;
			case 1:
				objects.add(new Sprite(context, size, R.drawable.chip1, objects,scene));
				break;
			case 2:
				objects.add(new Sprite(context, size, R.drawable.chip2, objects,scene));
				break;
			case 3:
				objects.add(new Sprite(context, size, R.drawable.chip3, objects,scene));
				break;
			case 4:
				objects.add(new Sprite(context, size, R.drawable.chip4, objects,scene));
				break;
			case 5:
				objects.add(new Sprite(context, size, R.drawable.chip5, objects,scene));
				break;
			case 6:
				objects.add(new Sprite(context, size, R.drawable.chip6, objects,scene));
				break;
			case 7:
				objects.add(new Sprite(context, size, R.drawable.chip7, objects,scene));
				break;
			case 8:
				objects.add(new Sprite(context, size, R.drawable.chip8, objects,scene));
				break;
			case 9:
				objects.add(new Sprite(context, size, R.drawable.chip9, objects,scene));
				break;
			case 10:
				objects.add(new Sprite(context, size, R.drawable.chip10, objects,scene));
				break;
			case 11:
				objects.add(new Sprite(context, size, R.drawable.chip11, objects,scene));
				break;
			case 12:
				objects.add(new Sprite(context, size, R.drawable.chip12, objects,scene));
				break;
			case 13:
				objects.add(new Sprite(context, size, R.drawable.chip13, objects,scene));
				break;
			}
			
		}
		scene.reset();
		//runFlag = true;
	}


	public void setDrawThread(DrawThread drawThread) {
		this.drawThread = drawThread;
	}

}
