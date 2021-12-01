package app.toolkit;

import java.io.IOException;
import java.util.ArrayList;

import app.controller.Controller;
import app.controller.MiniChatController;
import client.ClientConnector;
import datapackage.DataPackage;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MiniChat extends Controller {
	private Stage stage;
	private String sender;
	private String partnerName, partnerUsername;
	private MiniChatController controller;

	public MiniChat() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/gui/MiniChat.fxml"));

		stage = new Stage(StageStyle.UTILITY);
		this.sender = ClientConnector.getInstance().getCookie().get("username").toString();
		try {
			stage.setScene(new Scene(loader.load()));
			stage.setResizable(false);
			this.controller = loader.getController();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setPartner(String username, String name) {
		this.partnerName = name;
		this.partnerUsername = username;
		this.stage.setTitle("MiniChat - " + name);
		controller.setInfo(sender, partnerUsername, partnerName);
	}

	public void renderOldMessage(ArrayList<String> data) {
		controller.renderOldMessage(data);
	}

	public void receiveMessage(DataPackage data) {
		controller.setMessage(data, false);
	}

	public void close() {
		stage.close();
	}

	public void show() {
		if (stage != null)
			stage.show();
	}

	@Override
	public void focus() {
		Platform.runLater(() -> {
			stage.show();
			if (!stage.isFocused())
				stage.requestFocus();
		});
	}
}
