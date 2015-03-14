package main;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import model.User;
import preferences.Preferences;
import server.Connect;
import server.Server;
import updater.Updater;
import utils.UtilMusic;

public class LoginController extends AnchorPane implements Initializable {

	@FXML
	private TextField user;
	@FXML
	private PasswordField password;
	@FXML
	private Button login;
	@FXML
	private Button register;
	@FXML
	private Label errorMessage;
	@FXML
	private HBox boxLabel;
	@FXML
	private Label version;

	private ProgressBar p3 = new ProgressBar();

	private Main application;

	public void setApp(Main application) {
		this.application = application;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		errorMessage.setOpacity(0.0);
		errorMessage.setText("");
		if (Preferences.getPreferences() == null)
			user.setText("");
		else
			user.setText(Preferences.getPreferences().getUser());
		password.setText("");
		user.setPromptText("username");
		password.setPromptText("password");
		version.setText("V" + Updater.VERSION);
		p3.setPrefWidth(100);
		p3.setLayoutY(30);

	}

	@FXML
	public void processLogin(ActionEvent event) {
		errorMessage.setOpacity(0.0);
		errorMessage.setText("");
		if (!boxLabel.getChildren().contains(p3))
			boxLabel.getChildren().add(p3);
		Thread worker = new Thread(() -> {

			if (user.getText().isEmpty() || password.getText().isEmpty())
				updateErrorMessage("Some fields are missing");
			else if (!(user.getText() + " " + password.getText()).matches("^[a-zA-Z0-9]+([ ][a-zA-Z0-9]+)?$"))
				updateErrorMessage("User name/Password missmatch requirements");
			else if (Connect.getConnection() == null)
				updateErrorMessage("Server Offline");
			else if (!Server.validateLogin(user.getText(), password.getText()))
				updateErrorMessage("Username/Password is incorrect");
			else
				userLoginSuccess();

		});
		worker.start();

	}

	@FXML
	public void processRegister(ActionEvent event) {
		application.gotoRegister();
	}

	private void updateErrorMessage(String str) {
		Platform.runLater(() -> {
			errorMessage.setText(str);
			animateMessage();
		});
	}

	private void userLoginSuccess() {
		User.getUser().setName(user.getText());
		User.getUser().setPassword(password.getText());
		User.getUser().updateIp();
		new p2p.Server();
		UtilMusic.play(UtilMusic.connection);
		Platform.runLater(() -> {
			application.gotoStartScreen();
		});
		User.getUser().setLogged(true);
	}

	private void animateMessage() {
		boxLabel.getChildren().remove(p3);
		FadeTransition ft = new FadeTransition(Duration.millis(1000), errorMessage);
		ft.setFromValue(0.0);
		ft.setToValue(1);
		ft.play();
	}
}
