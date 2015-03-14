package server;

import model.User;

public class Server {

	private static Connect connect = Connect.getConnection();
	private static Message message = new Message();
	private static volatile User user = User.getUser();
	private static final Object lock = new Object();

	public static boolean validateLogin(String user, String password) {
		synchronized (lock) {
			message.setMessage(Constants.LOGIN, user + " " + password);
			Util.sendBytes(message.toString(), connect.getOutput());
			message.decodeMessage(Util.readBytes(connect.getInput()));

			if (message.getType() == Constants.LOGINSUCCESS)
				return true;
			else
				return false;

		}
	}

	public static void logout() {
		synchronized (lock) {

			message.setMessage(Constants.LOGOUT, "");
			Util.sendBytes(message.toString(), connect.getOutput());
		}
	}

	public static String[] getBlockList() {
		synchronized (lock) {

			message.setMessage(Constants.BLOCKLIST, user.getName() + " " + user.getPassword());
			Util.sendBytes(message.toString(), connect.getOutput());
			message.decodeMessage(Util.readBytes(connect.getInput()));
			String[] blocklist = message.getMessage().split(" ");

			if (blocklist[0].equals(""))
				return new String[0];
			return blocklist;
		}
	}

	public static void addBlock(String block) {
		synchronized (lock) {

			message.setMessage(Constants.ADDBLOCK, user.getName() + " " + user.getPassword() + " " + block);
			Util.sendBytes(message.toString(), connect.getOutput());
		}
	}

	public static void removeBlock(String unblock) {
		synchronized (lock) {

			message.setMessage(Constants.REMOVEBLOCK, user.getName() + " " + user.getPassword() + " " + unblock);
			Util.sendBytes(message.toString(), connect.getOutput());
		}

	}

	public static String[] getFriends() {
		synchronized (lock) {

			message.setMessage(Constants.FRIENDSLIST, user.getName() + " " + user.getPassword());
			Util.sendBytes(message.toString(), connect.getOutput());
			message.decodeMessage(Util.readBytes(connect.getInput()));
			String[] friends = message.getMessage().split(" ");

			if (friends[0].equals(""))
				return new String[0];
			return friends;
		}
	}

	public static String[] getRequests() {
		synchronized (lock) {

			message.setMessage(Constants.FRIENDREQUEST, "");
			Util.sendBytes(message.toString(), connect.getOutput());
			message.decodeMessage(Util.readBytes(connect.getInput()));
			String[] requests = message.getMessage().split(" ");

			if (requests[0].equals(""))
				return new String[0];
			return requests;
		}
	}

	public static String[] whoIsOnline() {
		synchronized (lock) {

			message.setMessage(Constants.WHONLINE, "");
			Util.sendBytes(message.toString(), connect.getOutput());
			message.decodeMessage(Util.readBytes(connect.getInput()));
			String[] online = message.getMessage().split(" ");

			if (online[0].equals(""))
				return new String[0];
			return online;
		}
	}

	public static boolean addFriend(String friend) {
		synchronized (lock) {

			message.setMessage(Constants.ADDFRIEND, user.getName() + " " + user.getPassword() + " " + friend);
			Util.sendBytes(message.toString(), connect.getOutput());
			message.decodeMessage(Util.readBytes(connect.getInput()));
			if (message.getType() == Constants.ADDFRIENDOK)
				return true;
			else
				return false;
		}
	}

	public static boolean searchFriend(String search) {
		synchronized (lock) {

			message.setMessage(Constants.SEARCH, user.getName() + " " + user.getPassword() + " " + search);
			Util.sendBytes(message.toString(), connect.getOutput());
			message.decodeMessage(Util.readBytes(connect.getInput()));
			if (message.getType() == Constants.FOUND)
				return true;
			else
				return false;
		}
	}

	public static boolean validateRegister(String user, String password, String email) {
		synchronized (lock) {
			message.setMessage(Constants.REGISTER, user + " " + password + " " + email);
			Util.sendBytes(message.toString(), connect.getOutput());
			message.decodeMessage(Util.readBytes(connect.getInput()));

			if (message.getType() == Constants.REGISTERSUCCESS)
				return true;
			else
				return false;

		}
	}

	public static void deleteFriend(String friend) {
		synchronized (lock) {
			message.setMessage(Constants.DELETEFRIEND, user.getName() + " " + user.getPassword() + " " + friend);
			Util.sendBytes(message.toString(), connect.getOutput());

		}

	}

	public static String[] getIp(String nick) {
		synchronized (lock) {
			message.setMessage(Constants.REQUESTUSERIP, user.getName() + " " + user.getPassword() + " " + nick);
			Util.sendBytes(message.toString(), connect.getOutput());
			message.decodeMessage(Util.readBytes(connect.getInput()));
			if (message.getType() == Constants.REQUESTIPACCEPTED) {
				String[] address = message.getMessage().split(" ");
				String ip = address[1];
				String port = address[2];
				return new String[] { ip, port };
			}

			return new String[0];
		}

	}

	public static void updatePort(String ip, int port) {
		synchronized (lock) {

			message.setMessage(Constants.UPDATEPORT, user.getName() + " " + user.getPassword() + " " + ip + " " + port);
			Util.sendBytes(message.toString(), connect.getOutput());
		}
	}

}