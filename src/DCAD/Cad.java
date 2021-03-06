/**
 *
 * @author brom
 */

package DCAD;

import Message.DrawMessage;
import Message.ConnectMessage;
import Message.Message;
import Message.RemoveMessage;

//central unit of the client side, creates GUI and connection to server. Acts as a connection between the two as well. 

public class Cad {
	static private GUI gui = new GUI(750, 600);
	private ServerConnection m_connection = null;

	public static void main(String[] args) {
		gui.addToListener();
		Cad c = new Cad();
		c.connectToServer(args[0], Integer.parseInt(args[1]));
	}

	private Cad() {	
	}
	private void connectToServer(String hostName, int port) {
		m_connection = new ServerConnection(hostName, port);
		gui.passSC(m_connection);

		//with handshaking
		if (m_connection.handshake(gui))
			listenForMessages();
		else
			System.err.println("Unable to connect to server");

	}

	//calls serverconnection to get message 
	private void listenForMessages() {
		while (true) {
			Message message = m_connection.receiveMessage();

			if (message != null) {
				if (message instanceof ConnectMessage) {
					gui.reDrawEverything(((ConnectMessage) message).getList());
				} else if (message instanceof DrawMessage) {
					GObject obj = (GObject) message.getObj();
					drawObject(obj);
				} else if (message instanceof RemoveMessage) {
					removeObject((GObject) message.getObj());
				}
			}
		}
	}
	//connecting GUI and ServerConnection
	private void drawObject(GObject obj) {
		gui.addObject(obj);
	}
	//connecting GUI and ServerConnection
	private void removeObject(GObject obj) {
		gui.removeObject(obj);
	}

}