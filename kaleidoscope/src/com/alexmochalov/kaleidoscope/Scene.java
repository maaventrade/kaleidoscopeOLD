package com.alexmochalov.kaleidoscope;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Scene extends PixelObject{
	// 200 x 200
		boolean copy[][];
	
		public Scene(int dim){
			super(dim);
			
			float d = dim/2f;
			pixels = new boolean[dim][dim];
			copy = new boolean[dim][dim];
			
			for (int i=0; i< dim; i++)
				for (int j=0; j< dim; j++)
					if (Math.sqrt((i-d)*(i-d) + (j-d)*(j-d)) >= d-2 ){
						pixels[i][j] = true;
						copy[i][j] = true;
					}	
		}

/*
		public Scene(Scene scene){
			super(scene.dim);
			
			pixels = new boolean[dim][dim];
			for (int i=0; i< dim; i++)
				for (int j=0; j< dim; j++)
						pixels[i][j] = scene.pixels[i][j];
		}
		*/
		
		public void paint(Canvas canvas, Paint paint){
			
			//for (int i=0; i< dim; i++)
			//	for (int j=0; j< dim; j++)
			//		if (pixels[i][j])
			//			canvas.drawRect(i*3, j*3, i*3+3, j*3+3, paint);
			
			//canvas.drawLine(100, 100, (100-70), (100+70), paint);
			//canvas.drawLine(100, 100, (100+70), (100+70), paint);
			
		}

		public void reset() {
			for (int i=0; i< dim; i++)
				for (int j=0; j< dim; j++)
					pixels[i][j] = copy[i][j];
		}

		
	}
