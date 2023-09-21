package com.orangomango.tetris;

import javafx.scene.image.Image;

import dev.webfx.platform.resource.Resource;

public enum Piece{
	SHAPE_I("/images/block1.png", 4, 4, new boolean[]{false, true, false, false, false, true, false, false, false, true, false, false, false, true, false, false}),
	SHAPE_L("/images/block2.png", 3, 3, new boolean[]{false, true, false, false, true, false, false, true, true}),
	SHAPE_L_FLIPPED("/images/block3.png", 3, 3, new boolean[]{false, true, false, false, true, false, true, true, false}),
	SHAPE_SQUARE("/images/block4.png", 2, 2, new boolean[]{true, true, true, true}),
	SHAPE_T("/images/block5.png", 3, 3, new boolean[]{false, false, false, true, true, true, false, true, false}),
	SHAPE_Z("/images/block6.png", 3, 3, new boolean[]{false, false, false, true, true, false, false, true, true}),
	SHAPE_Z_FLIPPED("/images/block7.png", 3, 3, new boolean[]{false, false, false, false, true, true, true, true, false});

	private Image image;
	private int width, height;
	private boolean[] shape;

	private Piece(String imageName, int w, int h, boolean[] shape){
		this.image = new Image(Resource.toUrl(imageName, getClass()));
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

	public Image getImage(){
		return this.image;
	}
}