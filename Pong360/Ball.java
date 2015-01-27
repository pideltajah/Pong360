//	Ball.java
//	Justin, Ruth, Seth
//	OCCC 11/15/14
//	Advanced Java
//	Final Project
//	Last updated 12/06/2014

import java.awt.Color;

public class Ball{
	
	private static final int START_X = 305;
	private static final int START_Y = 305;
	
	private int x, y; // Current ball position
	private int diameter; // Ball diameter
	private int dx; // Increment on ball's x-coordinate
	private int dy; // Increment on ball's y-coordinate
	private Color c;
	
	public Ball(Color c) {

		x = START_X; 
		y = START_Y;// Current ball position
		diameter = 20;
		dx = 0;
		dy = 5;
		this.c = c;
	}

	public int getX(){
		return x;
	}

	public int getY(){
		return y;
	}
	
	public void setY(int y){
		this.y = y;
	}
	public void setX(int x){
		this.x = x;
	}
	
	public int getDiameter(){
		return diameter;
	}

	public int getDx(){
		return dx;
	}

	public void setDx(int dx){
		this.dx = dx;
	}
	
	public int getDy(){
		return dy;
	}
	
	public void setDy(int dy){
		this.dy = dy;
	}
	
	public void moveBall(){
		x += dx;
		y += dy;
	}
	
	public Color getColor(){
		return c;
	}
}