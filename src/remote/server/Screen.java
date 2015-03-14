package remote.server;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javax.swing.ImageIcon;

public class Screen extends Thread {

	private Socket socket = null;
	private Robot robot = null;
	private Rectangle rectangle = null;
	private boolean runnning = true;

	public Screen(Socket socket, Robot robot, Rectangle rect) {
		this.socket = socket;
		this.robot = robot;
		rectangle = rect;
		start();
	}

	public void run() {
		ObjectOutputStream output = null;

		try {

			output = new ObjectOutputStream(socket.getOutputStream());
			output.writeObject(rectangle);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		while (runnning) {
			BufferedImage image = robot.createScreenCapture(rectangle);
			ImageIcon imageIcon = new ImageIcon(image);

			try {
				output.writeObject(imageIcon);
				output.reset();
			} catch (IOException ex) {
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	public void stopRunning() {
		this.runnning = false;
	}

}
