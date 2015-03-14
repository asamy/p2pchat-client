package remote.client;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.JPanel;

public class CommandsSender implements KeyListener, MouseMotionListener, MouseListener {

	private Socket socket = null;
	private JPanel cPanel = null;
	private PrintWriter writer = null;
	private Rectangle clientScreenDim = null;

	CommandsSender(Socket s, JPanel p, Rectangle r) {
		socket = s;
		cPanel = p;
		clientScreenDim = r;

		cPanel.addKeyListener(this);
		cPanel.addMouseListener(this);
		cPanel.addMouseMotionListener(this);
		try {

			writer = new PrintWriter(socket.getOutputStream());
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

	public void mouseDragged(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
		double xScale = clientScreenDim.getWidth() / cPanel.getWidth();
		double yScale = clientScreenDim.getHeight() / cPanel.getHeight();
		writer.println(EnumCommands.MOVE_MOUSE.getAbbrev());
		writer.println((int) (e.getX() * xScale));
		writer.println((int) (e.getY() * yScale));
		writer.flush();
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		writer.println(EnumCommands.PRESS_MOUSE.getAbbrev());
		int button = e.getButton();
		int xButton = 16;
		if (button == 3) {
			xButton = 4;
		}
		writer.println(xButton);
		writer.flush();
	}

	public void mouseReleased(MouseEvent e) {
		writer.println(EnumCommands.RELEASE_MOUSE.getAbbrev());
		int button = e.getButton();
		int xButton = 16;
		if (button == 3) {
			xButton = 4;
		}
		writer.println(xButton);
		writer.flush();
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {

	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
		writer.println(EnumCommands.PRESS_KEY.getAbbrev());
		writer.println(e.getKeyCode());
		writer.flush();
	}

	public void keyReleased(KeyEvent e) {
		writer.println(EnumCommands.RELEASE_KEY.getAbbrev());
		writer.println(e.getKeyCode());
		writer.flush();
	}

}
