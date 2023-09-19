package com.orangomango.tetris;

import javafx.scene.paint.Color;

public enum Piece{
	SHAPE_I(Color.CYAN, 1, 4, new boolean[]{true, true, true, true}, 0, 2),
	SHAPE_L(Color.ORANGE, 2, 3, new boolean[]{true, false, true, false, true, true}, 0, 1),
	SHAPE_L_FLIPPED(Color.BLUE, 2, 3, new boolean[]{false, true, false, true, true, true}, 0, 1),
	SHAPE_SQUARE(Color.YELLOW, 2, 2, new boolean[]{true, true, true, true}, 0, 0),
	SHAPE_T(Color.PURPLE, 3, 2, new boolean[]{true, true, true, false, true, false}, 1, 0),
	SHAPE_Z(Color.RED, 3, 2, new boolean[]{true, true, false, false, true, true}, 1, 0),
	SHAPE_Z_FLIPPED(Color.LIME, 3, 2, new boolean[]{false, true, true, true, true, false}, 1, 0);

	private Color color;
	private int width, height;
	private boolean[] shape;
	private int cx, cy;

	private Piece(Color color, int w, int h, boolean[] shape, int cx, int cy){
		this.color = color;
		this.width = w;
		this.height = h;
		this.shape = shape;
		this.cx = cx;
		this.cy = cy;
	}

	public int getWidth(){
		return this.width;
	}

	public int getHeight(){
		return this.height;
	}

	public boolean[] getShape(){
		return this.shape;
	}

	public Color getColor(){
		return this.color;
	}

	public int getCenterX(){
		return this.cx;
	}

	public int getCenterY(){
		return this.cy;
	}
}