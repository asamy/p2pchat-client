package p2p;

import java.io.IOException;

import javafx.application.Platform;
import model.Friend;
import model.User;
import remote.server.Server;
import server.Message;
import server.Util;
import utils.UtilDialogs;
import chat.Chat;

public class ClientHandler extends Thread {

	private Friend friend;
	private Message message = new Message();

	public ClientHandler(Friend friend) {
		this.friend = friend;
	}

	public void run() {
		String string = null;

		while (friend.isConnected() && User.getUser().isLogged()) {

			if ((string = Util.readBytes(friend.getInput())) == null)
				break;

			if (!friend.isConnected())
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
			if (UtilDialogs.friendWantRemote(friend.getNick())) {
				remote.start();
			}
			;
		});

	}

	private void handleMessage(String message) {
		synchronized (Chat.lockMap) {
			if (!Chat.chats.containsKey(friend.getNick()))
				Chat.chats.put(friend.getNick(), new Chat(friend));

			Chat.chats.get(friend.getNick()).append(message);
		}
	}

	private void close() {
		try {
			if (friend.getInput() != null)
				friend.getInput().close();
			if (friend.getOutput() != null)
				friend.getOutput().close();
			if (friend.getSocket() != null)
				friend.getSocket().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
