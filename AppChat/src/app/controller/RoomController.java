package app.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import app.toolkit.MiniChat;
import app.toolkit.Notification;
import app.toolkit.Notification.Type;
import client.ClientConnector;
import datapackage.ChatPackage;
import datapackage.DataPackage;
import datapackage.DataType;
import datapackage.SendType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class RoomController extends Controller implements Initializable {
	private ClientConnector connector;
	private ArrayList<String> onlineList;
	private JSONObject userInfo;
	private Map<String, MiniChat> listMiniChat = new HashMap<>();
	
	@FXML
	private TextField messageInput;

	@FXML
	private ScrollPane scrollMessage, scrollLogger;

	@FXML
	private FlowPane userOnlinePanel, loggerPanel, broadcastMessagePanel;

	@FXML
	private VBox vBoxLogger;
	
	@FXML
	private void sendFileHandle(MouseEvent event) {
		Stage stage = new Stage();
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Choose your file");
		chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All", "*.*"));
		File file = chooser.showOpenDialog(stage);
		if (file != null) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Confirm");
			alert.setHeaderText("Are you sure to send this image?");
			Optional<ButtonType> type = alert.showAndWait();
			if (type.get() == ButtonType.OK) {
				DataPackage dataPackage = new DataPackage(DataType.FILE, file.getAbsolutePath());
				ChatPackage chatPackage = new ChatPackage(userInfo.get("username").toString(), SendType.BROADCAST,
						"broadcast", dataPackage);
				connector.sendPackage(chatPackage);
				renderBroadcastMessage(chatPackage, true);
			}
		}
	}

	@FXML
	private void sendImageHandle(MouseEvent event) {
		Stage stage = new Stage();
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Choose your file");
		chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image", "*.jpg", "*.png", "*.gif"));
		File file = chooser.showOpenDialog(stage);
		if (file != null) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Confirm");
			alert.setHeaderText("Are you sure to send this image?");
			Optional<ButtonType> type = alert.showAndWait();
			if (type.get() == ButtonType.OK) {
				DataPackage dataPackage = new DataPackage(DataType.IMAGE, file.getAbsolutePath());
				ChatPackage chatPackage = new ChatPackage(userInfo.get("username").toString(), SendType.BROADCAST,
						"broadcast", dataPackage);
				connector.sendPackage(chatPackage);
				renderBroadcastMessage(chatPackage, true);
			}
		}
	}

	@FXML
	private void logoutHandle(MouseEvent mouseEvent) {
		File file = new File("temp");
		for (File item : file.listFiles())
			item.delete();
		listMiniChat.forEach((key, windows) -> windows.close());
		Platform.runLater(() -> {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/gui/Login.fxml"));
			Parent parent = null;
			try {
				parent = loader.load();
			} catch (IOException e) {
				e.getMessage();
			}
			ChatPackage chatPackage = new ChatPackage(userInfo.get("username").toString(), SendType.LOGOUT, null, null);

			connector.sendPackage(chatPackage);

			connector.setContext(loader.getController());

			Stage roomStage = (Stage) (messageInput.getScene().getWindow());

			Stage loginStage = new Stage();
			loginStage.setResizable(false);
			loginStage.getIcons().add(new Image(getClass().getResourceAsStream("/flat/animation.png")));
			loginStage.setTitle("MiniChat - Login");
			loginStage.setScene(new Scene(parent));
			roomStage.close();
			loginStage.show();

		});
	}

	@FXML
	private void sendButtonHandle(MouseEvent event) {
		if (messageInput.getText().trim().length() > 0)
			sendMessage(messageInput.getText().toString());
		messageInput.setText("");
	}

	private void initRoomInfo() {
		Stage stage = ((Stage) messageInput.getScene().getWindow());
		stage.setTitle("MiniChat - " + userInfo.get("name") + " || " + userInfo.get("username"));
		stage.setOnCloseRequest(e -> {
			File file = new File("temp");
			for (File item : file.listFiles())
				item.delete();
			listMiniChat.forEach((key, windows) -> windows.close());
			connector.stop();
			Platform.exit();
		});
		ChatPackage chatPackage = 
				new ChatPackage(userInfo.get("username").toString(), 
						SendType.ROOMINFO, "server",null);
		connector.sendPackage(chatPackage);
		writeLogger("Logined");
	}

	/**
	 * @author tamth
	 * 
	 * @param location, resources
	 * 
	 * @exception error when delete the function
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.connector = ClientConnector.getInstance();
		this.userInfo = connector.getCookie();
		this.connector.setContext(this);
		Platform.runLater(() -> initRoomInfo());
		messageInput.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				if (messageInput.getText().trim().length() > 0)
					sendMessage(messageInput.getText().toString());
				messageInput.setText("");
			}
		});
		broadcastMessagePanel.heightProperty().addListener(e -> scrollMessage.setVvalue(1.0));

		loggerPanel.heightProperty().addListener(e -> scrollLogger.setVvalue(1.0));

	}

	@Override
	public void focus() {
		((Stage) messageInput.getScene().getWindow()).requestFocus();
	}

	public void renderUserOnline(ArrayList<String> list) {
		this.onlineList = list;
		Platform.runLater(() -> {
			userOnlinePanel.getChildren().clear();
			for (String string : list)
				try {
					JSONObject json = (JSONObject) JSONValue.parse(string);
					if (json.get("username").toString().equalsIgnoreCase(userInfo.get("username").toString()))
						continue;
					FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/gui/UserTag.fxml"));
					AnchorPane userTag = loader.load();
					UserTagController userTagController = loader.getController();
					userTagController.setContext(this);
					userTagController.setInfo(json.get("name").toString(), json.get("username").toString());
					userOnlinePanel.getChildren().add(userTag);
				} catch (IOException e) {
					e.printStackTrace();
				}
		});
	}

	public void renderBroadcastMessage(ChatPackage data, boolean status) {
		String name = getName(data.getSender());
		Platform.runLater(() -> {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/gui/Message.fxml"));
				FlowPane message = loader.load();
				MessageController messageController = loader.getController();
				messageController.getDataPackage((DataPackage) data.getDataPackage(), name);
				messageController.setIsUser(status);
				broadcastMessagePanel.getChildren().add(message);
				writeLogger((status ? "Send" : "Receive") + " a broadcast!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public void renderBroadcastOldMessage(ArrayList<String> data) {
		for (String string : data) {
			JSONObject json = (JSONObject) JSONValue.parse(string);
			DataPackage dataPackage = new DataPackage(DataType.MESSAGE, json.get("content").toString());
			boolean status = json.get("sender").toString().equalsIgnoreCase(userInfo.get("username").toString());
			Platform.runLater(() -> {
				try {
					FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/gui/Message.fxml"));
					FlowPane message = loader.load();
					MessageController messageController = loader.getController();
					messageController.getDataPackage(dataPackage, json.get("name").toString());
					messageController.setTimer(json.get("time").toString());
					messageController.setIsUser(status);
					broadcastMessagePanel.getChildren().add(message);

				} catch (Exception e) {
				}
			});
		}
		writeLogger("Render old broadcast!");
	}

	private void sendMessage(String message) {
		DataPackage dataPackage = new DataPackage(DataType.MESSAGE, message.replaceAll("'", ""));

		ChatPackage chatPackage = new ChatPackage(userInfo.get("username").toString(), SendType.BROADCAST, "broadcast",
				dataPackage);

		renderBroadcastMessage(chatPackage, true);
		connector.sendPackage(chatPackage);
	}

	private void writeLogger(String content) {
		String time = new SimpleDateFormat("hh:mm:ss").format(new Date());
		Label text = new Label("[" + time + "]: " + content);
		text.setWrapText(true);
		text.setMaxWidth(190);
		Platform.runLater(() -> vBoxLogger.getChildren().add(new AnchorPane(text)));
	}

	private String getName(String username) {
		for (String string : onlineList) {
			JSONObject user = (JSONObject) JSONValue.parse(string);
			if (user.get("username").toString().equalsIgnoreCase(username))
				return user.get("name").toString();
		}
		return null;
	}

	private MiniChat getMiniChat(String username) {
		MiniChat miniChat = listMiniChat.get(username);
		if (miniChat == null) {
			miniChat = new MiniChat();
			miniChat.setPartner(username, getName(username));
			listMiniChat.put(username, miniChat);
		}
		return miniChat;
	}

	public void openSingleChatPanel(String username) {
		MiniChat miniChat = getMiniChat(username);
		miniChat.show();
		miniChat.focus();
		writeLogger("Open a mini chat");
	}

	public void renderSingleMessage(ChatPackage data) {
		Platform.runLater(() -> {
			MiniChat miniChat = getMiniChat(data.getSender());
			miniChat.receiveMessage((DataPackage) data.getDataPackage());
			switch (((DataPackage) data.getDataPackage()).getType()) {
			case FILE:
				new Notification("You was received a file", "The message of " + getName(data.getSender()) , Type.FILE).setContext(miniChat).show();
				break;

			case IMAGE:
				new Notification("You was received a image", "The message of " + getName(data.getSender()) , Type.IMAGE).setContext(miniChat).show();
				break;
			
			default:
				new Notification("You was received a message", "The message of " +getName(data.getSender()) , Type.MESSAGE).setContext(miniChat).show();
			}

		});
		writeLogger("Receive single message from " +getName(data.getSender()));
	}

	public void initMiniChatRoom(ArrayList<String> dataString, String destination) {
		listMiniChat.get(destination).renderOldMessage(dataString);
	}
}
