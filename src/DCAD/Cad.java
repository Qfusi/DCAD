/**
 *
 * @author brom
 */

package DCAD;

import Message.DrawMessage;
import Message.ConnectMessage;
import Message.DisconnectMessage;
import Message.Message;
import Message.RemoveMessage;

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
		
		//without handshaking
		//m_connection.sendMessage(new JoinMessage());
		//listenForMessages();
	}

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
					removeObject();
				}
			}
		}
	}
	private void drawObject(GObject obj) {
		gui.addObject(obj);
	}
	
	private void removeObject() {
		gui.removeObject();
	}
	
}