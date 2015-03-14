package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class Connect {

	private static Connect connect;

	private SSLSocket socket;
	private BufferedReader input;
	private PrintStream output;

	private Connect() throws UnknownHostException, IOException {
		SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		socket = (SSLSocket) sslSocketFactory.createSocket("kfir.ddns.net", 5555);

		final String[] enabledCipherSuites = { "SSL_DH_anon_WITH_RC4_128_MD5" };
		socket.setEnabledCipherSuites(enabledCipherSuites);

		output = new PrintStream(socket.getOutputStream());
		input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}

	public static Connect getConnection() {
		if (Connect.connect == null) {
			try {
				Connect.connect = new Connect();
			} catch (IOException e) {
				System.out.println("Unable connect server");
				return null;
			}

		}

		return connect;

	}

	public void close() {
		if (socket != null)
			if (!socket.isClosed()) {
				try {
					socket.close();
				} catch (IOException e) {
					System.out.println(e);
				}
			}
	}

	public BufferedReader getInput() {
		return input;
	}

	public PrintStream getOutput() {
		return output;
	}

}
