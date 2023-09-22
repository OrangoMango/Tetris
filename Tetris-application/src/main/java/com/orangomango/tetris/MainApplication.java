package com.orangomango.tetris;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.media.*;
import javafx.scene.image.Image;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Insets;

import java.util.*;
import java.util.function.Consumer;

import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.stack.ui.controls.dialog.DialogUtil;
import dev.webfx.stack.ui.controls.dialog.DialogCallback;
import dev.webfx.platform.resource.Resource;
import dev.webfx.platform.scheduler.Scheduler;
import dev.webfx.platform.storage.LocalStorage;

public class MainApplication extends Application{
	private static double WIDTH = Screen.getPrimary().getVisualBounds().getWidth();
	private static double HEIGHT = Screen.getPrimary().getVisualBounds().getHeight();
	private static final int FPS = 40;

	private World world;
	private Tetromino fallingTetromino;
	private Map<KeyCode, Boolean> keys = new HashMap<>();
	private volatile int fallTime;
	private Piece nextPiece = null;
	private boolean paused;
	private Leaderboard leaderboard;
	private List<Map.Entry<String, Integer>> entries;
	private UiButton leadButton, submitButton;
	private volatile boolean softDrop = false;

	public static volatile int score, highscore, difficulty = 5;
	public static Map<String, AudioClip> audio = new HashMap<>();
	private static Media BACKGROUND_MUSIC;
	private static Font MAIN_FONT = Font.loadFont(Resource.toUrl("/fonts/font.ttf", MainApplication.class), 25);
	private static int FALLING_SPEED = getSpeed();
	
	@Override
	public void start(Stage stage){
		StackPane pane = new StackPane();
		Canvas canvas = new Canvas(WIDTH, HEIGHT);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		CanvasPane canvasPane = new CanvasPane(canvas, (w, h) -> resize(w, h));
		pane.getChildren().add(canvasPane);

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

		this.leadButton = new UiButton(new Image(Resource.toUrl("/images/lead.png", getClass())), new Rectangle2D(WIDTH*0.1, HEIGHT*0.2, Tetromino.SIZE*3, Tetromino.SIZE*3), () -> {
			this.paused = !this.paused;
			this.leaderboard.load(() -> this.entries = this.leaderboard.getEntries());
		});

		this.submitButton = new UiButton(new Image(Resource.toUrl("/images/submit.png", getClass())), new Rectangle2D(WIDTH*0.1, HEIGHT*0.8, Tetromino.SIZE*3, Tetromino.SIZE*3), () -> {
			createTextAlert("Username:", canvasPane, s -> {
				if (!s.equals("") && highscore > 0){
					this.leaderboard.addEntry(s, highscore, () -> this.leaderboard.load(() -> this.entries = this.leaderboard.getEntries()));
				}
			});
		});

		canvas.setOnMousePressed(e -> {
			if (this.paused){
				boolean ok = this.submitButton.click(e.getX(), e.getY());
				if (!ok) this.paused = false;
			} else {
				this.leadButton.click(e.getX(), e.getY());
			}
		});

		this.leaderboard = new Leaderboard("https://mangogamesid.000webhostapp.com/games/tetris/leaderboard.php");
		this.leaderboard.load(() -> this.entries = this.leaderboard.getEntries());

		gameLoop(); // Start the game loop
		
		Timeline loop = new Timeline(new KeyFrame(Duration.millis(1000.0/FPS), e -> update(gc)));
		loop.setCycleCount(Animation.INDEFINITE);
		loop.play();

		stage.setTitle("Tetris");
		stage.getIcons().add(new Image(Resource.toUrl("/images/icon.png", getClass())));
		stage.setScene(new Scene(pane, WIDTH, HEIGHT));
		stage.show();
	}

	private void createTextAlert(String headerText, CanvasPane pane, Consumer<String> onSuccess){
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(5, 5, 5, 5));
        gridPane.setHgap(5);
        gridPane.setVgap(5);
        gridPane.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
        HtmlText header = new HtmlText("<b>"+headerText+"</b>");
        TextField field = new TextField();
        Button ok = new Button("OK");
        ok.setDefaultButton(true);
        Button cancel = new Button("CANCEL");
        cancel.setCancelButton(true);
        gridPane.add(header, 0, 0, 2, 1);
        gridPane.add(field, 0, 1, 2, 1);
        gridPane.add(cancel, 0, 2);
        gridPane.add(ok, 1, 2);
        DialogCallback callback = DialogUtil.showModalNodeInGoldLayout(gridPane, pane);
        field.requestFocus();
        ok.setOnAction(e -> {
        	callback.closeDialog();
        	onSuccess.accept(field.getText());
        });
        cancel.setOnAction(e -> callback.closeDialog());
    }

	private void gameLoop(){
		this.world.update();

		// Create a tetromino
		if (!this.fallingTetromino.isFalling()){
			this.world.checkLines();
			MainApplication.audio.get("block_landed.wav").play();
			createTetromino();
		}

		if (this.softDrop){
			score += 2;
			MainApplication.audio.get("move.wav").play();
		}

		Scheduler.scheduleDelay(this.softDrop ? this.fallTime/4 : this.fallTime, this::gameLoop);
	}

	private void resize(double w, double h){
		WIDTH = w;
		HEIGHT = h;
		Tetromino.SIZE = (int)(HEIGHT*0.0375);
		MAIN_FONT = Font.loadFont(Resource.toUrl("/fonts/font.ttf", MainApplication.class), HEIGHT*0.03);
		this.world.setX((WIDTH-10*Tetromino.SIZE)*0.5+Tetromino.SIZE*2);
		this.world.setY((HEIGHT-20*Tetromino.SIZE)/2);
		this.leadButton.setRect(new Rectangle2D(WIDTH*0.1, HEIGHT*0.2, Tetromino.SIZE*3, Tetromino.SIZE*3));
		this.submitButton.setRect(new Rectangle2D(WIDTH*0.1, HEIGHT*0.8, Tetromino.SIZE*3, Tetromino.SIZE*3));
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
		LocalStorage.setItem("highscore-tetris", Integer.toString(highscore));
	}

	private void loadHighscore(){
		String data = LocalStorage.getItem("highscore-tetris");
		if (data != null){
			highscore = Integer.parseInt(data);
		}
	}

	private static void loadAudio(){
		try {
			BACKGROUND_MUSIC = new Media(Resource.toUrl("/audio/background.mp3", MainApplication.class));
			String[] audios = new String[]{"block_landed.wav", "clear.wav", "falling.wav", "gameover.wav", "move.wav", "rotate.wav", "tetris.wav"};

			for (String a : audios){
				audio.put(a, new AudioClip(Resource.toUrl("/audio/"+a, MainApplication.class)));
			}
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}

	private void gameOver(){
		MainApplication.audio.get("gameover.wav").play();
		this.world.getTetrominoes().clear();
		this.fallingTetromino = null;
		this.fallTime = FALLING_SPEED;
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
		this.fallTime = FALLING_SPEED;
	}

	private static int getSpeed(){
		double t = difficulty/9.0;
		int speed = (int)Math.round((1-t)*950+50);
		return speed;
	}
	
	private void update(GraphicsContext gc){
		gc.clearRect(0, 0, WIDTH, HEIGHT);
		gc.setFill(Color.web("#D5FDEB"));
		gc.fillRect(0, 0, WIDTH, HEIGHT);

		if (this.paused){
			// Show leaderboard
			if (this.entries != null){
				gc.setFill(Color.BLACK);
				gc.setTextAlign(TextAlignment.CENTER);
				StringBuilder builder = new StringBuilder();
				builder.append("Click to exit\n\n");
				int i = 1;
				for (Map.Entry<String, Integer> entry : this.entries){
					builder.append((i++)+". "+entry.getKey()+"    "+entry.getValue()+"\n");
					if (i == 4){
						builder.append("\n");
					} else if (i == 11) break;
				}
				gc.fillText(builder.toString(), WIDTH/2, HEIGHT*0.15);
			}

			this.submitButton.render(gc);
			return;
		}

		this.leadButton.render(gc);

		if (this.fallingTetromino != null){
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
				if (this.fallingTetromino.isFalling()){
					MainApplication.audio.get("falling.wav").play();
					Tetromino shadow = createTempTetromino();
					score += (shadow.getMinY()-this.fallingTetromino.getMaxY()-1)*10;
					this.fallingTetromino.stop();
					this.fallingTetromino.setX(shadow.getX());
					this.fallingTetromino.setY(shadow.getY());
					this.keys.put(KeyCode.SPACE, false);
				}
			}
		}

		this.softDrop = this.keys.getOrDefault(KeyCode.DOWN, false);

		// Difficulty
		if (this.keys.getOrDefault(KeyCode.DIGIT1, false)){
			difficulty = 1;
			FALLING_SPEED = getSpeed();
			this.fallTime = FALLING_SPEED;
			this.keys.put(KeyCode.DIGIT1, false);
		} else if (this.keys.getOrDefault(KeyCode.DIGIT2, false)){
			difficulty = 2;
			FALLING_SPEED = getSpeed();
			this.fallTime = FALLING_SPEED;
			this.keys.put(KeyCode.DIGIT2, false);
		} else if (this.keys.getOrDefault(KeyCode.DIGIT3, false)){
			difficulty = 3;
			FALLING_SPEED = getSpeed();
			this.fallTime = FALLING_SPEED;
			this.keys.put(KeyCode.DIGIT3, false);
		} else if (this.keys.getOrDefault(KeyCode.DIGIT4, false)){
			difficulty = 4;
			FALLING_SPEED = getSpeed();
			this.fallTime = FALLING_SPEED;
			this.keys.put(KeyCode.DIGIT4, false);
		} else if (this.keys.getOrDefault(KeyCode.DIGIT5, false)){
			difficulty = 5;
			FALLING_SPEED = getSpeed();
			this.fallTime = FALLING_SPEED;
			this.keys.put(KeyCode.DIGIT5, false);
		} else if (this.keys.getOrDefault(KeyCode.DIGIT6, false)){
			difficulty = 6;
			FALLING_SPEED = getSpeed();
			this.fallTime = FALLING_SPEED;
			this.keys.put(KeyCode.DIGIT6, false);
		} else if (this.keys.getOrDefault(KeyCode.DIGIT7, false)){
			difficulty = 7;
			FALLING_SPEED = getSpeed();
			this.fallTime = FALLING_SPEED;
			this.keys.put(KeyCode.DIGIT7, false);
		} else if (this.keys.getOrDefault(KeyCode.DIGIT8, false)){
			difficulty = 8;
			FALLING_SPEED = getSpeed();
			this.fallTime = FALLING_SPEED;
			this.keys.put(KeyCode.DIGIT8, false);
		} else if (this.keys.getOrDefault(KeyCode.DIGIT9, false)){
			difficulty = 9;
			FALLING_SPEED = getSpeed();
			this.fallTime = FALLING_SPEED;
			this.keys.put(KeyCode.DIGIT9, false);
		} else if (this.keys.getOrDefault(KeyCode.DIGIT0, false)){
			difficulty = 0;
			FALLING_SPEED = getSpeed();
			this.fallTime = FALLING_SPEED;
			this.keys.put(KeyCode.DIGIT0, false);
		}

		this.world.render(gc);

		if (this.fallingTetromino != null && this.fallingTetromino.isFalling()){
			gc.save();
			gc.translate(this.world.getX(), this.world.getY());
			createTempTetromino().render(gc);
			gc.restore();
		}

		gc.setFill(Color.BLACK);
		gc.setFont(MAIN_FONT);
		gc.setTextAlign(TextAlignment.CENTER);
		gc.fillText("Score: "+score+"   Highscore: "+highscore+"   Difficulty: "+difficulty, WIDTH/2, HEIGHT*0.1);

		gc.setStroke(Color.BLACK);
		gc.setLineWidth(3);
		gc.setFill(Color.BLACK);
		double xp = this.world.getX()-Tetromino.SIZE*5.5;
		gc.fillText("NEXT", xp+Tetromino.SIZE*2+10, HEIGHT*0.6);
		gc.strokeRect(xp, HEIGHT*0.62, Tetromino.SIZE*4.8, Tetromino.SIZE*4.8);
		Tetromino.render(gc, this.nextPiece, xp+Tetromino.SIZE*0.4, HEIGHT*0.62+Tetromino.SIZE*0.4);
	}
	
	public static void main(String[] args){
		launch(args);
	}
}
