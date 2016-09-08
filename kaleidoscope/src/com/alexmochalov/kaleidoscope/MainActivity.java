package com.alexmochalov.kaleidoscope;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
//import android.support.v7.app.ActionBarActivity;



import android.widget.Toast;

import com.alexmochalov.kaleidoscope.SurfaceViewScreen.TouchEventCallback;

/**
 * 
 * @author Alexey Mochalov
 * Application Kaleidoscope Camera gets picture from the phone camera
 * and tramslates it to the kaleidoscope picture.   
 * 
 * 2015
 *
 */
public class MainActivity extends Activity  implements SensorEventListener
{
	private SensorManager sensorManager; 
	double ax,ay,az; // these are the acceleration in x,y and z axis
	
	// Variables for the preferences saving
	private SharedPreferences prefs;
	private static final String FACING = "FACING";
	private static final String SCALE = "SCALE";
	private static final String SLIDING = "SLIDING";
	private static final String SHOWICONPHOTO = "SHOWICONPHOTO";
	private static final String SHOWSHADOW = "SHOWSHADOW";
	private static final String RESOLUTION = "RESOLUTION";
	private static final String GLASSES = "GLASSES";

	// This object contains camera object 
    private Preview preview;
	// This object is a SurfaceView
    private SurfaceViewScreen surfaceViewScreen;

    // Reference to the application context
    private Context context;
	
    @SuppressLint("NewApi")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				 WindowManager.LayoutParams.FLAG_FULLSCREEN);
		if (android.os.Build.VERSION.SDK_INT >= 11)
			getActionBar().hide();
		// else getSupportActionBar().hide();
		
		context = this;
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		surfaceViewScreen = (SurfaceViewScreen)findViewById(R.id.surfaceViewScreen);
		// Open settings dialog on touch on the screen
		surfaceViewScreen.touchEventCallback = new TouchEventCallback(){
			@Override
			public void callbackCall() {
				DialogSettings dialog = new DialogSettings(context, preview, surfaceViewScreen); 
				dialog.show();
			}

			@Override
			public void callbackPhoto() {
				preview.makePhoto();
			}
		};
		surfaceViewScreen.setShowIconPhoto(prefs.getBoolean(SHOWICONPHOTO, true));
		surfaceViewScreen.setShowShadow(prefs.getBoolean(SHOWSHADOW, true));
		surfaceViewScreen.setGlasses(prefs.getBoolean(GLASSES, false));
		
		preview = new Preview(context);
		preview.setSurfaceViewScreen(surfaceViewScreen);
		preview.setFacing(prefs.getInt(FACING, CameraInfo.CAMERA_FACING_BACK));
		//preview.setScale(prefs.getFloat(SCALE, 1));
		preview.setScale(1);
		
		preview.setSliding(prefs.getInt(SLIDING, 0));

		preview.setPictureSize(prefs.getInt("RESOLUTION", -1));
		
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE); 
		sensorManager.registerListener(this, 
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 
				SensorManager.SENSOR_DELAY_NORMAL); //SENSOR_DELAY_GAME 
		
//		preview.setPictureSize(-1);
		
    }

    /*
    static String CAMERA_IMAGE_BUCKET_ID;
    public static List<String> getCameraImages(Context context) {
    	
    	final String CAMERA_IMAGE_BUCKET_NAME =
    	        Environment.getExternalStorageDirectory().toString()
    	        + "/DCIM/Camera";
    	final String CAMERA_IMAGE_BUCKET_ID =  String.valueOf(CAMERA_IMAGE_BUCKET_NAME.toLowerCase().hashCode());
    	
        final String[] projection = { MediaStore.Images.Media.DATA };
        final String selection = MediaStore.Images.Media.BUCKET_ID + " = ?";
        final String[] selectionArgs = { CAMERA_IMAGE_BUCKET_ID };
        final Cursor cursor = context.getContentResolver().query(Images.Media.EXTERNAL_CONTENT_URI, 
                projection, 
                selection, 
                selectionArgs, 
                null);
        ArrayList<String> result = new ArrayList<String>(cursor.getCount());
        if (cursor.moveToFirst()) {
            final int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            do {
                final String data = cursor.getString(dataColumn);
                result.add(data);
                Log.d("MY",  data);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }
   */ 
    
	@Override
	protected void onPause() {
		super.onPause();
		// save preferences
		Editor editor = prefs.edit();
		editor.putInt(FACING, preview.getFacing());
		editor.putFloat(SCALE, preview.getScale());
		editor.putInt(SLIDING, preview.getSliding());
		editor.putBoolean(SHOWICONPHOTO, surfaceViewScreen.getShowIconPhoto());
		editor.putBoolean(SHOWSHADOW, surfaceViewScreen.getShowShadow());
		editor.putInt(RESOLUTION, preview.getPictureSize());
		editor.putBoolean(GLASSES, surfaceViewScreen.getGlasses());
		
		editor.apply();
		// Stop camera preview
		preview.close();
		this.sensorManager.unregisterListener(this); 
	}
	
	@Override
	public void onResume()
	{
		preview.startPreview();
	    super.onResume();
	}	
	
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
       if ( keyCode == KeyEvent.KEYCODE_MENU ) {
    	   // Instead of opening options menu open Dialog 
    	   DialogSettings dialog = new DialogSettings(context, preview, surfaceViewScreen); 
    	   dialog.show();
    	   return true;
       }
       return super.onKeyDown(keyCode, event);
    }
    
	double ax0,ay0; // these are the acceleration in x,y and z axis
	
	double last_x,last_y,last_z;
	long lastUpdate;
	private static final int SHAKE_THRESHOLD = 400;
	
	@Override
	public void onSensorChanged(SensorEvent event)
	{
		if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){ 
			ax=-event.values[0]; 
			ay=event.values[1]; 
			az=event.values[2];
			
			Var.gx = ax-ax0;
			Var.gy = ay-ay0;
			
			ax0 = ax;
			ay0 = ay;
			
			long curTime = System.currentTimeMillis();
		    // only allow one update every 100ms.
		    if ((curTime - lastUpdate) > 100) {
		      long diffTime = (curTime - lastUpdate);
		      lastUpdate = curTime;

		      double speed = Math.abs(ax+ay+az - last_x - last_y - last_z) / diffTime * 10000;

		      if (speed > SHAKE_THRESHOLD) {
		    	  if (surfaceViewScreen != null)
		    		 surfaceViewScreen.resetThread();
		    	  Log.d("sensor", "shake detected w/ speed: " + speed);
		    	  Toast.makeText(this, "shake detected w/ speed: " + speed, Toast.LENGTH_SHORT).show();
		      }
		      last_x = ax;
		      last_y = ay;
		      last_z = az;
		    }			
			
		}
	}

	@Override
	public void onAccuracyChanged(Sensor p1, int p2)
	{
		// TODO: Implement this method
	}
    
}
