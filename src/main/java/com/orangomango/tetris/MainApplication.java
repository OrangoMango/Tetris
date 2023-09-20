package com.orangomango.tetris;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.*;

public class MainApplication extends Application{
	private static final double WIDTH = 600;
	private static final double HEIGHT = 800;
	private static final int FPS = 40;

	private World world;
	private Tetromino fallingTetromino;
	private Map<KeyCode, Boolean> keys = new HashMap<>();
	private volatile int fallTime;
	private Piece nextPiece = null;

	public static volatile int score;
	
	@Override
	public void start(Stage stage){
		StackPane pane = new StackPane();
		Canvas canvas = new Canvas(WIDTH, HEIGHT);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		pane.getChildren().add(canvas);

		canvas.setFocusTraversable(true);
		canvas.setOnKeyPressed(e -> this.keys.put(e.getCode(), true));
		canvas.setOnKeyReleased(e -> this.keys.put(e.getCode(), false));

		this.world = new World((WIDTH-10*Tetromino.SIZE)*0.7, (HEIGHT-20*Tetromino.SIZE)/2, 10, 20);
		createTetromino();

		Thread gameLoop = new Thread(() -> {
			while (true){
				try {
					this.world.update();

					// Create a tetromino
					if (!this.fallingTetromino.isFalling()){
						this.world.checkLines();
						createTetromino();
					}

					if (this.keys.getOrDefault(KeyCode.DOWN, false)){
						score += 2;
					}

					Thread.sleep(this.keys.getOrDefault(KeyCode.DOWN, false) ? 100 : this.fallTime);
				} catch (InterruptedException ex){
					ex.printStackTrace();
				}
			}
		});
		gameLoop.setDaemon(true);
		gameLoop.start();
		
		Timeline loop = new Timeline(new KeyFrame(Duration.millis(1000.0/FPS), e -> update(gc)));
		loop.setCycleCount(Animation.INDEFINITE);
		loop.play();

		stage.setTitle("Tetris");
		stage.setResizable(false);
		stage.setScene(new Scene(pane, WIDTH, HEIGHT));
		stage.show();
	}

	private void gameOver(){
		System.out.println("Game over");
		this.world.getTetrominoes().clear();
		this.fallingTetromino = null;
		this.fallTime = 400;
		this.nextPiece = null;
		score = 0;
		createTetromino();
	}

	private void createTetromino(){
		Random random = new Random();
		Piece p = this.nextPiece == null ? Piece.values()[random.nextInt(Piece.values().length)] : this.nextPiece;
		this.nextPiece = Piece.values()[random.nextInt(Piece.values().length)];
		Tetromino t = new Tetromino(this.world, this.world.getWidth()/2-p.getWidth()/2, 0, p);

		if (t.collided()){
			gameOver();
			return;
		}

		this.fallingTetromino = t;
		this.world.addTetromino(t);
		this.fallTime = 400;
	}
	
	private void update(GraphicsContext gc){
		gc.clearRect(0, 0, WIDTH, HEIGHT);
		gc.setFill(Color.web("#D5FDEB"));
		gc.fillRect(0, 0, WIDTH, HEIGHT);

		if (this.keys.getOrDefault(KeyCode.RIGHT, false)){
			this.fallingTetromino.move(1);
			this.keys.put(KeyCode.RIGHT, false);
		} else if (this.keys.getOrDefault(KeyCode.LEFT, false)){
			this.fallingTetromino.move(-1);
			this.keys.put(KeyCode.LEFT, false);
		} else if (this.keys.getOrDefault(KeyCode.UP, false)){
			this.fallingTetromino.rotate();
			this.keys.put(KeyCode.UP, false);
		} else if (this.keys.getOrDefault(KeyCode.SPACE, false)){
			this.fallTime = 0;
			score += 50;
			this.keys.put(KeyCode.SPACE, false);
		}

		this.world.render(gc);

		gc.setFill(Color.BLACK);
		gc.setFont(new Font("sans-serif", 20));
		gc.setTextAlign(TextAlignment.CENTER);
		gc.fillText("Score: "+score, WIDTH/2, 60);

		gc.setStroke(Color.BLACK);
		gc.setLineWidth(3);
		gc.setFill(Color.BLACK);
		gc.fillText("NEXT", 30+Tetromino.SIZE*2+10, 480);
		gc.strokeRect(30, 490, Tetromino.SIZE*4+20, Tetromino.SIZE*4+20);
		Tetromino.render(gc, this.nextPiece, 40, 500);
	}
	
	public static void main(String[] args){
		launch(args);
	}
}
