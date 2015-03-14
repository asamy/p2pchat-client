package remote.client;

import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;
import java.io.ObjectInputStream;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class ScreenReciever extends Thread {

	private ObjectInputStream input = null;
	private JPanel cPanel = null;
	public boolean running = true;

	public ScreenReciever(ObjectInputStream ois, JPanel p) {
		input = ois;
		cPanel = p;

		start();
	}

	public void run() {

		try {

			while (running) {

				ImageIcon imageIcon = (ImageIcon) input.readObject();
				Image image = imageIcon.getImage();
				image = image.getScaledInstance(cPanel.getWidth(), cPanel.getHeight(), Image.SCALE_FAST);

				Graphics graphics = cPanel.getGraphics();
				graphics.drawImage(image, 0, 0, cPanel.getWidth(), cPanel.getHeight(), cPanel);
			}
		} catch (IOException ex) {
			// ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			// ex.printStackTrace();
		}
	}

	public void stopRunning() {
		running = false;
	}
}
