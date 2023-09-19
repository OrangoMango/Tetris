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

	public Tetromino(World world, int x, int y, Piece piece){
		this.world = world;
		this.x = x;
		this.y = y;
		this.pieceColor = piece.getColor();
		this.pieceWidth = piece.getWidth();
		this.pieceHeight = piece.getHeight();
		this.pieceShape = new boolean[piece.getShape().length];
		System.arraycopy(piece.getShape(), 0, this.pieceShape, 0, this.pieceShape.length);
	}

	public boolean isFalling(){
		return this.falling;
	}

	public int getMinX(){
		int minX = Integer.MAX_VALUE;
		for (int y = 0; y < this.pieceHeight; y++){
			int foundX = -1;
			for (int x = 0; x < this.pieceWidth; x++){
				boolean pos = this.pieceShape[x+this.pieceWidth*y];
				if (pos){
					foundX = x;
					break;
				}
			}

			if (foundX != -1 && foundX < minX){
				minX = foundX;
			}
		}

		return this.x+minX;
	}

	public int getMaxX(){
		int maxX = Integer.MIN_VALUE;
		for (int y = 0; y < this.pieceHeight; y++){
			int foundX = -1;
			for (int x = this.pieceWidth-1; x >= 0; x--){
				boolean pos = this.pieceShape[x+this.pieceWidth*y];
				if (pos){
					foundX = x;
					break;
				}
			}

			if (foundX != -1 && foundX > maxX){
				maxX = foundX;
			}
		}

		return this.x+maxX;
	}

	public int getMinY(){
		int minY = Integer.MAX_VALUE;
		for (int x = 0; x < this.pieceWidth; x++){
			int foundY = -1;
			for (int y = 0; y < this.pieceHeight; y++){
				boolean pos = this.pieceShape[x+this.pieceWidth*y];
				if (pos){
					foundY = y;
					break;
				}
			}

			if (foundY != -1 && foundY < minY){
				minY = foundY;
			}
		}

		return this.y+minY;
	}

	public int getMaxY(){
		int maxY = Integer.MIN_VALUE;
		for (int x = 0; x < this.pieceWidth; x++){
			int foundY = -1;
			for (int y = this.pieceHeight-1; y >= 0; y--){
				boolean pos = this.pieceShape[x+this.pieceWidth*y];
				if (pos){
					foundY = y;
					break;
				}
			}

			if (foundY != -1 && foundY > maxY){
				maxY = foundY;
			}
		}

		return this.y+maxY;
	}

	public void move(int n){
		if (this.falling){
			this.x += n;
			if (getMinX() < 0 || getMaxX() > this.world.getWidth()-1 || collided()){
				this.x -= n;
			}
		}
	}

	public void partialFall(){
		int space = -1;
		final int offset = getMinY()-this.y;
		for (int y = offset; y < this.pieceHeight; y++){
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

		if (getMaxY() >= this.world.getHeight()-1){
			this.falling = false;
		}
	}

	public void rotate(){
		if (this.falling){
			int w = this.pieceHeight;
			int h = this.pieceWidth;

			// Fix position
			if (this.x+w > this.world.getWidth()){
				this.x = this.world.getWidth()-w;
			}
			if (this.x < 0) this.x = 0;
			if (this.y+h >this.world.getHeight()){
				this.y = this.world.getHeight()-h;
			}

			boolean[] shape = new boolean[w*h];
			int newY = 0;
			for (int x = 0; x < this.pieceWidth; x++){
				int newX = 0;
				for (int y = this.pieceHeight-1; y >= 0; y--){
					shape[newX+w*newY] = this.pieceShape[x+this.pieceWidth*y];
					newX++;
				}
				newY++;
			}
			this.pieceWidth = w;
			this.pieceHeight = h;
			this.pieceShape = shape;
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