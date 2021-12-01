package app.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

public class UserTagController implements Initializable {
	@FXML
	private Text username, name;

	RoomController controller;
	
	private String in_username, in_name;

	@FXML
	private void onMousPressHandle(MouseEvent event) {
		controller.openSingleChatPanel(in_username);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Platform.runLater(()->{
			username.setText(in_username);
			name.setText(in_name);
		});
	}
	
	public void setInfo(String vName, String vUsername) {
		this.in_name = vName;
		this.in_username = vUsername;
	}
	
	public void setContext(RoomController roomController) {
		this.controller = roomController;
	}

}
