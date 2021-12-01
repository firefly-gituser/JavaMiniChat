package app.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

import datapackage.DataPackage;
import datapackage.FileInfo;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class MessageController implements Initializable {
	
	private DataPackage datapackage;
	
	private Label timer;
	
	private boolean isUser = false; 
	private static Stage stage;
	
	@FXML
	private Label nameLabel;
	
	private String senderName;
	@FXML
	private FlowPane messagePanel, mesageBase;
	
	@FXML
	private VBox vBox;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Platform.runLater(()->{
			if(isUser) {
				mesageBase.setAlignment(Pos.TOP_RIGHT);
				messagePanel.setAlignment(Pos.TOP_RIGHT);
				nameLabel.setText("You");
				vBox.setAlignment(Pos.CENTER_RIGHT);
			}
			else {
				nameLabel.setText(senderName);
			}
			nameLabel.setStyle("-fx-background-color:#0373fc33; -fx-padding: 5px;-fx-background-radius:5px");
			setContent();
		});
	}
	
	public void getDataPackage(DataPackage dataPackage, String name) {
		this.datapackage = dataPackage;
		this.senderName =  name;
	}
	
	public void setIsUser(boolean status) {
		this.isUser = status;
	}
	
	public void setTimer(String time) {
		timer = new Label(time);
	}
	
	private void setContent() {
		switch (datapackage.getType()) {
			case IMAGE:
				imageHandle();
				break;
		
			case FILE:
				fileHandle();
				break;
	
			default:
				Label label = new Label((String)datapackage.getData());
				label.setWrapText(true);
				label.setMaxWidth(350);
				label.setAlignment(isUser?Pos.CENTER_RIGHT:Pos.CENTER_LEFT);
				label.setStyle("-fx-padding:5px");
				if(timer==null)
					timer = new Label((new SimpleDateFormat("hh:mm:ss")).format(new Date()));
				timer.setFont(new Font(14));
				timer.setOpacity(0.8);
				vBox.getChildren().add(label);
				vBox.getChildren().add(timer);
				break;
		}
	}
	
	private AnchorPane getImagePane(ImageView view, FileInfo fileInfo) {
		Button button = new Button();
		button.setText("Download");
		button.setStyle("-fx-background-color:#0373fc33;");
		button.setOnMousePressed(e->{
			DirectoryChooser chooser = new DirectoryChooser();
			File file = chooser.showDialog(new Stage());
			if(file!=null)
				fileInfo.getFile(file.getAbsolutePath());
		});
		AnchorPane.setLeftAnchor(view, 0.0);
		AnchorPane.setRightAnchor(view, 0.0);
		AnchorPane.setLeftAnchor(button, 0.0);
		AnchorPane anchorPane = new AnchorPane();
		anchorPane.getChildren().add(view);
		if(!isUser) anchorPane.getChildren().add(button);
		return anchorPane;
	}
	
	
	private void  imageHandle() {
		FileInfo fileInfo = (FileInfo) datapackage.getData();
		fileInfo.getFile("temp");
		try {
			ImageView view = new ImageView(new Image(new FileInputStream(new File("temp/" + fileInfo.getName()))));
			view.setPreserveRatio(true);
			view.setFitWidth(250);
			if(timer==null)
				timer = new Label((new SimpleDateFormat("hh:mm:ss")).format(new Date()));
			timer.setFont(new Font(14));
			timer.setOpacity(0.8);
			Platform.runLater(()->{
				vBox.getChildren().add(view);
				vBox.getChildren().add(timer);
			});
			view.setOnMousePressed(e -> {
				try {
					ImageView newView = new ImageView(
							new Image(new FileInputStream(new File("temp/" + fileInfo.getName()))));
					newView.setFitHeight(600);
					newView.setPreserveRatio(true);
					if (stage == null)
						stage = new Stage();
					stage.setScene(new Scene(getImagePane(newView, fileInfo)));
					stage.setHeight(600);
					stage.setWidth(800);
					stage.setTitle(fileInfo.getName());
					stage.setResizable(false);
					stage.show();
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
			});
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void fileHandle() {
		FileInfo fileInfo = (FileInfo) datapackage.getData();
		String extension= fileInfo.getName().substring(fileInfo.getName().lastIndexOf("."));

		AnchorPane filePane = new AnchorPane();
		filePane.setPrefSize(250, 60);;
		filePane.setStyle("-fx-background-color: #0373fc1A; -fx-background-radius:15px");
		
		Label name = new Label(extension);
		name.setFont(new Font(24));
		name.setPrefSize(65, 50);
		name.setAlignment(Pos.CENTER);
		name.setStyle("-fx-background-color: #0373fc33; -fx-background-radius:15px");
		
		Label stringName = new Label(fileInfo.getName());
		stringName.setFont(new Font(18));
		stringName.setPrefWidth(160);
		AnchorPane.setLeftAnchor(stringName, 75.0);
		AnchorPane.setTopAnchor(stringName, 5.0);
		
		AnchorPane.setLeftAnchor(name, 5.0);
		AnchorPane.setTopAnchor(name, 5.0);
		if(timer==null)
			timer = new Label((new SimpleDateFormat("hh:mm:ss")).format(new Date()));
		timer.setFont(new Font(14));
		timer.setOpacity(0.8);
		AnchorPane.setLeftAnchor(timer,75.0);
		AnchorPane.setTopAnchor(timer,35.0);
		
		if(!isUser)
			filePane.setOnMousePressed(e->{
				DirectoryChooser chooser = new DirectoryChooser();
				Stage stage = new Stage();
				chooser.setTitle("Choose your directory to download!");
				File file = chooser.showDialog(stage);
				if(file!=null) {
					fileInfo.getFile(file.getAbsolutePath());
				}
			});
		filePane.getChildren().add(name);
		filePane.getChildren().add(stringName);
		filePane.getChildren().add(timer);
		vBox.getChildren().add(filePane);
	}
}
