package com.orangomango.tetris;

import javafx.scene.paint.Color;

public enum Piece{
	SHAPE_I(Color.CYAN, 4, 4, new boolean[]{false, true, false, false, false, true, false, false, false, true, false, false, false, true, false, false}),
	SHAPE_L(Color.ORANGE, 3, 3, new boolean[]{false, true, false, false, true, false, false, true, true}),
	SHAPE_L_FLIPPED(Color.BLUE, 3, 3, new boolean[]{false, true, false, false, true, false, true, true, false}),
	SHAPE_SQUARE(Color.YELLOW, 2, 2, new boolean[]{true, true, true, true}),
	SHAPE_T(Color.PURPLE, 3, 3, new boolean[]{false, false, false, true, true, true, false, true, false}),
	SHAPE_Z(Color.RED, 3, 3, new boolean[]{false, false, false, true, true, false, false, true, true}),
	SHAPE_Z_FLIPPED(Color.LIME, 3, 3, new boolean[]{false, false, false, false, true, true, true, true, false});

	private Color color;
	private int width, height;
	private boolean[] shape;

	private Piece(Color color, int w, int h, boolean[] shape){
		this.color = color;
		this.width = w;
		this.height = h;
		this.shape = shape;
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
}