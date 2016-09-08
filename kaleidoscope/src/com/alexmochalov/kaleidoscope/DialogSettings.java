package com.alexmochalov.kaleidoscope;

import android.app.Dialog;
import android.content.Context;
import android.hardware.Camera.CameraInfo;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.*;
import android.util.*;

/**
 * 
 * @author Alexey Mochalov
 * 
 * Dialog for the setting application parameters  
 *
 */
public class DialogSettings extends Dialog{
	// Copy of this object to use in the child dialog
	private Dialog dialogSettings;
	
	public DialogSettings(final Context context, final Preview preview, final SurfaceViewScreen surfaceViewScreen) {
		super(context);
		dialogSettings = this;
			
		setContentView(R.layout.dialog_settings);
		getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		
		setTitle("Settings");

		// The radio button for the selecting phone camera 
		RadioGroup cameraGroup = (RadioGroup)findViewById(R.id.radioGroupCamera);
		if (preview.getFacing() == 0){
			RadioButton r = (RadioButton)findViewById(R.id.cameraBackside);
			r.setChecked(true);
		} else {
			RadioButton r = (RadioButton)findViewById(R.id.cameraFrontal);
			r.setChecked(true);
		}
		cameraGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){

				@Override
				public void onCheckedChanged(RadioGroup p1, int checkId)
				{
					
					Log.d("","checkId  "+checkId);
					switch (checkId){
						case R.id.cameraFrontal:
							preview.setFacing(CameraInfo.CAMERA_FACING_FRONT);
							break;
						case R.id.cameraBackside:
							preview.setFacing(CameraInfo.CAMERA_FACING_BACK);
							break;
						
					}
				}
			});
		
		CheckBox checkBoxGlases = (CheckBox)findViewById(R.id.glasses);
		checkBoxGlases.setChecked(surfaceViewScreen.setGlasses());
		checkBoxGlases.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(CompoundButton buttonView,
											 boolean isChecked) {
					surfaceViewScreen.setGlasses(isChecked);
				}});
		
			
		// The Seekbar changes size of the kaleydoscope triangles 
		SeekBar seekBarScale = (SeekBar)findViewById(R.id.seekBarScale);
		seekBarScale.setMax(100);
		seekBarScale.setProgress((int)(preview.getScale()*50));
		seekBarScale.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				preview.setScale((seekBar.getProgress()+5)/50f);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}});       
		
		// Set the speed of the sliding triangles in the boards of image
		SeekBar seekBarSliding = (SeekBar)findViewById(R.id.seekBarSliding);
		seekBarSliding.setMax(20);
		seekBarSliding.setProgress(preview.getSliding());
		seekBarSliding.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				preview.setSliding((seekBar.getProgress()));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}});       
	
		// This button closes the dialog 
		Button buttonClose = (Button)findViewById(R.id.buttonClose);
		buttonClose.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		
		// This button opens the dialog About 
		Button buttonInfo = (Button)findViewById(R.id.buttonAbout);
		buttonInfo.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				final Dialog dialog = new Dialog(context);
				dialog.setContentView(R.layout.dialog_about);
				dialog.setTitle("About");
				
				TextView textView = (TextView)dialog.findViewById(R.id.textViewEMail);
				textView.setText(Html.fromHtml(context.getResources().getString(R.string.about_3)));
				textView.setMovementMethod(LinkMovementMethod.getInstance());				

				Button buttonClose = (Button)dialog.findViewById(R.id.buttonOk);
				buttonClose.setOnClickListener(new Button.OnClickListener(){
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
					
				});
				
				dialog.show();
				dialogSettings.dismiss();
			}
			});
		
		// Switch flash on/off
		CheckBox checkBoxFlash = (CheckBox)findViewById(R.id.checkBoxFlash);
		checkBoxFlash.setChecked(preview.getFlash());
		checkBoxFlash.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				preview.setFlash(isChecked);
			}});
		
		// Switch showing icon Photo on/off
		CheckBox checkBoxShowIconPhoto = (CheckBox)findViewById(R.id.checkBoxIconPhoto);
		checkBoxShowIconPhoto.setChecked(surfaceViewScreen.getShowIconPhoto());
		checkBoxShowIconPhoto.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				surfaceViewScreen.setShowIconPhoto(isChecked);
			}});

		
		// Switch showing shadow on/off
		CheckBox checkBoxShowShadow = (CheckBox)findViewById(R.id.checkBoxShadow);
		checkBoxShowShadow.setChecked(surfaceViewScreen.getShowShadow());
		checkBoxShowShadow.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				surfaceViewScreen.setShowShadow(isChecked);
			}});
		
		Spinner spinnerResolution = (Spinner)findViewById(R.id.spinnerResolution);

	    String[] items = preview.getSupportedPictureSizes();
	    items[0] = "Screenshot from preview";

	    ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
	            android.R.layout.simple_spinner_item, items);

	    spinnerResolution.setAdapter(adapter);

	    spinnerResolution.setOnItemSelectedListener(new OnItemSelectedListener() {
	        @Override
	        public void onItemSelected(AdapterView<?> parent, View view,
	                int position, long id) {
	        	preview.setPictureSize(position-1);
	        }

	        @Override
	        public void onNothingSelected(AdapterView<?> parent) {
	            // TODO Auto-generated method stub
	        }
	    });		
	    spinnerResolution.setSelection(preview.getPictureSize()+1);
		
	}
}
