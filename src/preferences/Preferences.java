package preferences;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javafx.scene.text.Font;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class Preferences {

	private static final String filename = "data/Preferences.xml";
	private static Preferences intance;

	private int port;
	private int remotePort;
	private String user = "";
	private String password;
	private boolean acceptMessageFromNotFriends;
	private boolean trayIcon = true;
	private Font font;

	public static Preferences getPreferences() {
		if (intance == null) {
			try {
				intance = new Preferences();
			} catch (JDOMException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return intance;

	}

	private Preferences() throws JDOMException, IOException, Exception {
		File file = new File(filename);
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(file);
		Element root = doc.getRootElement();
		List<Element> elements = root.getChildren("account");

		setPort(Integer.parseInt(elements.get(0).getChildText("port")));
		setUser(elements.get(0).getChildText("user"));
		setRemotePort(Integer.parseInt(elements.get(0).getChildText("remoteport")));
		setAcceptMessageFromNotFriends(Boolean.parseBoolean(elements.get(0).getChildText("acceptmessage")));
		setTrayIcon(Boolean.parseBoolean(elements.get(0).getChildText("trayicon")));
		// setFont(new Font(elements.get(0).getChildText("font"),14));

	}

	public void savePreferences() throws Exception {
		Document doc = new Document();
		Element root = new Element("accounts");
		doc.addContent(root);

		Element element = new Element("account");

		root.addContent(element);

		addChildElement(element, "user", getUser());
		addChildElement(element, "port", String.valueOf(getPort()));
		addChildElement(element, "remoteport", String.valueOf(getRemotePort()));
		addChildElement(element, "trayicon", String.valueOf(isTrayIcon()));
		addChildElement(element, "acceptmessage", String.valueOf(isAcceptMessageFromNotFriends()));

		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());

		FileWriter writer = new FileWriter(new File(filename));
		outputter.output(doc, writer);

	}

	private Element addChildElement(Element parent, String elementName, String textValue) {
		Element child = new Element(elementName);
		child.setText(textValue);
		parent.addContent(child);
		return child;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getRemotePort() {
		return remotePort;
	}

	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isAcceptMessageFromNotFriends() {
		return acceptMessageFromNotFriends;
	}

	public void setAcceptMessageFromNotFriends(boolean acceptMessageFromNotFriends) {
		this.acceptMessageFromNotFriends = acceptMessageFromNotFriends;
	}

	public boolean isTrayIcon() {
		return trayIcon;
	}

	public void setTrayIcon(boolean trayIcon) {
		this.trayIcon = trayIcon;
	}

	public Font getFont() {
		return font;
	}

	public void setFont(Font font) {
		this.font = font;
	}

}
