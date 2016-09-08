package com.alexmochalov.kaleidoscope;

public class Var
{
	static double gx = 0f;
	static double gy = 0f;

	public static void setDir(int dir)
	{
		switch (dir){
			case 0: 
				gx = 0;
				gy = 9.8f;
				break;
			case 1: 
				gx = -9.8f;
				gy = 0;
				break;
			case 2: 
				gx = 0;
				gy = -9.8f;
				break;
			case 3: 
				gx = 9.8f;
				gy = 0;
				break;
		}
	}}
