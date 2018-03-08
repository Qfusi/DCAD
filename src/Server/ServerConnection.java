package Server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import Message.ConnectMessage;
import Message.ElectionMessage;
import Message.ElectionWinnerMessage;
import Message.Message;
import Message.ServerPingMessage;

public class ServerConnection {
	private ReplicaServer m_rs;
	private int m_id;
	private  InetAddress m_address;
	private  int m_port;
	private Socket m_socket;
	private int m_disconnectedPort;
	private ObjectOutputStream outputStream;
	
	public ServerConnection(ReplicaServer rs, int id, InetAddress address, int port, Socket socket) {
		m_rs = rs;
		m_id = id;
		m_address = address;
		m_port = port;
		m_socket = socket;
	}
	
	//----------------------------------------------Constantly tries to connect to specified server
	public void connectToServer() {
		startListenerThread(m_socket);
		while (true) {
			Message message = new ServerPingMessage(m_id, false);
			
			sendMessage(message, false);
			
			try {
				Thread.sleep(7500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void sendMessage(Message message, boolean fromRS) {
		try {
			if (message instanceof ServerPingMessage) {
				message.setPort(m_socket.getLocalPort());
				System.out.println("(TCP side) Server " + m_id + " -=SENT=- ping to port: " + m_port);
			}
			else if (message instanceof ServerPingMessage) {
				message.setPort(m_socket.getLocalPort());
				((ServerPingMessage)message).setID(m_id);
			}
			else if (message instanceof ElectionMessage) {
				message.setPort(m_socket.getLocalPort());
				System.out.println("(TCP side) Server " + m_id + " -=SENT=- ElectionMessage with ID " + ((ElectionMessage)message).getID() + " to server on port: " + m_socket.getPort());
			}
			else if (message instanceof ElectionWinnerMessage) {
				message.setPort(m_socket.getLocalPort());
				System.out.println("(TCP side) Server " + m_id + " -=SENT=- ElectionWinnerMessage with ID " + ((ElectionWinnerMessage)message).getID() + " to server on port: " + m_socket.getPort());
			}
			else if (message instanceof ConnectMessage) {}
			
			outputStream = new ObjectOutputStream(m_socket.getOutputStream());
			outputStream.writeObject(message);
			
		} catch (IOException e) {
			if (!fromRS)
			{
				if (m_disconnectedPort == 0)
					m_disconnectedPort = m_socket.getPort();
				
				System.err.println("Server " + m_disconnectedPort + " has disconnected (Exception found in ServerConnection sendMessage method)");
				
				reconnect();
			}
		}
	}
	
	private void reconnect() {
		while (true) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			try {
				
				Socket socket = new Socket();
				InetSocketAddress serveraddress = new InetSocketAddress(m_address, m_port);
				socket.connect(serveraddress);
				
				m_disconnectedPort = 0;
				
				m_rs.addServerConnection(new ServerConnection(m_rs, m_id, m_address, m_port, socket));
				
				startListenerThread(socket);
				
				this.m_socket = socket;
				
				System.out.println("Reconnected to port: " + m_port);
				break;
			} catch (IOException e1) {
				System.err.println("Tried to reconnect to port: " + m_port);
				e1.printStackTrace();
			}
		}
	}
	
	private void startListenerThread(final Socket socket) {
		new Thread(new Runnable() {
			public void run() {
				m_rs.listenForServerMessages(socket);
			}
		}).start();
	}
	
	public Socket getSocket() {
		return m_socket;
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
		return m_id;
	}
}
