package main;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import javax.imageio.ImageIO;

import preferences.Preferences;
import model.User;
import updater.Updater;
import utils.UtilDialogs;

public class Main extends Application {

	private static Stage stage;

	private boolean firstTime = true;
	private TrayIcon trayIcon;
	private SystemTray tray;

	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler((Thread t, Throwable e) -> {
			UtilDialogs.exception(e);
		});
		if (Preferences.getPreferences() == null) {
			System.out.println("error read xml");
		}
		Updater.startupCheckUpdate();
		Application.launch(Main.class, (java.lang.String[]) null);
	}

	@Override
	public void start(Stage primaryStage) {

		try {
			stage = primaryStage;
			stage.setTitle("Chat");
			stage.setMinHeight(400);
			stage.setMinWidth(400);

			stage.getIcons().add(new Image("file:resources/icon.png"));
			gotoLogin();

			if (Preferences.getPreferences().isTrayIcon())
				createTrayIcon();
			else {
				stage.setOnCloseRequest((WindowEvent t) -> {
					System.exit(0);
				});
			}

			stage.show();
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	public static Stage getStage() {
		return stage;

	}

	private void createTrayIcon() {
		if (SystemTray.isSupported()) {
			Platform.setImplicitExit(false);
			tray = SystemTray.getSystemTray();

			java.awt.Image image = null;
			try {
				image = ImageIO.read(new URL("file:resources/on.png"));
			} catch (IOException ex) {
				System.out.println(ex);
				System.exit(1);
			}

			stage.setOnCloseRequest((WindowEvent t) -> {
				hide();
			});

			PopupMenu popup = new PopupMenu();

			MenuItem showItem = new MenuItem("Show");
			showItem.addActionListener((ActionEvent e) -> {
				show();
			});

			MenuItem closeItem = new MenuItem("Close");
			closeItem.addActionListener((ActionEvent e) -> {
				System.exit(0);
			});

			popup.add(showItem);
			popup.add(closeItem);

			trayIcon = new TrayIcon(image, "P2P Chat", popup);
			trayIcon.addActionListener((ActionEvent e) -> {
				show();
			});

		}

	}

	public void showProgramIsMinimizedMsg() {
		if (firstTime) {
			trayIcon.displayMessage("Online", "Window still open", TrayIcon.MessageType.INFO);
			firstTime = false;
		}
	}

	private void show() {
		Platform.runLater(() -> {
			stage.show();
		});
		tray.remove(trayIcon);
	}

	private void hide() {
		if (User.getUser().isLogged()) {
			Platform.runLater(() -> {
				stage.hide();
			});
			try {
				tray.add(trayIcon);
			} catch (AWTException e) {
				e.printStackTrace();
			}
			showProgramIsMinimizedMsg();
		} else {
			System.exit(0);
		}
	}

	public void gotoRegister() {
		try {
			RegisterController register = (RegisterController) replaceSceneContent("Register.fxml");
			register.setApp(this);
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	public void gotoLogin() {
		try {
			LoginController login = (LoginController) replaceSceneContent("Login.fxml");
			login.setApp(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void gotoStartScreen() {
		try {
			StartScreenController startScreen = (StartScreenController) replaceSceneContent("StartScreen.fxml");
			startScreen.setApp(this);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Initializable replaceSceneContent(String fxml) throws Exception {
		FXMLLoader loader = new FXMLLoader();
		InputStream in = Main.class.getResourceAsStream(fxml);
		loader.setBuilderFactory(new JavaFXBuilderFactory());
		loader.setLocation(Main.class.getResource(fxml));
		AnchorPane page;
		try {
			page = (AnchorPane) loader.load(in);
		} finally {
			in.close();
		}
		int duration = 600;
		if (fxml.contains("StartScreen"))
			duration = 1000;

		FadeTransition ft = new FadeTransition(Duration.millis(duration), page);
		ft.setFromValue(0.1);
		ft.setToValue(1.0);
		ft.play();

		Scene scene = new Scene(page);
		scene.setCursor(new ImageCursor(new Image("file:resources/curser.gif")));
		stage.setScene(scene);
		stage.sizeToScene();
		return (Initializable) loader.getController();
	}

}
