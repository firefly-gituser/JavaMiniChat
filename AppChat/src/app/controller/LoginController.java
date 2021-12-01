package app.controller;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.json.simple.JSONObject;

import app.toolkit.Notification;
import app.toolkit.Notification.Type;
import client.ClientConnector;
import datapackage.ChatPackage;
import datapackage.LoginInfo;
import datapackage.SendType;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginController extends Controller implements Initializable {
	private ClientConnector connector;

	@FXML
	private TextField usernameField;

	@FXML
	private PasswordField passwordField;

	@FXML
	private AnchorPane signupPanel;

	@FXML
	private TextField signup_name, signup_username;

	@FXML
	private ComboBox<Gender> signup_gender;

	@FXML
	private PasswordField signup_password, signup_confirm;

	@SuppressWarnings("unchecked")
	@FXML
	private void signupHandle(MouseEvent event) {
		if(!validationSignup()) return;
		
		JSONObject json = new JSONObject();
		Map<String, String> map = new HashMap<>();
		map.put("username", signup_username.getText());
		map.put("password", signup_password.getText());
		map.put("gender", signup_gender.getValue().getValue()+"");
		map.put("name", signup_name.getText());
		json.putAll(map);
		ChatPackage chatPackage = new ChatPackage("client",SendType.SIGNUP,"server", json);
		connector.sendPackage(chatPackage);
	}

	@FXML
	private void loginHandle(MouseEvent event) throws IOException {
		login();
	}

	@FXML
	private void openSignupHandle(MouseEvent event) {
		TranslateTransition transition = new TranslateTransition();
		transition.setNode(signupPanel);
		transition.setByX(450);
		transition.setDuration(Duration.millis(500));
		transition.play();
	}

	@FXML
	void closeSignupHandle(MouseEvent event) {
		closeSignUp();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		//Get instance of the connect tor
		connector = ClientConnector.getInstance();
		connector.setContext(this);
		
		//On the windows close, the connector should be closed
		Platform.runLater(
				() -> ((Stage) usernameField.getScene().getWindow())
				.setOnCloseRequest(e ->{
					connector.stop();
					Platform.exit();
				}));
	
		//Initialize value for the combo box of the gender and behavior
		signup_gender.getItems().addAll(new Gender("Nam", 1),new Gender("Nữ", 0));
		signup_gender.setButtonCell(new ListCell<Gender>() {
			protected void updateItem(Gender item, boolean empty) {
				if (empty || item == null)
					setStyle("-fx-text-fill: derive(-fx-control-inner-background,-30%)");
				else {
					setStyle("-fx-text-fill: -fx-text-inner-color");
					setText(item.toString());
				}
			};
		});
		signup_gender.valueProperty().addListener(new ChangeListener<Gender>() {
			@Override
			public void changed(ObservableValue<? extends Gender> observable, Gender oldValue, Gender newValue) {
				signup_gender.setButtonCell(new ListCell<Gender>() {
					protected void updateItem(Gender item, boolean empty) {
						if (empty || item == null)
							setStyle("-fx-text-fill: derive(-fx-control-inner-background,-30%)");
						else {
							setStyle("-fx-text-fill: -fx-text-inner-color");
							setText(item.toString());
						}
					};
				});
			}
		});
		
		// On key press and call login
		usernameField.setOnKeyPressed(e->{
			if(e.getCode()==KeyCode.ENTER)
				login();
		});
		passwordField.setOnKeyPressed(e->{
			if(e.getCode()==KeyCode.ENTER)
				login();
		});
	}

	public void switchToRoom(boolean status) {
		Platform.runLater(() -> {
			if (status) {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/gui/Room.fxml"));
				Parent parent = null;
				try {
					parent = loader.load();
				} catch (IOException e) {
					e.getMessage();
				}

				connector.setContext(loader.getController());

				Stage loginStage = (Stage) (usernameField.getScene().getWindow());

				Stage roomStage = new Stage();
				roomStage.getIcons().add(new Image(getClass().getResourceAsStream("/flat/animation.png")));
				roomStage.setScene(new Scene(parent));
				roomStage.setResizable(false);
				loginStage.close();
				roomStage.show();
			} else
				setPopup("Login failed!", "Your login information incorrect!");
		});
	}
	
	public void signupComplete(boolean status) {
		Platform.runLater(()->{
			if(status) {
				closeSignUp();
				new Notification("Success","Now! You can use your account to login!", Type.INFORMATION).show();
			}
			else {
				new Notification("Error","Your username existed!", Type.INFORMATION).show();
			}
		});
	}

	private boolean validationLogin() {
		String username = usernameField.getText();
		String password = passwordField.getText();
		if (username.trim().length() > 0 && password.trim().length() > 0)
			return true;
		return false;
	}

	private void setPopup(String title, String content) {
		new Notification(title, content, Type.WARN).setContext(this).show();
	}

	private boolean validationSignup() {
		String 	fullname = signup_name.getText().trim(),
				username = signup_username.getText().trim(),
				password = signup_password.getText().trim(),
				confirm = signup_confirm.getText().trim();
		Gender gender = signup_gender.getValue();
		boolean test1 = fullname.length()==0 || username.length()==0|| gender==null ||password.length()==0 || confirm.length()==0;
		if(test1) {
			new Notification("Less information","You need fill all the field!",Type.WARN).show();
			return false;
		}
		if(!password.equalsIgnoreCase(confirm)) {
			new Notification("Confirm password","Your confirm password incorrect!",Type.WARN).show();
			return false;
		}
		return true;
	}
	
	private void resetSignupField() {
		signup_confirm.setText("");
		signup_gender.setValue(null);
		signup_name.setText("");
		signup_password.setText("");
		signup_username.setText("");
	}
	
	private void closeSignUp() {
		TranslateTransition transition = new TranslateTransition();
		transition.setNode(signupPanel);
		transition.setByX(-450);
		transition.setDuration(Duration.millis(500));
		transition.play();
		resetSignupField();
	}
	
	private void login() {
		if (validationLogin()) {
			LoginInfo loginInfo = new LoginInfo(usernameField.getText(), passwordField.getText());
			ChatPackage chatPackage = new ChatPackage("client", SendType.LOGIN, "server", loginInfo);
			connector.sendPackage(chatPackage);
		} else {
			new Notification("Login information", "You need fill out all the field to login", Type.INFORMATION)
					.setContext(this).show();
		}
	}

	@Override
	public void focus() {
		usernameField.getScene().getWindow().requestFocus();
	}
}

class Gender {
	private String name;
	private int value;
	
	public Gender(String name, int value) {
		this.name = name;
		this.value = value;
	}
	
	public String getName() {
		return name;
	}
	public int getValue() {
		return value;
	}

	public String toString() {
		return this.name;
	}
}
