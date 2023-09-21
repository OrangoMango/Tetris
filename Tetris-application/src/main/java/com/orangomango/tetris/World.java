package com.orangomango.tetris;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;

import java.util.*;

import dev.webfx.platform.resource.Resource;

public class World{
	private double x, y;
	private int width, height;
	private List<Tetromino> tetrominoes = new ArrayList<>();
	private Image background = new Image(Resource.toUrl("/images/block8.png", getClass()));

	public World(double x, double y, int w, int h){
		this.x = x;
		this.y = y;
		this.width = w;
		this.height = h;
	}

	public List<Tetromino> getTetrominoes(){
		return this.tetrominoes;
	}

	public double getX(){
		return this.x;
	}

	public double getY(){
		return this.y;
	}

	public void setX(double v){
		this.x = v;
	}

	public void setY(double v){
		this.y = v;
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
		int rowsCleared = 0;
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
						if (t.getMaxY() > y){
							t.partialFall();
						} else {
							t.fall();	
						}
					}
				}
				rowsCleared++;
			}
		}

		switch (rowsCleared){
			case 1:
				MainApplication.score += 100;
				MainApplication.audio.get("clear.wav").play();
				break;
			case 2:
				MainApplication.score += 300;
				MainApplication.audio.get("clear.wav").play();
				break;
			case 3:
				MainApplication.score += 500;
				MainApplication.audio.get("clear.wav").play();
				break;
			case 4:
				MainApplication.score += 800;
				MainApplication.audio.get("tetris.wav").play();
				break;
		}
	}

	public void update(){	
		for (int i = 0; i < this.tetrominoes.size(); i++){
			Tetromino t = this.tetrominoes.get(i);
			t.update();
		}
	}

	public void render(GraphicsContext gc){
		gc.save();
		gc.translate(this.x, this.y);

		// Background
		for (int x = 0; x < this.width; x++){
			for (int y = 0; y < this.height; y++){
				gc.drawImage(this.background, x*Tetromino.SIZE, y*Tetromino.SIZE, Tetromino.SIZE, Tetromino.SIZE);
			}
		}

		for (int i = 0; i < this.tetrominoes.size(); i++){
			Tetromino t = this.tetrominoes.get(i);
			t.render(gc);
		}
		gc.restore();

		gc.setStroke(Color.BLACK);
		gc.setLineWidth(3);
		gc.strokeRect(this.x, this.y, Tetromino.SIZE*this.width, Tetromino.SIZE*this.height);
	}
}