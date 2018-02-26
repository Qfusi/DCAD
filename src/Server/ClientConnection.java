
package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ClientConnection {

	private final String m_name;
	private final InetAddress m_address;
	private final int m_port;
	private boolean activeStatus=true;

	public ClientConnection(String name, InetAddress address, int port) {
		m_name = name;
		m_address = address;
		m_port = port;
		activeStatus=true;
	}

	public void sendMessage(String message, DatagramSocket socket,int k) {

			byte[] x= (message).getBytes();
			DatagramPacket dp= new DatagramPacket(x, x.length, m_address, m_port);
			try {
				socket.send(dp);

			} catch (IOException e) {
				
				System.err.println("Something wrong with sending first message.");
			}

			//TODO chilla som faen xD
	}

	public boolean hasName(String testName) {

		return testName.equals(m_name);
	}
	public String getName() {
		return m_name;
	}
	public InetAddress getAddress() {
		return m_address;
	}
	public int getPort() {
		return m_port;
	}
	public void setActiveStatus(boolean newStatus) {
		activeStatus=newStatus;
	}
	public boolean getActiveStatus() {
		return activeStatus;
	}
}
