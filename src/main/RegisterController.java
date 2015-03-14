package main;

import java.net.URL;
import java.util.ResourceBundle;

import server.Connect;
import server.Server;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

public class RegisterController extends AnchorPane implements Initializable {

	@FXML
	private TextField user;
	@FXML
	private PasswordField password;
	@FXML
	private TextField email;
	@FXML
	private TextArea about;
	@FXML
	private CheckBox agreement;
	@FXML
	private Hyperlink back;
	@FXML
	private Button register;
	@FXML
	private Label success;

	private Main application;

	public void setApp(Main application) {
		this.application = application;
		success.setOpacity(0);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	@FXML
	public void processBack(ActionEvent event) {
		application.gotoLogin();
	}

	@FXML
	public void processRegister(ActionEvent event) {
		new Thread(
				() -> {
					if (user.equals("") || password.equals("") || agreement.isSelected() == false
							|| email.getText().equals(""))
						updateMessage("Some fields are missing");
					else if (!email.getText().matches("[A-Za-z0-9._%-]+@[A-Za-z0-9._-]+\\.[A-Za-z]{2,4}"))
						updateMessage("Email missmatch requirements");
					else if (!(user.getText() + " " + password.getText()).matches("^[a-zA-Z0-9]+([ ][a-zA-Z0-9]+)?$"))
						updateMessage("User name/Password missmatch requirements");
					else if (Connect.getConnection() == null)
						updateMessage("Server Offline");
					else if (!Server.validateRegister(user.getText(), password.getText(), email.getText())) {
						updateMessage("Registration failed! User already exists");
					} else {
						updateMessage(user.getText() + " Registered successfully!");
					}
				}).start();
	}

	@FXML
	public void processReset(ActionEvent event) {
		resetFields();
	}

	private void updateMessage(String msg) {
		Platform.runLater(() -> {
			success.setText(msg);
			animateMessage();
			if (success.getText().contains("success"))
				resetFields();
		});

	}

	private void resetFields() {
		user.clear();
		password.clear();
		email.clear();
		about.clear();
		agreement.setSelected(false);

	}

	private void animateMessage() {
		FadeTransition ft = new FadeTransition(Duration.millis(1000), success);
		ft.setFromValue(0.0);
		ft.setToValue(1);
		ft.play();
	}
}
