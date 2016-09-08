package com.alexmochalov.kaleidoscope;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.util.Log;

public class Sprite extends PixelObject{
	private Context context;

	
	private int indexChip = 0;
	private Rect   rect;
	
	private int x;
	private int y;
	
	private double deltaX;
	private double deltaY;

	private double deltaXmin;
	private double deltaYmin;

	private Bitmap chip0;
	private Bitmap chip;
	private Rect   rectChip;

	private void loadBitmap(int id, int dim){
		chip0 =  BitmapFactory.decodeResource(context.getResources(), id);
		
		indexChip = (int)(Math.random()*8);
		
		chip = Bitmap.createBitmap(chip0, indexChip*chip0.getHeight(), 0, chip0.getHeight()-1, chip0.getHeight()-1 );
		
		rectChip = new Rect(0, 0, chip.getWidth(), chip.getHeight());
		
		Bitmap chip1 =  Bitmap.createScaledBitmap(chip,
						dim, dim, false);
		
		rect = new Rect(0, 0, dim, dim);

		pixels = new boolean[dim][dim];
		for (int i=0; i< dim; i++)
			for (int j=0; j< dim; j++)
				if (chip1.getPixel(i, j) != 0)
					pixels[i][j] = true;
				else
					pixels[i][j] = false;
	}
	
	public Sprite(Context context, int dim, int id){
		super(dim);
		
		this.context = context;
		x = 100;
		y = 100;

		deltaX = 0;
		deltaY = 3;

		//x = 173;
		//x = 30;
		
		//deltaX = (int)(Math.random()*10-5);
		//deltaY = (int)(Math.random()*10-5);
		loadBitmap(id, dim);
		
	}
	
	private void addSpritesToScene(ArrayList<Sprite> objects, Scene scene){
		scene.reset();
		
		for (Sprite p: objects)
			if (p != this)
				for (int i=0; i< p.dim; i++)
					for (int j=0; j< p.dim; j++)
						try{
							scene.pixels[p.x+i][p.y+j] = scene.pixels[p.x+i][p.y+j] || p.pixels[i][j];
						}catch(Exception e){
							Log.d("", "!!!!!!! x = "+(p.x+i)+" y = "+(p.y+j));
							System.exit(0);
						}
	}
	
	public Sprite(Context context, int dim, int id, ArrayList<Sprite> objects, Scene scene){
		super(dim);

		this.context = context;

		loadBitmap(id, dim);
		
		addSpritesToScene(objects, scene);
		
		while (true){
			int x0 = (int)(Math.random()*200);
			int y0 = (int)(Math.random()*200);
			if (! intersect(scene, x0, y0)){
				x = x0;
				y = y0;
				break;
			}
		}
	}
	
	private boolean intersect(Scene scene, int x, int y) {
		for (int i=0; i< dim; i++)
			for (int j = 0; j < dim; j++)
				if ( x + i < 0 ||
					 y + j < 0 ||
					 x + i >= 200 ||
					 y + j >= 200 ||
					pixels[i][j] && scene.pixels[x + i][y + j])
				return true; 
		return false;
	}

	public void paint(Canvas canvas, Paint paint){
		canvas.drawBitmap(chip, rectChip, new Rect(x, y, x+dim*3, y+dim*3) ,  paint);
/*
		paint.setStyle(Style.STROKE);
		canvas.drawRect(new Rect(x*3, y*3, x*3+dim*3, y*3+dim*3) ,  paint);
		
		for (int i=0; i< dim; i++)
			for (int j = 0; j < dim; j++)
				if (pixels[i][j])
					canvas.drawPoint((x+i)*3, (y+j)*3, paint);
*/							
	}


	public void step(ArrayList<Sprite> objects, Scene scene) {
		deltaXmin = 0;
		deltaYmin = 0;

		addSpritesToScene(objects, scene);
		
		Point pointOfCrossing = new Point(); 
		
		if (isMovingPossible(pixels, scene, pointOfCrossing)){
			x = (int)( x + deltaX);
			y = (int)( y + deltaY);
			if (y + dim-1 > 199)
				Log.d("","$$ y "+y);
			
			deltaY = deltaY + Var.gy; 
			deltaX = deltaX + Var.gx; 
		} else if (isMovingBackPossible(scene, pointOfCrossing)) {
			x = (int)( x + deltaX);
			y = (int)( y + deltaY);
			if (y + dim-1 > 199)
				Log.d(""," y "+y);


			deltaY = deltaY + Var.gy; 
			deltaX = deltaX + Var.gx; 
		} else {
			deltaY = 0; 
			deltaX = 0; 
		}
		scene.reset();
		
	}

	private boolean isMovingPossible(boolean pixels[][], Scene scene, Point point) {
		point.x = (int)( x + deltaX);
		point.y = (int)( y + deltaY);
		
		for (int i=0; i< dim; i++)
			for (int j = 0; j < dim; j++)
				if (point.x + i < 0
						|| point.y + j < 0 
						|| point.x + i >= scene.dim
						|| point.y + j >= scene.dim
						|| pixels[i][j] && scene.pixels[point.x + i][point.y + j])
				return false;
				
		return true;
	}

	private boolean isMovingBackPossible(Scene scene, Point point) {
		double deltaX1 = -deltaX/2;
		double deltaY1 = -deltaY/2;
		
		int dX = 100 - point.x;
		int dY = 100 - point.y;
		
		//Log.d("", "deltaX "+deltaX+" deltaY "+deltaY);
		//Log.d("", "dx "+dX+" dY "+dY);
		
		double cos = 
			(deltaX1 * dX + deltaY1 * dY)/
				(Math.sqrt(deltaX1*deltaX1 + deltaY1*deltaY1)*
			  		Math.sqrt(dX*dX + dY*dY));

//		Log.d("", "cos "+cos);
			
		double rad = Math.acos(cos);
		//double rad1 = Math.PI - rad;
		double rad1 =  2 * rad; // - , +
		if (dX > 0 ) rad1 = - rad1;

		//Log.d("", "rad  "+rad);
		//Log.d("", "rad1 "+rad1);
		
		if (dX == 0 ){
			deltaX = 0; 
			deltaY = 0-deltaY; 					
		} else {
			deltaX = 0- (-Math.sin(rad1)*deltaY1+Math.cos(rad1)*deltaX1); 
			deltaY = 0- (Math.cos(rad1)*deltaY1+Math.sin(rad1)*deltaX1); 					
		}
		
		if (rotate(scene, point, dX))
			return true;
		
		return isMovingPossible(pixels, scene, point);
	}

	private boolean rotate(Scene scene, Point point, int dx) {
		if (dx > 0) indexChip++;
		else if (dx < 0) indexChip--;
		
		if (indexChip == 8) indexChip = 0;
		else if (indexChip < 0) indexChip = 7;
		
		
		Bitmap tempBmp = Bitmap.createBitmap(chip0, indexChip*chip0.getHeight(), 0, chip0.getHeight()-1, chip0.getHeight()-1 );

		Bitmap chip1 =  Bitmap.createScaledBitmap(tempBmp,
				dim, dim, false);
		
	    
		boolean tempPixels[][] = new boolean[dim][dim];
		
		for (int i=0; i< dim; i++)
			for (int j=0; j< dim; j++)
				if (chip1.getPixel(i, j) != 0)
					tempPixels[i][j] = true;
				else
					tempPixels[i][j] = false;

		if (isMovingPossible(tempPixels, scene, point)){
			chip = Bitmap.createBitmap(tempBmp);
			for (int i=0; i< dim; i++)
				for (int j=0; j< dim; j++)
						pixels[i][j] = tempPixels[i][j];
			
			return true;
		} else
	    
		return false;
	}

	public void rotate() {
		indexChip++;
		if (indexChip == 8) indexChip = 0;
		
		//Log.d("","  "+indexChip+"   "+(indexChip*chip0.getHeight()-1) + "   "+chip0.getWidth());
		
		chip = Bitmap.createBitmap(chip0, indexChip*chip0.getHeight(), 0, chip0.getHeight()-1, chip0.getHeight()-1 );

		Bitmap chip1 =  Bitmap.createScaledBitmap(chip,
				dim, dim, false);
		
		for (int i=0; i< dim; i++)
			for (int j=0; j< dim; j++)
				if (chip1.getPixel(i, j) != 0)
					pixels[i][j] = true;
				else
					pixels[i][j] = false;
	}
	
}
