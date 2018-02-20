/**
 *
 * @author brom
 */

package DCAD;

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
		int i=0;
		m_connection = new ServerConnection(hostName, port);
		m_connection.handshake();
	}
}