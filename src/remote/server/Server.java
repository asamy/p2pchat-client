package remote.server;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;

public class Server {

	private Socket socket = null;

	private Screen spy;
	private Delegate delegate;

	public static void startRemote(String ip, int port) {
		new Server().initialize(ip, port);
	}

	public void initialize(String ip, int port) {

		Robot robot = null;
		Rectangle rectangle = null;

		try {
			socket = new Socket(ip, port);

			GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice gDev = gEnv.getDefaultScreenDevice();

			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			rectangle = new Rectangle(dim);
			robot = new Robot(gDev);
			drawGUI();
			spy = new Screen(socket, robot, rectangle);
			delegate = new Delegate(socket, robot);
		} catch (UnknownHostException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (AWTException ex) {
			ex.printStackTrace();
		}
	}

	private void drawGUI() {
		JFrame frame = new JFrame("Remote");
		JButton button = new JButton("Terminate");

		frame.setBounds(100, 100, 150, 150);
		frame.add(button);
		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				spy.stopRunning();
				delegate.stopRunning();
				frame.dispose();
				close();
			}
		});
		frame.setVisible(true);
	}

	protected void close() {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}
