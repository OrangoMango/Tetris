package com.orangomango.tetris;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.*;

public class Tetromino{
	public static final int SIZE = 30;

	private World world;
	private int x, y;
	private boolean falling = true;
	private int pieceWidth, pieceHeight;
	private boolean[] pieceShape;
	private Color pieceColor;
	private int cx, cy;

	public Tetromino(World world, int x, int y, Piece piece){
		this.world = world;
		this.x = x;
		this.y = y;
		this.pieceColor = piece.getColor();
		this.pieceWidth = piece.getWidth();
		this.pieceHeight = piece.getHeight();
		this.cx = piece.getCenterX();
		this.cy = piece.getCenterY();
		this.pieceShape = new boolean[piece.getShape().length];
		System.arraycopy(piece.getShape(), 0, this.pieceShape, 0, this.pieceShape.length);
	}

	public boolean isFalling(){
		return this.falling;
	}

	public int getY(){
		return this.y;
	}

	public int getPieceHeight(){
		return this.pieceHeight;
	}

	public void move(int n){
		if (this.falling){
			this.x += n;
			if (this.x < 0 || this.x+this.pieceWidth > this.world.getWidth() || collided()){
				this.x -= n;
			}
		}
	}

	public void partialFall(){
		int space = -1;
		for (int y = 0; y < this.pieceHeight; y++){
			boolean empty = true;
			for (int x = 0; x < this.pieceWidth; x++){
				if (this.pieceShape[x+this.pieceWidth*y]){
					empty = false;
					break;
				}
			}

			if (empty){
				space = y;
				break;
			}
		}

		if (space != -1){
			int h = this.pieceHeight-1;
			int newY = this.y+1;
			boolean[] shape = new boolean[this.pieceWidth*h];
			for (int y = 0; y < this.pieceHeight; y++){
				for (int x = 0; x < this.pieceWidth; x++){
					if (y < space){
						shape[x+this.pieceWidth*(y+1)] = this.pieceShape[x+this.pieceWidth*y];
					} else if (y > space){
						shape[x+this.pieceWidth*(y-1)] = this.pieceShape[x+this.pieceWidth*y];
					}
				}
			}
			this.y = newY;
			this.pieceHeight = h;
			this.pieceShape = shape;
		}
	}

	public void fall(){
		this.y += 1;

		if (collided()){
			this.y -= 1;
			this.falling = false;
		}

		if (this.y+this.pieceHeight >= this.world.getHeight()){
			this.falling = false;
		}
	}

	public void rotate(){
		if (this.falling){
			int w = this.pieceHeight;
			int h = this.pieceWidth;
			if (this.x+w >this.world.getWidth() || this.y+h >this.world.getHeight()){
				return;
			}
			boolean[] shape = new boolean[w*h];
			int newX = 0;
			int newY = 0;
			for (int x = 0; x < this.pieceWidth; x++){
				newX = 0;
				for (int y = this.pieceHeight-1; y >= 0; y--){
					shape[newX+w*newY] = this.pieceShape[x+this.pieceWidth*y];
					newX++;
				}
				newY++;
			}
			this.pieceWidth = w;
			this.pieceHeight = h;
			this.pieceShape = shape;
			//this.x += this.cx;
			//this.y -= this.cy;
		}
	}

	public void update(){
		if (this.falling){
			fall();
		}
	}

	private boolean collided(){
		List<Tetromino> tetrominoes = this.world.getTetrominoes();
		for (int i = 0; i < tetrominoes.size(); i++){
			Tetromino t = tetrominoes.get(i);
			if (t != this){
				for (int x = 0; x < this.pieceWidth; x++){
					for (int y = 0; y < this.pieceHeight; y++){
						boolean thisSquare = this.getAbsoluteFromShape(this.x+x, this.y+y);
						boolean otherSquare = t.getAbsoluteFromShape(this.x+x, this.y+y);
						if (thisSquare && otherSquare){
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	public void removeRow(int ry){
		ry = ry-this.y;
		if (ry >= 0 && ry < this.pieceHeight){
			for (int x = 0; x < this.pieceWidth; x++){
				this.pieceShape[x+this.pieceWidth*ry] = false;
			}
		}
	}

	public boolean getAbsoluteFromShape(int x, int y){
		if (x < this.x || y < this.y || x >= this.x+this.pieceWidth || y >= this.y+this.pieceHeight) return false;

		return this.pieceShape[(x-this.x)+this.pieceWidth*(y-this.y)];
	}

	public void stop(){
		this.falling = false;
	}

	public void render(GraphicsContext gc){
		for (int x = 0; x < this.pieceWidth; x++){
			for (int y = 0; y < this.pieceHeight; y++){
				boolean show = this.pieceShape[x+this.pieceWidth*y];
				if (show){
					gc.setFill(this.pieceColor);
					gc.fillRect((this.x+x)*SIZE, (this.y+y)*SIZE, SIZE, SIZE);
				}
			}
		}
	}
}