package DCAD;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import Message.DrawMessage;
import Message.JoinMessage;
import Message.DisconnectMessage;
import Message.Message;
import Message.MessageConvertion;
import Message.RemoveMessage;

public class ServerConnection {
	private DatagramSocket m_socket;
	private InetAddress m_serverAddress;
	private int m_port;

	public ServerConnection(String hostName, int port) {
		m_port = port;
		try {
			m_socket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		try {
			m_serverAddress = InetAddress.getByName(hostName);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public boolean handshake(GUI gui) {
		Message message = new JoinMessage();
		sendMessage(message);

		message = receiveMessage();
		
		if (message instanceof JoinMessage) {
			if (((JoinMessage) message).getMayJoin()) {
				gui.reDrawEverything(((JoinMessage) message).getList());
				return true;
			}
		}
		return false;
	}

	public Message receiveMessage() {
		Message message = null;
		// IF EOFEXCEPTION OCCURS THIS BYTE ARRAY HAS BEEN EXCEEDED!!!!!!!!!!!!!!!!
		byte[] b = new byte[1024];
		DatagramPacket packet = new DatagramPacket(b, b.length);

		try {
			m_socket.receive(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			message = (Message) MessageConvertion.deserialize(packet.getData());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (message instanceof JoinMessage)
			System.out.println("received join message");
		else if (message instanceof DrawMessage)
			System.out.println("received draw message");
		else if (message instanceof RemoveMessage)
			System.out.println("received remove message");
		else if (message instanceof DisconnectMessage)
			System.out.println("received leave message");

		return message;
	}

	public void sendMessage(Message message) {
		byte[] b = null;
		try {
			b = MessageConvertion.serialize(message);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		DatagramPacket packet = new DatagramPacket(b, b.length, m_serverAddress, m_port);
		try {
			m_socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (message instanceof JoinMessage)
			System.out.println("sent join message");
		else if (message instanceof DrawMessage)
			System.out.println("sent draw message");
		else if (message instanceof RemoveMessage)
			System.out.println("sent remove message");
		else if (message instanceof DisconnectMessage)
			System.out.println("sent disconnect message");
	}
	
	public void requestDisconnect() {
		for (int i = 10; i > 0; i--)
			sendMessage(new DisconnectMessage(m_socket.getLocalPort()));
	}
}
