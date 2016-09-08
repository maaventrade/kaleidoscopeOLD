package com.alexmochalov.kaleidoscope;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;

public class DrawThread extends Thread{
	//Bitmap ttt;
	
	public boolean test = false;
	
	private boolean runFlag = false;
    private SurfaceHolder surfaceHolder;

	private int height;
	private int width;
	private int maskWidth;
	private int maskHeight;
	
	private Bitmap mask;
	private Bitmap bitmapShadow;
	private float deltaX = 0;
	private float deltaY = 0;
	
    Bitmap chip = null;
    Rect rectChip = null;

	private Bitmap bitmapCamera;
	private Rect rectCameraSrc;
	private Rect rectCameraDst;
	
	private Paint paint;
	private int leftChip = 0;
	private Canvas canvasChip;

	private boolean ShowIconPhoto;
	private boolean showShadow = true;
    
	private Context context;
	
	private ProgressDialog dialog = null;
	
    int N = 0;
    
    private Bitmap background = null;
    private Canvas canvasBackground;
	private Bitmap original = null;
	
	Paint paintButton = new Paint(Paint.ANTI_ALIAS_FLAG);

	private boolean glasses;
	
    public DrawThread(Context context, SurfaceHolder surfaceHolder, SurfaceViewScreen surfaceViewScreen){
    	this.context = context;
        this.surfaceHolder = surfaceHolder;
        bitmapCamera = BitmapFactory.decodeResource(context.getResources(), R.drawable.btn_camera);
        rectCameraSrc = new Rect(0,0, bitmapCamera.getWidth(), bitmapCamera.getHeight());
        paintButton.setAlpha(200);
        
        //ttt = 	    		Bitmap.createBitmap(
		//		BitmapFactory.decodeResource(context.getResources(),R.drawable.image), 0,0, 300, 300);

    }

	public boolean getGlasses()
	{
		return glasses;
	}

	public synchronized void  move(float dX, float dY)
	{
		deltaX += dX;
		deltaY += dY;
		
		if (deltaY > -maskHeight){
			deltaY = -maskHeight*3;
		}
		else 
			if (deltaY < -maskHeight*3){
			deltaY = -maskHeight;
	
		} 
		
		if (deltaX >  -maskWidth/2){
			deltaX = -maskWidth*3.5f;
		} else
			if (deltaX < -maskWidth*3.5f){
				deltaX = -maskWidth/2;
		}
		
	}

	public void setMask(Bitmap mask)
	{
		this.mask = mask;

		maskWidth = mask.getWidth();
		maskHeight = mask.getHeight();

        chip = Bitmap.createBitmap(maskWidth, maskHeight,  Config.ARGB_8888);
        canvasChip = new Canvas(chip);
		//startX = width/2 - ((width/2/maskWidth)+2)*maskWidth;
		//startY = 0;
		deltaX = -maskWidth/2;
		deltaY = -maskHeight;
		//deltaX = 0;
		
    	//background = Bitmap.createBitmap(maskWidth, maskHeight, Config.ARGB_8888 );
		//canvasBackground = new Canvas(background);
		
	}

	public void setParams(int width, int height, Rect rectCameraDst)
	{
		this.width = width;
		this.height = height;
        this.rectCameraDst = rectCameraDst; 
        
        //bitmapShadow = BitmapFactory.decodeResource(context.getResources(), R.drawable.shadow);
        
        bitmapShadow = makeShadow(width, height);
	}

	private Bitmap makeShadow(int width, int height){
        Bitmap bitmapShadow = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas tempCanvas = new Canvas(bitmapShadow);
        
        tempCanvas.drawColor(Color.BLACK);
        
        Paint p = new Paint();
        
        int[] colors = {0xFF000000, 0x00000000}; 
        float[] stops = {0.4f, 1f};
        
        RadialGradient gradient = new android.graphics.RadialGradient(
                width/2, height/2,
                width, colors , stops ,
                android.graphics.Shader.TileMode.CLAMP);

        p = new Paint();
        p.setShader(gradient);
        p.setColor(0xFF000000);
        p.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
        tempCanvas.drawCircle(width/2, height/2,
        		width, p);  
		return bitmapShadow;
	}
	

	public int adjustAlpha(int color, float factor) {
	    int alpha = Math.round(Color.alpha(color) * factor);
	    //int alpha = Color.alpha(color);
		//if (alpha > 0)
	    //	alpha = alpha-2;
	    Log.d("MY", "alpha "+alpha);
	    int red = Color.red(color);
	    int green = Color.green(color);
	    int blue = Color.blue(color);
	    
	    
	    return Color.argb(alpha, red, green, blue);
	}	
	
	
	private Bitmap glass = null;

	public synchronized void setGlassBitmap(Bitmap glass) {
		this.glass = glass;
		if (glass == null)
			glasses = false;
		else glasses = true;
	}
	
	public synchronized void setOriginalBitmap(Bitmap original) {
		this.original = original;
	}
	
    /**
     * 
     * @param original - bitmap with the original image
     * @param mask - the mask for cropping the original image
     * This method crops a part from the center of the original image. We suggest what the mask is smaller when the original image. 
     * 
     */
	public synchronized void bitmapsToChips() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	    
        if (glass != null){
        	background = Bitmap.createBitmap(maskWidth, maskHeight, Config.ARGB_8888 );
			canvasBackground = new Canvas(background);
			
			if (original != null){
				canvasBackground.drawBitmap(original, 0, 0, null);
			}
			
			paint.setColor(Color.DKGRAY);
			paint.setAlpha(220);
			
			canvasBackground.drawRect(0,0,maskWidth, maskHeight, paint);
			paint.setAlpha(255);

        	int n = (int) (maskHeight/1.5f);
        	canvasBackground.drawBitmap(glass, 
					   new Rect(0,
								  0, 
								  glass.getWidth(), 
								  glass.getHeight()),											  
					   new Rect(maskWidth/2-n,
					  maskHeight/2-n, 
					  maskWidth/2+n, 
					  maskHeight/2+n),
					  paint);
											  
		} else if (original == null) return;
			else background = original;
			
        
        canvasChip.drawARGB(0, 0, 0, 0);
        
        rectChip = new Rect(0, 0, chip.getWidth(), chip.getHeight());
        
        final Paint paint = new Paint();
        
        canvasChip.drawBitmap(mask, rectChip, rectChip, null);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvasChip.drawBitmap(background, rectChip, rectChip, paint);

        
		//0 1 2
		//3 4 5
        
	}

	public void setRunning(boolean run) {
        runFlag = run;
    }

    @Override
    public void run() {
        Canvas canvas;
        
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.RED);
		paint.setTextSize(32);

        new Paint(Paint.ANTI_ALIAS_FLAG);
		
        while (runFlag) {
            canvas = null;
            try {
        		//setBitmaps(ttt);
                // get the Canvas and start drawing
                canvas = surfaceHolder.lockCanvas(null);
                synchronized (surfaceHolder) {
                	if (canvas != null){
                		if (test)
                			drawTest(canvas, width, height, false);
                		else draw(canvas, width, height, false);
                	}
                }
            } 
            finally {
                if (canvas != null) {
                    // drawing is finished
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
     	   //try {
       	   // sleep(10);
       	   //} catch (InterruptedException e) {
       	    // TODO Auto-generated catch block
       	   // e.printStackTrace();
       	   //}
        }
    }
    
    private synchronized void draw(Canvas canvas, int width, int height, boolean hideButton){
    	//if (glass != null){
    	//	canvas.drawBitmap(glass,10, 10, paint);
    	//}
    	bitmapsToChips();
		
    	if (chip == null){
			//canvas.drawColor(Color.YELLOW);
			
			return;
		};
		//if(background != null)
		//	canvas.drawBitmap(background, 10, 10,null);
		
    	//int pixel = chip.getPixel(maskWidth/2, maskHeight/2);
        //canvas.drawColor(pixel);
        
		int dx = 0;
		for (int y = (int)(deltaY); y <= height; y = y + maskHeight){
			for (int x = (int)(deltaX) + dx; x <= width; x = (int)(x + maskWidth*3f)-1){
				canvas.save(Canvas.MATRIX_SAVE_FLAG);
		
				canvas.drawBitmap(chip, rectChip, new Rect(x, y, x+maskWidth, y+maskHeight), paint);

				canvas.rotate(-60, x+maskWidth, y+maskHeight);
				canvas.scale(-1f, 1f, x+maskWidth, y+maskHeight);
				
				canvas.drawBitmap(chip, rectChip, new Rect(x, y, x+maskWidth, y+maskHeight), paint);
				
				canvas.scale(-1f, 1f, x+maskWidth, y+maskHeight);
				canvas.rotate(-180, x+maskWidth, y+maskHeight);
				
				canvas.drawBitmap(chip, rectChip, new Rect(x, y, x+maskWidth, y+maskHeight), paint);
				
				canvas.scale(-1f, 1f, x+maskWidth, y+maskHeight);
				canvas.rotate(-60, x+maskWidth, y+maskHeight);
				canvas.drawBitmap(chip, rectChip, new Rect(x, y, x+maskWidth, y+maskHeight), paint);
				
				canvas.scale(-1f, 1f, x+maskWidth, y+maskHeight);
				canvas.rotate(60, x+maskWidth, y+maskHeight);
				canvas.drawBitmap(chip, rectChip, new Rect(x, y, x+maskWidth, y+maskHeight), paint);
				
				canvas.scale(1f, -1f, x+maskWidth, y+maskHeight);
				canvas.drawBitmap(chip, rectChip, new Rect(x, y, x+maskWidth, y+maskHeight), paint);
						
				canvas.restore(); 
			}
			if (dx == 0)
				dx = (int)(dx + maskWidth*1.5f);
			else	
				dx = 0;
		}
		
		if (showShadow && bitmapShadow != null)
			canvas.drawBitmap(bitmapShadow,0, 0, null);
		
		if (! hideButton){
			if (ShowIconPhoto)
				canvas.drawBitmap(bitmapCamera, rectCameraSrc, rectCameraDst, paintButton);
		}
		//paint.setColor(Color.WHITE);
		//canvas.drawText(""+maskWidth+"  "+maskHeight,15,15, paint);
    }

    private synchronized void drawTest(Canvas canvas, int width, int height, boolean hideButton){
    	if (chip == null) return;

        //canvas.drawRGB(0, 0, 255);
    	
    	int x = 10;
    	int y = 10;
		canvas.drawBitmap(chip, rectChip, new Rect(x, y, x+maskWidth, y+maskHeight), paint);

    }
    
	private synchronized void draw1(Canvas canvas, int width, int height){
    	if (chip == null) return;

		int dx = 0;
		for (int y = (int)(deltaY); y <= height; y = y + maskHeight){
			for (int x = (int)(deltaX) + dx; x <= width; x = (int)(x + maskWidth*3f)){
				canvas.save(Canvas.MATRIX_SAVE_FLAG);
		
				canvas.drawBitmap(chip, rectChip, new Rect(x, y, x+maskWidth, y+maskHeight), paint);

				canvas.rotate(-60, x+maskWidth, y+maskHeight);
				canvas.scale(-1f, 1f, x+maskWidth, y+maskHeight);
				
				canvas.drawBitmap(chip, rectChip, new Rect(x, y, x+maskWidth, y+maskHeight), paint);
				
				canvas.scale(-1f, 1f, x+maskWidth, y+maskHeight);
				canvas.rotate(-180, x+maskWidth, y+maskHeight);
				
				canvas.drawBitmap(chip, rectChip, new Rect(x, y, x+maskWidth, y+maskHeight), paint);
				
				canvas.scale(-1f, 1f, x+maskWidth, y+maskHeight);
				canvas.rotate(-60, x+maskWidth, y+maskHeight);
				canvas.drawBitmap(chip, rectChip, new Rect(x, y, x+maskWidth, y+maskHeight), paint);
				
				canvas.scale(-1f, 1f, x+maskWidth, y+maskHeight);
				canvas.rotate(60, x+maskWidth, y+maskHeight);
				canvas.drawBitmap(chip, rectChip, new Rect(x, y, x+maskWidth, y+maskHeight), paint);
				
				canvas.scale(1f, -1f, x+maskWidth, y+maskHeight);
				canvas.drawBitmap(chip, rectChip, new Rect(x, y, x+maskWidth, y+maskHeight), paint);
						
				canvas.restore(); 
			}
			if (dx == 0)
				dx = (int)(dx + maskWidth*1.5f-2);
			else	
				dx = 0;
		}
    	
		if (showShadow){
			float radius = Math.min(width, height);
	        Paint p = new Paint();
	        
	        int[] colors = {0x00000000, 0xFF000000}; 
	        float[] stops = {0.4f, 1f};
	        
	        RadialGradient gradient = new android.graphics.RadialGradient(
	                width/2, height/2,
	                radius, colors , stops ,
	                android.graphics.Shader.TileMode.CLAMP);

	        p.setShader(gradient);
	        p.setColor(0xff000000);
	        
	        canvas.drawCircle(width/2, height/2,
	        		radius, p);
		}
    }
	
	
	public void setInformation(String information) {
		if (information != null){
			dialog = new ProgressDialog(context);			
		    dialog.setMessage(information);
		    dialog.setIndeterminate(true);
		    dialog.setCancelable(true);
		    dialog.show();		
		} else if (dialog != null){
			dialog.dismiss();
			dialog = null;
		}
	}
	
	public Bitmap getBitmap() {
		Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		draw(new Canvas(bmp), width, height, true);
		return bmp;
	}

	public void setShowIconPhoto(boolean param) {
		ShowIconPhoto = param;
	}

	public void setShowShadow(boolean param) {
		showShadow = param;
	}

	public void repaint(Canvas canvas) {
		draw1(canvas, canvas.getWidth(), canvas.getHeight());
	}

}
