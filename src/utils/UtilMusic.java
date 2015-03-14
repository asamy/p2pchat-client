package utils;

import java.io.File;
import java.net.MalformedURLException;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class UtilMusic {

	private static final String messagePath = "resources/Message.mp3";
	private static final String connectionPath = "resources/Connection.mp3";
	private static final String messageWindowOnPath = "resources/MessageWindowOn.mp3";
	private static final String newFriendRequestPath = "resources/newRequest.mp3";

	public static final int message = 1, connection = 2, messageWindowOn = 3, newFriendRequest = 4;

	public static void play(int choice) {
		new Thread(() -> {
			String path = null;

			switch (choice) {
			case message:
				path = messagePath;
				break;
			case connection:
				path = connectionPath;
				break;
			case messageWindowOn:
				path = messageWindowOnPath;
				break;
			case newFriendRequest:
				path = newFriendRequestPath;
				break;
			}
			String workingDir = System.getProperty("user.dir");
			Media media = null;
			try {
				media = new Media(new File(workingDir + "/" + path).toURI().toURL().toExternalForm());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}

			MediaPlayer mediaPlayer = new MediaPlayer(media);
			mediaPlayer.play();
		}).start();
	}
}
