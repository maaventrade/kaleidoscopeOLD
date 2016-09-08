package com.alexmochalov.kaleidoscope;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.hardware.Camera.Size;
import android.os.Environment;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class SurfaceViewScreen extends SurfaceView implements SurfaceHolder.Callback {
	// Mask for a cutting triangles from the original images
	private Bitmap mask = null;
	// Size of the Mask
	public float maskWidth = 221f;
	public float maskHeight = maskWidth/1.157894737f;  //190f;

	private Context context;
	// The ghread to draw in the screen
	private DrawThread drawThread;

	private ChipsThread chipsThread;
	
	// Triangles can be scaled
	private float scale = 1;
	
	private float prevX;
	private float prevY;

	private Point downCoords = new Point();
	
	public TouchEventCallback touchEventCallback;
	
	
	private Handler handler = new Handler(); 
	private int slideY = 0; // 
	private int slideX = 0; // 
	private long eventTime;

	private Rect rectCameraDst;
	
	private boolean showIconPhoto;
	private boolean makePhoto = false;
	private boolean showShadow = true;
	
	private boolean glasses = false;

	public boolean getGlasses()
	{
		return glasses;
	}
	
	private void init(){
        getHolder().addCallback(this);
	}
	
    public SurfaceViewScreen(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init();
	}

	public SurfaceViewScreen(Context context) {
        super(context);
		this.context = context;
		init();
    }
    
	interface TouchEventCallback { 
		void callbackCall(); 
		void callbackPhoto(); 
	}
	
	@Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
        int height) {
		
		rectCameraDst = new Rect(width-width/6, height-width/6, width-10, height-10); 
		drawThread.setParams(width, height, rectCameraDst);
		//chipsThread.setParams(width, height, rectCameraDst);
	}

	public void setBitmaps(Bitmap original) {
		if (drawThread != null)
			if (drawThread.test)
				drawThread.setOriginalBitmap(
		    		Bitmap.createBitmap(
		    				BitmapFactory.decodeResource(getResources(),R.drawable.image), 0,0, mask.getWidth(), mask.getHeight()));
			else		
				drawThread.setOriginalBitmap(original);
	}
	
	private static final float CTG_60 = 0.57735026919f;
	private static final float TG_60 = 1.73205080757f;
	
	private void fillMask(){
/*		
		mask = Bitmap.createBitmap((int)(maskWidth*scale), (int)(maskHeight*scale), Config.ARGB_8888);
    	//Clear the canvas
    	Canvas canvas = new Canvas(mask);

    	Paint paint = new Paint();
    	
        final int color = 0xff424242;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
*/

		mask = Bitmap.createBitmap((int)(maskWidth*scale), (int)(maskHeight*scale), Config.ARGB_8888);
    	//Clear the canvas
    	Canvas canvas = new Canvas(mask);

    	Paint paint = new Paint();
    	
    	paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR));
    	canvas.drawRect(0, 0, maskWidth*scale, maskHeight*scale, paint);    	
    	paint.setXfermode(null);
		
    	// Add white triangle.
    	for (float x = 0 ; x < maskWidth*scale ; x++)
        	for (float y = 0; y < maskHeight*scale; y++)
        		// If the point locates in the triangle area
        		if (y <= x/CTG_60 && x <= maskWidth*scale/2  
        		|| y < maskWidth*scale*TG_60 - (x)/CTG_60 && x > maskWidth*scale/2
        		// "+ 2" for overlapping triangles  
        				) 
        			canvas.drawPoint(x, maskHeight*scale-y, paint);
    	
	}
    	
	/**
	 * 
	 */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    	fillMask();
    	
    	drawThread = new DrawThread(context, getHolder(), this);
        drawThread.setRunning(true);
		drawThread.setMask(mask);
		drawThread.setShowIconPhoto(showIconPhoto);
		drawThread.setShowShadow(showShadow);
        drawThread.start();
        
        if (chipsThread != null)
        	chipsThread.setDrawThread(drawThread);
		//drawThread.setInformation("Start...");
        
    }

    public void resetThread(){
        if (chipsThread != null)
        	chipsThread.reset();
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
        
        // finish the thread wirking
        if (chipsThread != null){
            chipsThread.setRunning(false);
            while (retry) {
                try {
                	chipsThread.join();
                    retry = false;
                } catch (InterruptedException e) {
                    // try again ang again
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
	@Override
    public boolean onTouchEvent(MotionEvent event) {

		float x = event.getX();
		float y = event.getY();
	
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (rectCameraDst.contains((int)x, (int)y)){
				if (makePhoto)
					if (touchEventCallback != null){
						drawThread.setInformation("Saving...");
						touchEventCallback.callbackPhoto();
					}	
					else;
				else {
					drawThread.setInformation("Saving...");
					makeScreenShot(null);
				}
				break; 
			}
			
			downCoords.x = (int)x;
			downCoords.y = (int)y;
			
			eventTime = event.getEventTime();
			break;
		case MotionEvent.ACTION_MOVE:
			drawThread.move(x-prevX, y-prevY);
			break;
		case MotionEvent.ACTION_UP:
			if (downCoords.x == (int)x && downCoords.y == (int)y)
				if (touchEventCallback != null)
					touchEventCallback.callbackCall();
			
        	long eventTime1 = event.getEventTime();
		    
            if (downCoords.y > y) slideY = -Math.round((downCoords.y - y)/(eventTime1-eventTime)*50);
            else if (downCoords.y < y) slideY = -Math.round((downCoords.y - y)/(eventTime1-eventTime)*50);
            else downCoords.y = 0;
		    
            if (downCoords.x > x) slideX = -Math.round((downCoords.x - x)/(eventTime1-eventTime)*50);
            else if (downCoords.x < x) slideX = -Math.round((downCoords.x - x)/(eventTime1-eventTime)*50);
            else downCoords.x = 0;
            
            if (slideY !=0 || slideX != 0){
            	handler.postDelayed(updateTimeTask, 10);
            }	
			
			break; 
		}		
		prevX = x;
		prevY = y;
		return true; //super.onTouchEvent(event);
    }

	@SuppressLint("SimpleDateFormat")
	private void makeScreenShot(Bitmap bmp) {
		File pix = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		if (bmp == null)
			bmp = drawThread.getBitmap(); 
	    
	    Calendar c = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    String strDate = sdf.format(c.getTime());	    
	    
	    String filename = pix+"/"+strDate+".png";
	    
	    FileOutputStream out = null;
	    try {
	        out = new FileOutputStream(filename);
	        bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
	        // PNG is a lossless format, the compression factor (100) is ignored
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            if (out != null) {
	                out.close();
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	            return;
	        }
	    }
	    
		Toast.makeText(context, "Screen saved to "+filename, Toast.LENGTH_LONG).show();
	}

	private void slide() {
		if (slideY > 0) slideY--;
		else if (slideY < 0) slideY++;
		
		if (slideX > 0) slideX--;
		else if (slideX < 0) slideX++;
		drawThread.move(slideX, slideY);
	}
	
    private Runnable updateTimeTask = new Runnable() { 
		   public void run() { 
			   slide();
			   handler.postDelayed(this, 10);
		       if (slideY == 0 && slideX == 0)
			   	   handler.removeCallbacks(updateTimeTask); 
		   } 
		};        
    
 	public void setScale(float scale){
 		this.scale = scale;
 		fillMask();
 		if (drawThread != null)
 			drawThread.setMask(mask);
 		invalidate();
 	}

	public void setMaskSize(Size previewSize) {
		// maskWidth for 960px is 220
		// 960/220 = 4.363636364
		
		//maskWidth = previewSize.width/4.363636364f;
		//maskHeight = maskWidth/1.157894737f; 
	}

	public boolean getShowIconPhoto() {
		return showIconPhoto;
	}

	public void setShowIconPhoto(boolean param) {
		showIconPhoto = param;
		if (drawThread != null)
			drawThread.setShowIconPhoto(param);
	}
	
	public void setShowShadow(boolean param) {
		showShadow = param;
		if (drawThread != null)
			drawThread.setShowShadow(param);
	}
	
	public void setMakePhoto(boolean  param){
		makePhoto = param;
	}

	public void setBitmapsMakeScreenShot(Bitmap bitmap, Bitmap bitmapMask) {
		setBitmaps(bitmapMask);
		drawThread.repaint(new Canvas(bitmap));
		makeScreenShot(bitmap);
		drawThread.setInformation(null);
	}

	public boolean getShowShadow() {
		return showShadow;
	}

	public boolean setGlasses() {
		return glasses;
	}

	public void setGlasses(boolean isChecked) {
		glasses = isChecked;
		
		if (glasses){
	    	chipsThread = new ChipsThread(context, getHolder(), drawThread);
	    	chipsThread.setRunning(true);
	    	chipsThread.start();
		} else
			if (chipsThread != null)
				chipsThread.setRunning(false);
		
	}
	
}
