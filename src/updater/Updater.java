package updater;

import java.io.InputStream;
import java.net.URL;

import utils.UtilDialogs;

public class Updater {
	private final static String versionURL = "http://globnet.co.il/updater/chat/version.html";
	private final static String historyURL = "http://globnet.co.il/updater/chat/overview.html";
	public final static String downloadURL = "http://globnet.co.il/updater/chat/url.html";
	public static final double VERSION = 0.75;

	public static String getLatestVersion() throws Exception {
		String data = getData(versionURL);
		return data.substring(data.indexOf("[version]") + 9, data.indexOf("[/version]"));
	}

	public static String getWhatsNew() throws Exception {
		String data = getData(historyURL);
		return data.substring(data.indexOf("[overview]") + 10, data.indexOf("[/overview]"));
	}

	private static String getData(String address) throws Exception {
		URL url = new URL(address);
		InputStream html = null;
		html = url.openStream();
		int c = 0;
		StringBuffer buffer = new StringBuffer("");
		while (c != -1) {
			c = html.read();
			buffer.append((char) c);
		}
		return buffer.toString();
	}

	public static void startupCheckUpdate() {
		new Thread(() -> {
			try {
				if (Double.parseDouble(Updater.getLatestVersion()) > VERSION)
					new WhatsNew(Updater.getWhatsNew());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}).start();
	}

	public static void checkUpdate() {
		new Thread(() -> {
			try {
				if (Double.parseDouble(Updater.getLatestVersion()) > VERSION)
					new WhatsNew(Updater.getWhatsNew());
				else
					UtilDialogs.versionUpToDate();
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}).start();
	}
}