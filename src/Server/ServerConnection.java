package Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

import Message.JoinMessage;
import Message.Message;
import Message.ServerJoinMessage;

public class ServerConnection {
	private int m_ID;
	
	//---------------------socket1
	private Socket m_socket;
	private  InetAddress m_address;
	private  int m_port;
	
	public ServerConnection(ReplicaServer RS, int id, InetAddress address, int port, Socket socket) {
		m_socket = socket;
		m_ID = id;
		m_address = address;
		m_port = port;
	}
	
	//----------------------------------------------Constantly tries to connect to specified server
	public void connectToServer() {
		while (true) {
			try {
				ObjectOutputStream outputStream = new ObjectOutputStream(m_socket.getOutputStream());
				Message message = new ServerJoinMessage(m_ID, false);
				message.setPort(m_socket.getLocalPort());
				
				outputStream.writeObject(message);
				
				System.out.println("sent message to port: " + m_port);
				
				Thread.sleep(5000);
			} catch (IOException e) {
				System.err.println("A server has disconnected (Send method)");
				
				electionProtocol();
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void electionProtocol() {
		if (m_ID == 1) {
			
		}
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
	
	public int getID() {
		return m_ID;
	}
}
