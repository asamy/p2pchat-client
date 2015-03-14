package remote.client;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;

public class Handler extends Thread {

	private JDesktopPane desktop = null;
	private Socket socket = null;
	private JInternalFrame interFrame = new JInternalFrame("Client Screen", true, true, true);
	private JPanel cPanel = new JPanel();
	private ScreenReciever clientReciever;

	public Handler(Socket socket, JDesktopPane desktop) {
		this.socket = socket;
		this.desktop = desktop;
		start();
	}

	public void drawGUI() {
		interFrame.setLayout(new BorderLayout());
		interFrame.getContentPane().add(cPanel, BorderLayout.CENTER);
		interFrame.setSize(100, 100);
		desktop.add(interFrame);
		try {

			interFrame.setMaximum(true);
		} catch (PropertyVetoException ex) {
			ex.printStackTrace();
		}

		cPanel.setFocusable(true);
		interFrame.setVisible(true);
	}

	public void run() {
		Rectangle clientScreenDim = null;
		ObjectInputStream input = null;
		drawGUI();

		try {
			input = new ObjectInputStream(socket.getInputStream());
			clientScreenDim = (Rectangle) input.readObject();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		clientReciever = new ScreenReciever(input, cPanel);
		new CommandsSender(socket, cPanel, clientScreenDim);
	}

	public void stopRunning() {
		clientReciever.stopRunning();
	}

}
