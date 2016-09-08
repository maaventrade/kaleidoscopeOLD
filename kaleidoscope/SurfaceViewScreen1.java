package com.alexmochalov.kaleidoscope;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

public class SurfaceViewScreen extends SurfaceView implements SurfaceHolder.Callback {
	private Context context;
    private boolean updateFlag = false;

	private DrawThread drawThread;
	private Bitmap mask;
	
	private float scale = 1;
	private float maskWidth = 220f;
	private float maskHeight = 190f;
	
	private int height;
	private int width;
	
	private Point start = new Point();
	
	private Bitmap chips[] = {null, null, null, null, null, null};
	private Rect rects[] = {null, null, null, null, null, null};
	
	public TouchEventCallback touchEventCallback;
	
    public SurfaceViewScreen(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
        getHolder().addCallback(this);
	}

	public SurfaceViewScreen(Context context) {
        super(context);
		this.context = context;
        getHolder().addCallback(this);
    }
    
	interface TouchEventCallback { 
		void callbackCall(); 
	}
	
	@Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
        int height) {
		this.width = width;
		this.height = height;
		
		drawThread.setParams(width, height, maskWidth, maskHeight);
		//this.setVisibility(View.INVISIBLE);
	}

    /**
     * 
     * @param original - bitmap with the original image
     * @param mask - the mask for cropping the original image
     * This method crops a part from the center of the original image. We suggest what the mask is smaller when the original image. 
     * 
     */
	public synchronized void setBitmaps(Bitmap original) {
		updateFlag = true;
       // drawThread.setBitmaps(BitmapFactory.decodeResource(getResources(),R.drawable.image), mask);

		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //orig = original;
        
	    chips[4] = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(),  Config.ARGB_8888);
	    
	    // create Temp image. It is a transparent area with white triangle (from mask)
	    // Size of Temp is equal to the size of Original image
    	Bitmap temp = original.copy(original.getConfig(), true);
    	Canvas canvastemp = new Canvas(temp);
    	Paint transPainter = new Paint();
    	transPainter.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR));
    	// Make Temp transparent
    	canvastemp.drawRect(0, 0, temp.getWidth(), temp.getHeight(), transPainter);    	
    	transPainter.setXfermode(null);     
    	
    	transPainter.setColor(Color.WHITE);
    	transPainter.setStyle(Paint.Style.FILL_AND_STROKE);
    	
    	// Draw mask to the center of Temp bitmap (add white triangle) 
    	canvastemp.drawBitmap(mask, 0 , 0 , transPainter);    	

    	Canvas tempCanvas = new Canvas(chips[4]);

        paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
        // Copy central rectangle of Original image to the first "Chip"   
        tempCanvas.drawBitmap(original, 0, 0, null);
        // and add Temp image. It is a transparent area with white triangle (from mask)   
        tempCanvas.drawBitmap(    temp, 0, 0, paint);
        /*
        
//        tempCanvas.drawBitmap(mask, new Rect(0,0,mask.getWidth(), mask.getHeight()) , new Rect(130,130,mask.getWidth()+130, mask.getHeight()+130), paint);
        paint.setXfermode(null);   
		for (int i = 0; i<= 5; i++)
			rects[i] = new Rect(0, 0, chips[4].getWidth(), chips[4].getHeight());
		
	    chips[1] = flip(chips[4]);
	    chips[5] = rotate(chips[1], 120);
	    
		float sin120 = 0.866f;
		float cos120 = 0.5f;
		
		maskWidth = mask.getWidth();
		maskHeight = mask.getHeight();
		
		//Log.d("","===="+(maskWidth/2f));
		//Log.d("","===="+(int)(maskWidth/2f*cos120));
		rects[5].offset((int)(maskWidth/2f*cos120), 
						(int)(maskWidth/2f*sin120));
	    
	    chips[2] = flip(chips[5]);
		rects[2].offset((int)(maskWidth/2f*cos120), 
						0);
	    
		
	    chips[0] = rotate(chips[2], -120);
	    chips[3] = rotate(chips[5], 120);

		rects[0].offset((int)(maskWidth/2f*cos120), 
						(int)(maskWidth/4f*sin120));
		
		rects[3].offset((int)(maskWidth/2f*cos120), 
						(int)(maskWidth/1.333333f*sin120));
	
		start.x = (int) (width/2 - ((width/2/maskWidth)+2)*maskWidth);
		start.y = (int) (height/2 - ((height/2/maskHeight)+1)*maskHeight);
	*/	
		drawThread.setParams(width, height, maskWidth, maskHeight);
		
		updateFlag = false;	
	}
	
	private void fillMask(){
		mask = Bitmap.createBitmap((int)(maskWidth*scale), (int)(maskHeight*scale), Config.ARGB_8888);
    	//Clear the canvas
    	Canvas canvas = new Canvas(mask);

    	Paint paint = new Paint();
    	
    	paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR));
    	canvas.drawRect(0, 0, maskWidth*scale, maskHeight*scale, paint);    	
    	paint.setXfermode(null);
    	
    	paint.setColor(Color.WHITE);
    	for (float x = 0 ; x < maskWidth*scale ; x++)
        	for (float y = 0; y < maskHeight*scale; y++)
        		if (y < x/0.57735 && x <= maskWidth*scale/2 // ctg(60) 
        		|| y < maskWidth*scale*1.73205 - (x)/0.57735 && x > maskWidth*scale/2
        				) 
        	    	canvas.drawPoint(x, maskHeight*scale-y, paint);
	}
    	
	private Bitmap rotate(Bitmap result, int angle) {
    	Matrix matrix = new Matrix();
    	matrix.preRotate(angle, 0, 0);
        Bitmap dst = Bitmap.createBitmap(result, 0, 0, result.getWidth(), result.getHeight(), matrix, false);
        dst.setDensity(DisplayMetrics.DENSITY_DEFAULT);
        return dst;
    }
    
    private Bitmap flip(Bitmap result) {
    	Matrix m = new Matrix();
        m.preScale(1, -1);
        Bitmap dst = Bitmap.createBitmap(result, 0, 0, result.getWidth(), result.getHeight(), m, false);
        dst.setDensity(DisplayMetrics.DENSITY_DEFAULT);
        return dst;	
    }
	
	/**
	 * 
	 */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    	fillMask();
    	
    	drawThread = new DrawThread(getHolder(), this);
        drawThread.setRunning(true);
        drawThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
        // finish the thread wirking
        drawThread.setRunning(false);
        while (retry) {
            try {
                drawThread.join();
                retry = false;
            } catch (InterruptedException e) {
                // try again ang again
            }
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			touchEventCallback.callbackCall();
			break;
		case MotionEvent.ACTION_MOVE:
	//		if (! drawThread.rectButtonShot.contains(event.getX(), event.getY()))
	//			drawThread.setStateOff();
			break;
		case MotionEvent.ACTION_UP:
	//		if (drawThread.rectButtonShot.contains(event.getX(), event.getY())){
	//			Log.d("", "CALL");
	//			touchEventCallback.callbackCall();
	//		}
	//		else drawThread.setStateOff();
			break; 
		}		
		return true; //processed
    }

 	public void setScale(float scale){
 		this.scale = scale;
 		fillMask();
 		invalidate();
 	}

    private Point getStart(){
  	  return start;
    }

    private Bitmap[] getBitmaps(){
  	  return chips;
    }

    private Rect[] getRects(){
  	  return rects;
    }

    private boolean getUpdateFlag(){
  	  return updateFlag;
    }
    
    
 	
class DrawThread extends Thread{
	private SurfaceViewScreen surfaceViewScreen;
	
    private boolean runFlag = false;
    
    private SurfaceHolder surfaceHolder;

	private Point start = new Point();
	
	private int height;
	private int width;
	private int maskWidth;
	private int maskHeight;
	
    Bitmap chips[] = {null, null, null, null, null, null};
    Rect rects[] = {null, null, null, null, null, null};

	private Bitmap orig;
    
    int N = 0;
	
	//Paint paintButton = new Paint(Paint.ANTI_ALIAS_FLAG);
	//Paint paintProgress = new Paint();
	
    public DrawThread(SurfaceHolder surfaceHolder, SurfaceViewScreen surfaceViewScreen){
        this.surfaceHolder = surfaceHolder;
        this.surfaceViewScreen = surfaceViewScreen;
    }

	public void setParams(int width, int height, float maskWidth, float maskHeight)
	{
		this.width = width;
		this.height = height;
		this.maskWidth = (int)maskWidth;
		this.maskHeight = (int)maskHeight;
	}

	public void setRunning(boolean run) {
        runFlag = run;
    }

	private void read() {
		start = getStart();
		Bitmap[] chips1 = getBitmaps();//chips[i].copy(chips[i].getConfig(), true);
		Rect[] rects1 = getRects();//new Rect(chipR[i]);
    	
		if (chips1[4] != null)
			chips[4] = chips1[4].copy(chips1[4].getConfig(), true);
/*
    	if (chips1[0] == null ||
    			chips1[1] == null ||
    			chips1[2] == null ||
    			chips1[3] == null ||
    			chips1[4] == null ||
    			chips1[5] == null 
    			) 
    		return;
		
		for (int i = 0; i <= 5; i++){
			chips[i] = chips1[i].copy(chips1[i].getConfig(), true);
			rects[i] = new Rect(rects1[i]);
		}
		*/
    }

    @Override
    public void run() {
        Canvas canvas;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
       // Canvas mCanvas = new Canvas(result);
        
        while (runFlag) {
        	if (getUpdateFlag()) continue;
        	
            canvas = null;
            try {
                // get the Canvas and start drawing
                canvas = surfaceHolder.lockCanvas(null);
                synchronized (surfaceHolder) {
                    read();
                	if (canvas != null){
                		draw(canvas, paint);
                	}
                }
            } 
            finally {
                if (canvas != null) {
                    // drawing is finished
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }
    
    private void draw(Canvas canvas, Paint paint){
    	if (chips[4] != null)
    		canvas.drawBitmap(chips[4], 300, 300, paint);
		/*
    	if (chips[0] == null ||
    			chips[1] == null ||
    			chips[2] == null ||
    			chips[3] == null ||
    			chips[4] == null ||
    			chips[5] == null 
    			) 
    		return;
    	*/
    	/*
		for (int y = startY; y <= height; y = y + maskHeight){
			int i = 0;
			for (int x = startX; x <= width; x = x + maskWidth/2-4){
				N++;
				canvas.drawBitmap(chips1[i], chipR1[i], new Rect(x, y, x+maskWidth, y+maskHeight), paint);
				i++;
				if (i == 6){
					i = 0;
					//x = x - 4;
				}
			}
			y = y + maskHeight;
			i = 0;
			for (int x = startX; x <= width; x = x + maskWidth/2-4){
				if (i < 3)
					canvas.drawBitmap(chips1[i+3], chipR1[i+3], new Rect(x, y, x+maskWidth, y+maskHeight), paint);
				else
					canvas.drawBitmap(chips1[i-3], chipR1[i-3], new Rect(x, y, x+maskWidth, y+maskHeight), paint);
				N++;
				
				i++;
				if (i == 6){
					i = 0;
					//x = x - 4;
				}
			}
		}
	 */
    	
    	
    	/*
    	int ccc = 0;
		for (int y = 200; y <= maskHeight*2; y = y + maskHeight){
			int i = 0;
			for (int x = 100; x <= maskWidth*1.5; x = x + maskWidth/2){
				canvas.drawBitmap(chips[i], rects[i], new Rect(x, y, x+maskWidth, y+maskHeight), paint);
				if (N == 0){
					ccc++;
				}
				i++;
				if (i == 6){
					i = 0;
					//x = x - 4;
				}
			}
			y = y + maskHeight;
			i = 0;
			for (int x = 100; x <= maskWidth*1.5; x = x + maskWidth/2){
				if (i < 3)
					canvas.drawBitmap(chips[i+3], rects[i+3], new Rect(x, y, x+maskWidth, y+maskHeight), paint);
				else
					canvas.drawBitmap(chips[i-3], rects[i-3], new Rect(x, y, x+maskWidth, y+maskHeight), paint);
				if (N == 0){
					ccc++;
				}
				
				i++;
				if (i == 6){
					i = 0;
					//x = x - 4;
				}
			}
		}
		if (N <= 100){
			for (int i = 0; i<=5; i++)
				Log.d("", "rects "+rects[i]);
		}
		
		N++;
		*/
    }
    
	private Rect getRect(Bitmap bitmap) {
		return new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
	}

}

}
