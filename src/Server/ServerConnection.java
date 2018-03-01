package Server;

import java.net.InetAddress;

public class ServerConnection {
	private  InetAddress m_address;
	private  int m_port;
	
	public ServerConnection(InetAddress address, int port) {
		m_address = address;
		m_port = port;
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
}