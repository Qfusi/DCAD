package Server;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerConnection {
	//---------------------socket1
	private ServerSocket m_Ssocket;
	private  InetAddress m_address;
	private  int m_port;
	
	//---------------------socket2
	private Socket m_Csocket;
	private InetAddress m_address2;
	private  int m_port2;
	
	//---------------------socket3
	private Socket m_Csocket2;
	private InetAddress m_address3;
	private  int m_port3;
	
	//SERVER 1
	public ServerConnection(InetAddress address, int port) {
		m_address = address;
		m_port = port;
	}
	
	//SERVER 2
	public ServerConnection(InetAddress address, int port, InetAddress address2, int port2) {
		m_address = address;
		m_port = port;
		m_address2 = address2;
		m_port2 = port2;
	}
	
	//SERVER 3
	public ServerConnection(InetAddress address, int port, InetAddress address2, int port2, InetAddress address3, int port3) {
		m_address = address;
		m_port = port;
		m_address2 = address2;
		m_port2 = port2;
		m_address3 = address3;
		m_port3 = port3;
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
