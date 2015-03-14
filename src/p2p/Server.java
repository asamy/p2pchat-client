package p2p;

import java.io.IOException;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import preferences.Preferences;
import utils.UtilDialogs;
import model.User;

public class Server extends Thread {

	private SSLServerSocket serverSocket = null;

	public Server() {
		boolean flag = false;
		int port = Preferences.getPreferences().getPort();

		do {
			if (flag == true || port == 0)
				port = (int) (Math.random() * 1000) + 6000;
			try {

				SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory
						.getDefault();
				this.serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(port);

				final String[] enabledCipherSuites = { "SSL_DH_anon_WITH_RC4_128_MD5" };
				this.serverSocket.setEnabledCipherSuites(enabledCipherSuites);
				server.Server.updatePort(User.getUser().getIp(), serverSocket.getLocalPort());
				flag = false;
			} catch (IOException e) {
				System.err.println("Start client server error");
				flag = true;
			}
		} while (flag);
		start();

	}

	public void run() {
		SSLSocket sslSocket = null;
		if (serverSocket.getLocalPort() != Preferences.getPreferences().getPort()
				&& Preferences.getPreferences().getPort() != 0) {
			UtilDialogs.information("There was a problem start with port : " + Preferences.getPreferences().getPort()
					+ " \nSo we allocate random port: " + serverSocket.getLocalPort());
			Preferences.getPreferences().setPort(serverSocket.getLocalPort());
		}
		while (true) {
			try {
				sslSocket = (SSLSocket) serverSocket.accept();
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}

			ServerHandler friend = new ServerHandler(sslSocket);
			new Thread(friend).start();
		}

	}

}
