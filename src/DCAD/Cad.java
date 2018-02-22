/**
 *
 * @author brom
 */

package DCAD;

import Message.Message;

public class Cad {
	static private GUI gui = new GUI(750, 600);
	private ServerConnection m_connection = null;
	private int m_port;

	public static void main(String[] args) {
		gui.addToListener();
		Cad c = new Cad();
		c.connectToServer(args[0], Integer.parseInt(args[1]));
	}

	private Cad() {

	}

	private void connectToServer(String hostName, int port) {
		m_port = port;
		m_connection = new ServerConnection(hostName, port);
		if (m_connection.handshake())
			listenForMessages();
		else
			System.err.println("Unable to connect to server");
	}

	private void listenForMessages() {
		while (true) {
			Message message = m_connection.receiveMessage();

			GObject obj = (GObject) message.getObj();

			drawObject(obj);

			// TODO do things
		}
	}

	public void drawObject(GObject obj) {
		gui.addObject(obj);
	}
}