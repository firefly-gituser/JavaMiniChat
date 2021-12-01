package app.toolkit;

import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.Timer;

import app.controller.Controller;
import app.controller.NotificationsController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Notification implements ActionListener{
	public static enum Type{
		ERROR, WARN, INFORMATION,MESSAGE, FILE, IMAGE
	}
	private static final double TASKBAR_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height - GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height;
	private static final double SCREEN_WIDTH = Screen.getPrimary().getBounds().getMaxX();
	private static final double SCREEN_HEIGHT = Screen.getPrimary().getBounds().getMaxY();
	private static final double PADDING = 10;
	private Scene scene;
	private static Stage stage;
	private int duration = 7000;

	private NotificationsController controller;
	{
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/gui/Notification.fxml"));
		Parent parent = null;
		try {
			parent = loader.load();
		} catch (IOException e) {e.printStackTrace();}
		
		controller = loader.getController();
		this.scene = new Scene(parent);
		if(stage ==null)
		stage = new Stage(StageStyle.TRANSPARENT);
	}
	
	private int getType(Type type) {
		int number =5;
		switch (type) {
			case WARN:
				number = 1;
				break;
			case ERROR:
				number = 2;
				break;
			case MESSAGE:
				number = 3;
				break;
			case FILE:
				number = 4;
				break;
			case IMAGE:
				number = 5;
				break;
			default:
			number = 6;
		}
		return number;
	}

	
	public Notification(String title, String content, Type type) {
		controller.getData(title, content,getType(type));
	}
	
	public Notification setContext(Controller context) {
		this.controller.setContext(context);
		return this;
	}
	
	public void setPID() {
		//Notification
	}
	
	public void show() {
		
		stage.setScene(this.scene);
		stage.setAlwaysOnTop(true);
		stage.show();
		stage.setX(SCREEN_WIDTH - scene.getWidth() - PADDING );
		stage.setY(SCREEN_HEIGHT - scene.getHeight()- TASKBAR_HEIGHT - PADDING);
		Timer timer = new Timer(duration, this);
		timer.setRepeats(false);
		timer.start();
	}
	
	public void setDuration(int time) {
		this.duration =time;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Platform.runLater(()->stage.close());
	}
}
