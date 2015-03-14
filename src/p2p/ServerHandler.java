package p2p;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.image.ImageView;

import javax.net.ssl.SSLSocket;

import preferences.Preferences;
import main.StartScreenController;
import model.Friend;
import model.User;
import remote.server.Server;
import server.Message;
import server.Util;
import utils.UtilDialogs;
import chat.Chat;

public class ServerHandler implements Runnable {

	private SSLSocket socket;
	private BufferedReader input;
	private PrintStream output;
	private Message message;
	private Friend friend;

	public ServerHandler(SSLSocket socket) {
		this.socket = socket;
		this.message = new Message();
		try {
			this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.output = new PrintStream(socket.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {

		String string = null;
		if ((string = Util.readBytes(input)) == null) {
			close();
			return;
		}
		if (!message.decodeMessage(string)) {
			close();
			return;
		}

		if (message.getType() != Constants.WHOIM) {
			close();
			return;
		}

		if (openChat(message.getMessage()) == false) {
			close();
			return;
		}

		while (!socket.isClosed() && User.getUser().isLogged()) {

			if ((string = Util.readBytes(input)) == null)
				break;

			if (User.getUser().isLogged() == false)
				break;

			if (!message.decodeMessage(string))
				break;

			switch (message.getType()) {
			case Constants.MESSAGE:
				handleMessage(message.getMessage());
				break;
			case Constants.REMOTE:
				handleRemote(message.getMessage());
				break;
			case Constants.TYPING:
				handleTyping();
				break;
			default:
				System.err.println("Unkown type");
				break;
			}
		}

		close();
	}

	private void handleTyping() {
		try {
			synchronized (Chat.lockMap) {
				if (!Chat.chats.containsKey(friend.getNick()))
					Chat.chats.put(friend.getNick(), new Chat(friend));
				Chat.chats.get(friend.getNick()).typingFromFriend();
			}
		} catch (NullPointerException e) {
			System.out.println(friend == null);
			if (friend != null) {
				System.out.println(Chat.chats.containsKey(friend.getNick()));
			}
		}
	}

	private void handleRemote(String ip) {
		Thread remote = new Thread(() -> {
			Server.startRemote(ip, 5557);
		});

		Platform.runLater(() -> {
			if (UtilDialogs.friendWantRemote(friend.getNick()))
				remote.start();
		});
	}

	private void handleMessage(String message) {
		synchronized (Chat.lockMap) {
			Chat chat = Chat.chats.get(friend.getNick());
			if (chat != null)
				chat.append(message);
		}
	}

	private boolean openChat(String name) {
		if (Preferences.getPreferences().isAcceptMessageFromNotFriends()) {
			Friend anonymous = new Friend(name, new ImageView(Chat.imageUnread));

			anonymous.setOutput(output);
			anonymous.setInput(input);
			anonymous.setSocket(socket);
			synchronized (Chat.lockMap) {
				if (!Chat.chats.containsKey(anonymous.getNick()))
					Chat.chats.put(anonymous.getNick(), new Chat(anonymous));
			}
			return true;
		}
		boolean flag = false;
		ObservableList<Friend> friends = StartScreenController.getFriends();
		for (int i = 0; i < friends.size(); i++) {
			if (name.equals(friends.get(i).getNick())) {
				this.friend = friends.get(i);
				friend.setOutput(output);
				friend.setInput(input);
				friend.setSocket(socket);
				flag = true;
			}
		}
		if (flag == false)
			return false;
		synchronized (Chat.lockMap) {
			if (!Chat.chats.containsKey(friend.getNick()))
				Chat.chats.put(friend.getNick(), new Chat(friend));
		}
		return flag;
	}

	private void close() {
		try {
			if (input != null)
				input.close();
			if (output != null)
				output.close();
			if (socket != null)
				socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
