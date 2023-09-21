package com.orangomango.tetris;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.geometry.Rectangle2D;

public class UiButton{
	private Image image;
	private Runnable onClick;
	private Rectangle2D rect;

	public UiButton(Image image, Rectangle2D rect, Runnable onClick){
		this.image = image;
		this.rect = rect;
		this.onClick = onClick;
	}

	public boolean click(double x, double y){
		if (this.rect.contains(x, y)){
			this.onClick.run();
			return true;
		}

		return false;
	}

	public void setRect(Rectangle2D rect){
		this.rect = rect;
	}

	public void render(GraphicsContext gc){
		gc.drawImage(this.image, this.rect.getMinX(), this.rect.getMinY(), this.rect.getWidth(), this.rect.getHeight());
	}
}