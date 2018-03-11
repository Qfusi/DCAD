package Server;

import java.net.InetAddress;

//used to saves information of client
public class ClientConnection {
	private  InetAddress m_address;
	private  int m_port;
	private boolean m_activeStatus;

	public ClientConnection(InetAddress address, int port) {
		setAddress(address);
		setPort(port);
		m_activeStatus = true;
	}

	public InetAddress getAddress() {
		return m_address;
	}

	public void setAddress(InetAddress m_address) {
		this.m_address = m_address;
	}

	public int getPort() {
		return m_port;
	}

	public void setPort(int m_port) {
		this.m_port = m_port;
	}

	public boolean getActiveStatus() {
		return m_activeStatus;
	}

	public void setActiveStatus(boolean newStatus) {
		m_activeStatus = newStatus;
	}
}
