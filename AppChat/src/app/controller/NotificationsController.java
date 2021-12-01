package app.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class NotificationsController implements Initializable {

	@FXML
	private Text FXtitle;

	@FXML
	private Label FXcontent;

	@FXML
	private ImageView FXicon;

	@FXML
	private void closeNotify(MouseEvent event) {
		closeHandle(event);
	}
	
	@FXML
	private void requestFocus(MouseEvent event) {
		if(controller!=null)
			controller.focus();
		closeHandle(event);
	}
	
	private String title, content;
	private Image image;
	
	private Controller controller;
	private void closeHandle(MouseEvent event) {
		((Stage) FXtitle.getScene().getWindow()).close();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Platform.runLater(() -> {
			FXtitle.setText(title);
			FXcontent.setText(content);
			FXicon.setImage(image);
		});
	}

	public void getData(String title, String content, int type) {
		this.title = title;
		this.content = content;
		String url = null;
		switch (type) {
		case 1:
			url = getClass().getResource("/flat/notify_warning.png").toString();
			break;
		case 2:
			url = getClass().getResource("/flat/notify_error.png").toString();
			break;
		case 3:
			url = getClass().getResource("/flat/notify_message.png").toString();
			break;
		case 4:
			url = getClass().getResource("/flat/notify_file.png").toString();
			break;
		case 5:
			url = getClass().getResource("/flat/notify_image.png").toString();
			break;
		default:
			url = getClass().getResource("/flat/notify_info.png").toString();
		}
		image = new Image(url);
	}
	
	public void setContext(Controller controller) {
		this.controller = controller;
	}
}
