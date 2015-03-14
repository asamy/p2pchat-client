package chat;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.util.Duration;
import model.Friend;
import p2p.ClientHandler;
import p2p.Constants;
import server.Message;
import server.Server;
import server.Util;
import utils.UtilDialogs;

public class ChatController extends AnchorPane implements Initializable {

	@FXML
	private TextArea textArea;
	@FXML
	private Button send;
	@FXML
	private TextField text;
	@FXML
	private Button font;
	@FXML
	private Label typing;

	private Object lock = new Object();
	private volatile int count = 0;

	private Friend friend;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		text.setOnKeyPressed((KeyEvent keyEvent) -> {
			if (keyEvent.getCode() == KeyCode.ENTER) {
				setPressed(keyEvent.getEventType() == KeyEvent.KEY_PRESSED);
				handleSend(null);
				keyEvent.consume();
			} else if (keyEvent.getCode() != KeyCode.BACK_SPACE) {
				typing();
			}
		});
		typing.setText("Friend is typing...");
		typing.setOpacity(0);
		fadein();
		fadeout();

	}

	private void typing() {
		new Thread(() -> {
			if (friend.isConnected()) {
				Message message = new Message();
				message.setMessage(Constants.TYPING, "");
				Util.sendBytes(message.toString(), friend.getOutput());
			}
		}).start();
	}

	public void typingFromFriend() {
		if (count == 0)
			count = 1;
	}

	public void fadein() {
		Thread fadein = new Thread(() -> {
			while (true) {
				synchronized (lock) {
					while (count == 1) {
						Platform.runLater(() -> {
							FadeTransition ft = new FadeTransition(Duration.millis(1000), typing);
							ft.setToValue(1);
							ft.play();
						});
						count = 0;
						try {
							Thread.sleep(200);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
		fadein.setPriority(1);
		fadein.start();
	}

	public void fadeout() {
		Thread fadeout = new Thread(() -> {
			while (true) {
				synchronized (lock) {
					Platform.runLater(() -> {
						FadeTransition ft = new FadeTransition(Duration.millis(500), typing);
						ft.setToValue(0.0);
						ft.setAutoReverse(true);
						ft.play();
					});
				}
				try {
					Thread.sleep(200);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		fadeout.setPriority(1);
		fadeout.start();
	}

	public void setFriend(Friend setFriend) {
		this.friend = setFriend;
	}

	public void appendMessage(String msg) {
		Platform.runLater(() -> {
			textArea.appendText(msg + "\n");
		});
	}

	@FXML
	public void handleSend(ActionEvent event) {
		new Thread(() -> {
			if (text.getText().equals(""))
				return;
			if (startConnectFriend(friend) == false) {
				appendMessage("User has been disconnected!");
				return;
			}

			Platform.runLater(() -> {
				appendMessage(text.getText());
				text.requestFocus();
				text.clear();
			});
			Message message = new Message();
			message.setMessage(Constants.MESSAGE, text.getText());
			Util.sendBytes(message.toString(), friend.getOutput());
		}).start();
	}

	public void fontChooser() {
		Font curFont = textArea.getFont();
		Font font = UtilDialogs.font(curFont);

		if (font != null)
			textArea.setFont(font);

	}

	private boolean startConnectFriend(Friend friend) {
		if (!friend.isConnected()) {
			String[] address = Server.getIp(friend.getNick());
			if (address.length == 0) {
				return false;
			} else {
				if (friend.startConnection(address[0], address[1])) {
					new ClientHandler(friend).start();
					return true;
				} else {
					return false;
				}
			}
		}
		return true;
	}

}
