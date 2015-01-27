//	Paddle.java
//	Justin, Ruth, Seth
//	OCCC 11/15/14
//	Advanced Java
//	Final Project
//	Last updated 12/06/2014

import java.awt.Color;
import java.awt.geom.*;

public class Paddle extends Arc2D.Double{
	
	//	Named constants
	private static final int WIDTH = 600;
	private static final int HEIGHT = 600;
	
	//	Variables
	private Color c;
	
	public Paddle(int x, int y, int startAngle, Color c){
		
		super(x, y, WIDTH, HEIGHT, startAngle, 16, Arc2D.OPEN);
		
		this.c = c;
	}
	
	public Color getColor(){
		return c;
	}	
}//end Paddle class