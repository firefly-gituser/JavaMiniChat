package app.controller;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import client.ClientConnector;
import datapackage.ChatPackage;
import datapackage.DataPackage;
import datapackage.DataType;
import datapackage.SendType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MiniChatController implements Initializable {
	private ClientConnector connector;
	private String sender, partner;
	private String partnerName;
	@FXML
	private ScrollPane scrollMessage;

	@FXML
	private FlowPane messagePane;

	@FXML
	private TextField contentMiniChat;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		connector = ClientConnector.getInstance();
		contentMiniChat.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER)
				sendMessage();
		});
		messagePane.heightProperty().addListener(e->{
			scrollMessage.setVvalue(1);
		});
		
		
		Platform.runLater(()->{
			ChatPackage chatPackage = new ChatPackage(sender,SendType.MINICHATINIT, partner, null);
			connector.sendPackage(chatPackage);
		});
	}



	@FXML
	private void sendFileMiniChat(MouseEvent event) {
		Stage stage = new Stage();
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Choose your file");
		chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All", "*.*"));
		File file = chooser.showOpenDialog(stage);
		if (file != null) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Confirm");
			alert.setHeaderText("Are you sure to send this file?");
			Optional<ButtonType> type = alert.showAndWait();
			if (type.get() == ButtonType.OK) {
				DataPackage dataPackage = new DataPackage(DataType.FILE, file.getAbsolutePath());
				ChatPackage chatPackage = new ChatPackage(sender, SendType.SINGLE, partner, dataPackage);
				connector.sendPackage(chatPackage);
				setMessage(dataPackage, true);
			}
		}
	}

	@FXML
	private void sendImageMiniChat(MouseEvent event) {
		Stage stage = new Stage();
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Choose your image");
		chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image", "*.jpg", "*.png", "*.gif"));
		File file = chooser.showOpenDialog(stage);
		if (file != null) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Confirm");
			alert.setHeaderText("Are you sure to send this image?");
			Optional<ButtonType> type = alert.showAndWait();
			if (type.get() == ButtonType.OK) {
				DataPackage dataPackage = new DataPackage(DataType.IMAGE, file.getAbsolutePath());
				ChatPackage chatPackage = new ChatPackage(sender, SendType.SINGLE, partner, dataPackage);
				connector.sendPackage(chatPackage);
				setMessage(dataPackage, true);
			}
		}
	}

	@FXML
	private void sendMiniChat(MouseEvent event) {
		sendMessage();
	}

	private void sendMessage() {
		String message = contentMiniChat.getText().trim().replaceAll("'", "");
		if (message.length() > 0) {
			DataPackage dataPackage = new DataPackage(DataType.MESSAGE, message.replaceAll("'", ""));
			ChatPackage chatPackage = new ChatPackage(sender, SendType.SINGLE, partner, dataPackage);
			setMessage(dataPackage, true);
			connector.sendPackage(chatPackage);
		}
		contentMiniChat.setText("");
	}

	public void setInfo(String sender, String partner, String partnerName) {
		this.sender = sender;
		this.partner = partner;
		this.partnerName = partnerName;
	}
	
	public void renderOldMessage(ArrayList<String> data) {
		for (String string : data) {
			JSONObject json = (JSONObject) JSONValue.parse(string);
			DataPackage dataPackage = new DataPackage(DataType.MESSAGE,json.get("content").toString());
			boolean status = json.get("sender").toString().equalsIgnoreCase(sender);
			Platform.runLater(() -> {
				try {
					FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/gui/Message.fxml"));
					FlowPane message = loader.load();
					MessageController messageController = loader.getController();
					messageController.getDataPackage(dataPackage, status?sender:partnerName);
					messageController.setTimer(json.get("time").toString());
					messageController.setIsUser(status);
					messagePane.getChildren().add(message);
					
				} catch (Exception e) {
				}
			});
		}
	}

	public void setMessage(DataPackage data, boolean status) {
		Platform.runLater(() -> {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/gui/Message.fxml"));
				FlowPane message = loader.load();
				MessageController messageController = loader.getController();
				messageController.getDataPackage(data, partnerName);
				messageController.setIsUser(status);
				messagePane.getChildren().add(message);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
}
