package chat;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.Friend;

import org.controlsfx.control.Notifications;

import utils.UtilMusic;

public class Chat {

	private volatile Stage stage;
	private Friend friend;
	private PrintStream output;
	private volatile ChatController chatController;
	public static Map<String, Chat> chats = new HashMap<>();
	public static final Map<String, Chat> lockMap = Collections.synchronizedMap(chats);

	public static final Image imageUnread = new Image("file:resources/newmsg.png");
	private Image imageOn = new Image("file:resources/on.png");

	public Chat(Friend friend) {
		this.friend = friend;
		Platform.runLater(() -> {
			try {
				start(new Stage());
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public void start(Stage chatStage) throws Exception {
		this.stage = chatStage;

		stage.setTitle(friend.getNick());
		stage.setOnCloseRequest((WindowEvent event) -> {
			stage.hide();
		});

		FXMLLoader loader = new FXMLLoader();
		InputStream in = Chat.class.getResourceAsStream("Chat.fxml");
		loader.setBuilderFactory(new JavaFXBuilderFactory());
		loader.setLocation(Chat.class.getResource("Chat.fxml"));
		AnchorPane page;
		try {
			page = (AnchorPane) loader.load(in);
		} finally {
			in.close();
		}

		chatController = (ChatController) loader.getController();

		chatController.setFriend(this.friend);

		Scene scene = new Scene(page);
		scene.setCursor(new ImageCursor(new Image("file:resources/curser.gif")));
		scene.getStylesheets().add(Chat.class.getResource("Chat.css").toExternalForm());
		this.stage.getIcons().add(new Image("file:resources/message-unread.png"));
		stage.setScene(scene);
		stage.sizeToScene();
	}

	public void show() {
		Platform.runLater(() -> {
			stage.show();
		});
	}

	public void append(String msg) {
		new Thread(() -> {
			while (chatController == null || stage == null) {
			}

			if (!stage.isShowing() || stage.isIconified()) {
				UtilMusic.play(UtilMusic.message);
				friend.setImage(imageUnread);
				String msg2 = msg;
				if (msg.length() > 35)
					msg2 = msg2.substring(0, 34) + "...";
				showNotification(msg2);
				Platform.runLater(() -> {
					if (!stage.isIconified() && !main.Main.getStage().isShowing()) {
						stage.show();
						stage.setIconified(true);
					}
				});
			} else {
				UtilMusic.play(UtilMusic.messageWindowOn);
			}
			chatController.appendMessage(friend.getNick() + ": " + msg);

		}).start();
	}

	public PrintStream getOutput() {
		return this.output;
	}

	public Stage getStage() {
		return stage;
	}

	public void showNotification(String msg) {
		Platform.runLater(() -> {
			try {
				Notifications.create().title(friend.getNick()).text(msg).onAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						stage.show();
						stage.setIconified(false);
						friend.setImage(imageOn);
					}
				}).show();
			} catch (Exception e) {
			}
		});
	}

	public void typingFromFriend() {
		while (chatController == null) {
		}
		chatController.typingFromFriend();
	}

}
