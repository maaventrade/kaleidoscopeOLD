package com.alexmochalov.kaleidoscope;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

class Preview implements PreviewCallback, Camera.PictureCallback, Camera.AutoFocusCallback {  
	private Context context;
	
	private int facing = 0;
	private float scale = 1;
	
	private int x = 0, y = 0;
	private int zx = 1, zy = 1;
	private int dx = 0, dy = 0;
	
    private android.hardware.Camera mCamera;  

	//This variable is responsible for getting and setting the camera settings  
    private Parameters parameters;  
    //this variable stores the camera preview size   
    private Size previewSize;  
    
    private SurfaceViewScreen surfaceViewScreen;
    
    private boolean flashMode;
    
    private boolean makePhoto = false;
    
    private Size pictureSize = null;
    private int position = 0;
    
	public Preview(Context context) {
		super();
		this.context = context;
	}
    
	public void startPreview() {
		openCamera();
 		if (mCamera != null)
 			mCamera.startPreview();
	}
    
 	public void setFacing(int facing){
 		this.facing = facing;
 		if (mCamera != null){
 	        mCamera.stopPreview();
 	        mCamera.setPreviewCallback(null);  
            mCamera.release();  
            mCamera = null;
            openCamera();
            mCamera.startPreview();  
 		}
 	}
 	
 	public int getFacing(){
 		return facing;
 	}
 	
 	public float getScale(){
 		return scale;
 	}
 	
 	public void setScale(float scale){
 		if (previewSize != null){
 	    	int left = (int)(previewSize.width-surfaceViewScreen.maskWidth*scale)/2+x;
 	    	int top = (int)(previewSize.height-surfaceViewScreen.maskHeight*scale)/2+y;
 	    	if (left >= 0 && top >= 0){
 	     		this.scale = scale;
 	     		surfaceViewScreen.setScale(scale);
 	    	}
 			
 		} else {
 	     	this.scale = scale;
 	     	surfaceViewScreen.setScale(scale);
 		}
    	x = 0;
    	y = 0;
 	}
 	  
 	private void openCamera(){
 		int numCameras= Camera.getNumberOfCameras();
 		for(int i=0;i<numCameras;i++){
 		    Camera.CameraInfo info = new CameraInfo();
 		    Camera.getCameraInfo(i, info);
 		    if(facing == info.facing){
 	     	   mCamera = Camera.open(facing);
 		    }
 		}
 		
 		if (mCamera == null)
 	 		for(int i=0;i<numCameras;i++){
 	 		    Camera.CameraInfo info = new CameraInfo();
 	 		    Camera.getCameraInfo(i, info);
 	 		    facing = info.facing;
 	 	     	mCamera = Camera.open(facing);
 	 		}
 		
 		if (mCamera == null){
 			Toast.makeText(context, "Phone dosn't have any cameras.", Toast.LENGTH_LONG).show();
 			return;
 		}
        
        try {  
            parameters = mCamera.getParameters();
            previewSize = parameters.getPreviewSize();
            
            if (position >= 0){
				List<Size> sizes = parameters.getSupportedPictureSizes();
				pictureSize = sizes.get(position);
				parameters.setPictureSize(pictureSize.width, pictureSize.height);
				mCamera.setParameters(parameters);
            }
            
            //List<Size> sizes = parameters.getSupportedPreviewSizes();
            // Use only default preview size
            // Dont use: parameters.setPreviewSize(sizes.get(resolutionIndex).width, sizes.get(resolutionIndex).height);           
            surfaceViewScreen.setMaskSize(previewSize);
            
            // Camera doesn't show preview on the screen  
        	mCamera.setPreviewDisplay(null);  
        	//sets the camera callback to be the one defined in this class  
        	mCamera.setPreviewCallback(this); 
        	
        } catch (IOException exception) {  
            mCamera.release();  
            mCamera = null;  
            // TODO: add more exception handling logic here  
        }  
 	}
 
	public void setPreview(int width, int height) {
        Display display = ((WindowManager)context.getSystemService(context.WINDOW_SERVICE)).getDefaultDisplay();

        if(display.getRotation() == Surface.ROTATION_0)
        {
     //       parameters.setPreviewSize(height, width);                           
            mCamera.setDisplayOrientation(90);
        }

        if(display.getRotation() == Surface.ROTATION_90)
        {
           // parameters.setPreviewSize(width, height);                           
        }

        if(display.getRotation() == Surface.ROTATION_180)
        {
           // parameters.setPreviewSize(height, width);               
        }

        if(display.getRotation() == Surface.ROTATION_270)
        {
            //parameters.setPreviewSize(width, height);
            mCamera.setDisplayOrientation(180);
        }
	}
	
    @Override  
    public void onPreviewFrame(byte[] data, Camera camera) {
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
    	YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21,  previewSize.width, previewSize.height, null);
 
    	int left = (int)(previewSize.width-surfaceViewScreen.maskWidth*scale)/2+x;
    	int top = (int)(previewSize.height-surfaceViewScreen.maskHeight*scale)/2+y;
    	int right = (int)(left+surfaceViewScreen.maskWidth*scale);
    	int bottom = (int)(top+surfaceViewScreen.maskHeight*scale);
    	
    	if (left + dx * zx < 0 ||
    		right + dx * zx > previewSize.width) dx = -dx;
    	else {
    		randomZero(); 
        	if (left + dx * zx < 0 ||
            		right + dx * zx > previewSize.width) dx = -dx;
    	}
    		
    	if (top + dy * zy < 0 ||
    		bottom + dy * zy > previewSize.height) dy = -dy;
    	else {
    		randomZero(); 
        	if (top + dy * zy < 0 ||
            		bottom + dy * zy > previewSize.height) dy = -dy;
    	}

    	if (left + dx * zx < 0 ||
        		right + dx * zx > previewSize.width) zx = 0;
    	if (top + dy * zy < 0 ||
        		bottom + dy * zy > previewSize.height) zy = 0;
    	
    	x += dx * zx;
		y += dy * zy;
		
    	yuvImage.compressToJpeg(new Rect(left, top, right, bottom), 90, out);
    	//yuvImage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 90, out);
    	
    	byte[] imageBytes = out.toByteArray();
    	Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

    	surfaceViewScreen.setBitmaps(bitmap);
    }  
      
    private void randomZero() {
		double n = Math.random();
		if (n < 0.5f)
			zx = 1 - zx;
		n = Math.random();
		if (n < 0.5f)
			zy = 1 - zy;
		if (zx == 0 && zy == 0){
			n = Math.random();
			if (n < 0.5f) zx = 1;
			else zy = 1;
		}
		
	}

	public void setSurfaceViewScreen (SurfaceViewScreen surfaceViewScreen){
    	this.surfaceViewScreen = surfaceViewScreen;
    }

	public int getSliding() {
		return Math.max(Math.abs(dx), Math.abs(dy));
	}

	public void setSliding(int i) {
		dx = i;
		dy = i;
	}

	public void close() {
 		if (mCamera != null){
 	        mCamera.stopPreview();
 	        mCamera.setPreviewCallback(null);  
            mCamera.release();  
            mCamera = null;
 		}
	}

	public void setFlash(boolean b) {
		flashMode = b;
		if (b)
			parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
		else
			parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
		mCamera.setParameters(parameters);
	}

	public boolean getFlash() {
		// TODO Auto-generated method stub
		return flashMode;
	}
	
	public String[] getSupportedPictureSizes(){
		if (parameters == null){
			String[] result = new String[1];
			return result;
		}
		
		List<Size> sizes = parameters.getSupportedPictureSizes();
		String[] result = new String[sizes.size()+1];
		
		int i = 1;
		for (Size size: sizes)
			result[i++] = ""+size.width+"x"+size.height; 
		return result;
	}

	public void setPictureSize(int position) {
		this.position = position;
		if (position < 0) pictureSize = null;
		else {
			if (parameters != null){
				List<Size> sizes = parameters.getSupportedPictureSizes();
				pictureSize = sizes.get(position);
				parameters.setPictureSize(pictureSize.width, pictureSize.height);
				mCamera.setParameters(parameters);
			}
		}
    	surfaceViewScreen.setMakePhoto(position != 0);
	}

	public int getPictureSize() {
		return position;
	}

	public void makePhoto() {
		makePhoto = true;
		mCamera.autoFocus(this);
	}

	@SuppressLint("NewApi")
	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		
    	BitmapFactory.Options options = new BitmapFactory.Options();
    	options.inMutable = true;
    	Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
    	
    	int left = (int)(bitmap.getWidth()-surfaceViewScreen.maskWidth*scale)/2;
    	int top = (int)(bitmap.getHeight()-surfaceViewScreen.maskHeight*scale)/2;
    	int right = (int)(left+surfaceViewScreen.maskWidth*scale);
    	int bottom = (int)(top+surfaceViewScreen.maskHeight*scale);
    	
    	Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, left, top, right-left, bottom-top);
    	
		Log.d("", "MAKE");
    	surfaceViewScreen.setBitmapsMakeScreenShot(bitmap, croppedBitmap);

		Log.d("", "OK");
		mCamera.startPreview();		
	}

	@Override
	public void onAutoFocus(boolean success, Camera camera) {
		if (makePhoto && success){
			camera.takePicture(null, null, null, this);
		}
	}

}  
