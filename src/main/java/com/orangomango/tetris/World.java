package com.orangomango.tetris;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.*;

public class World{
	private double x, y;
	private int width, height;
	private List<Tetromino> tetrominoes = new ArrayList<>();

	public World(double x, double y, int w, int h){
		this.x = x;
		this.y = y;
		this.width = w;
		this.height = h;
	}

	public List<Tetromino> getTetrominoes(){
		return this.tetrominoes;
	}

	public int getWidth(){
		return this.width;
	}

	public int getHeight(){
		return this.height;
	}

	public void addTetromino(Tetromino t){
		this.tetrominoes.add(t);
	}

	private boolean pointExists(int x, int y){
		for (int i = 0; i < this.tetrominoes.size(); i++){
			Tetromino t = this.tetrominoes.get(i);
			if (t.getAbsoluteFromShape(x, y)){
				return true;
			}
		}

		return false;
	}

	public void checkLines(){
		for (int y = 0; y < this.height; y++){
			boolean completed = true;
			for (int x = 0; x < this.width; x++){
				boolean exists = pointExists(x, y);
				if (!exists){
					completed = false;
					break;
				}
			}

			if (completed){
				for (int i = 0; i < this.tetrominoes.size(); i++){
					Tetromino t = this.tetrominoes.get(i);
					t.removeRow(y);
					if (t.getMinY() < y){
						if (t.getMaxY() > y+1){
							t.partialFall();
						} else {
							t.fall();	
						}
					}
				}
			}
		}
	}

	public void update(){	
		for (int i = 0; i < this.tetrominoes.size(); i++){
			Tetromino t = this.tetrominoes.get(i);
			t.update();
		}
	}

	public void render(GraphicsContext gc){
		gc.setStroke(Color.WHITE);
		gc.setLineWidth(3);
		gc.strokeRect(this.x-3, this.y-3, Tetromino.SIZE*this.width+3, Tetromino.SIZE*this.height+3);

		gc.save();
		gc.translate(this.x, this.y);
		for (int i = 0; i < this.tetrominoes.size(); i++){
			Tetromino t = this.tetrominoes.get(i);
			t.render(gc);
		}
		gc.restore();
	}
}