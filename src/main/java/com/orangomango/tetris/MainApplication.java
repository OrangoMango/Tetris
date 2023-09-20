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
import javafx.scene.media.*;

import java.util.*;
import java.io.*;

public class MainApplication extends Application{
	private static final double WIDTH = 600;
	private static final double HEIGHT = 800;
	private static final int FPS = 40;

	private World world;
	private Tetromino fallingTetromino;
	private Map<KeyCode, Boolean> keys = new HashMap<>();
	private volatile int fallTime;
	private Piece nextPiece = null;

	public static volatile int score, highscore;
	public static Map<String, AudioClip> audio = new HashMap<>();
	private static Media BACKGROUND_MUSIC;
	private static Font MAIN_FONT = Font.loadFont(MainApplication.class.getResourceAsStream("/font.ttf"), 25);
	
	@Override
	public void start(Stage stage){
		StackPane pane = new StackPane();
		Canvas canvas = new Canvas(WIDTH, HEIGHT);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		pane.getChildren().add(canvas);

		loadHighscore();
		loadAudio();
		MediaPlayer mp = new MediaPlayer(BACKGROUND_MUSIC);
		mp.setCycleCount(MediaPlayer.INDEFINITE);
		mp.play();

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
						MainApplication.audio.get("block_landed.wav").play();
						createTetromino();
					}

					if (this.keys.getOrDefault(KeyCode.DOWN, false)){
						score += 2;
						MainApplication.audio.get("move.wav").play();
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

	private Tetromino createTempTetromino(){
		Tetromino temp = new Tetromino(this.world, 0, 0, this.fallingTetromino.getPiece());
		temp.setParent(this.fallingTetromino);
		for (int i = 0; i < this.fallingTetromino.getRotation(); i++){
			temp.rotate();
		}
		temp.setX(this.fallingTetromino.getX());
		temp.setY(this.fallingTetromino.getY());
		while (temp.isFalling()){
			temp.fall();
		}

		return temp;
	}

	private void saveHighscore(){
		File file = new File("highscore.txt");
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(Integer.toString(highscore));
			writer.close();
		} catch (IOException ex){
			ex.printStackTrace();
		}
	}

	private void loadHighscore(){
		File file = new File("highscore.txt");
		if (file.exists()){
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				highscore = Integer.parseInt(reader.readLine());
				reader.close();
			} catch (IOException ex){
				ex.printStackTrace();
			}
		}
	}

	private static void loadAudio(){
		try {
			BACKGROUND_MUSIC = new Media(MainApplication.class.getResource("/audio/background.mp3").toURI().toString());
			String[] audios = new String[]{"block_landed.wav", "clear.wav", "falling.wav", "gameover.wav", "move.wav", "rotate.wav", "tetris.wav"};

			for (String a : audios){
				audio.put(a, new AudioClip(MainApplication.class.getResource("/audio/"+a).toURI().toString()));
			}
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}

	private void gameOver(){
		MainApplication.audio.get("gameover.wav").play();
		this.world.getTetrominoes().clear();
		this.fallingTetromino = null;
		this.fallTime = 400;
		this.nextPiece = null;
		if (score > highscore){
			highscore = score;
			saveHighscore();
		}
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
			MainApplication.audio.get("rotate.wav").play();
			this.fallingTetromino.rotate();
			this.keys.put(KeyCode.UP, false);
		} else if (this.keys.getOrDefault(KeyCode.SPACE, false)){
			MainApplication.audio.get("falling.wav").play();
			Tetromino shadow = createTempTetromino();
			this.fallingTetromino.setX(shadow.getX());
			this.fallingTetromino.setY(shadow.getY());
			this.fallingTetromino.stop();
			score += 50;
			this.keys.put(KeyCode.SPACE, false);
		}

		this.world.render(gc);

		if (this.fallingTetromino.isFalling()){
			gc.save();
			gc.translate(this.world.getX(), this.world.getY());
			createTempTetromino().render(gc);
			gc.restore();
		}

		gc.setFill(Color.BLACK);
		gc.setFont(MAIN_FONT);
		gc.setTextAlign(TextAlignment.CENTER);
		gc.fillText("Score: "+score+"   Highscore: "+highscore, WIDTH/2, 60);

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
