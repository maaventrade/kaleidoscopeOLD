package com.alexmochalov.kaleidoscope;

public class PixelObject {
	int dim;
	boolean pixels[][]; 

	
	public PixelObject(int dim){
		this.dim = dim;
		
		pixels = new boolean[dim][dim];
		for (int i=0; i< dim; i++)
			for (int j=0; j< dim; j++)
				pixels[i][j] = false;
	}
	
}
