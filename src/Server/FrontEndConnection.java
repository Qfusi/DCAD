
package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import Message.DrawMessage;
import Message.JoinMessage;
import Message.LeaveMessage;
import Message.Message;
import Message.MessageConvertion;
import Message.RemoveMessage;

public class FrontEndConnection {
	private final InetAddress m_address;
	private final int m_port;
	private DatagramSocket m_socket;

	public FrontEndConnection(InetAddress address, int port) {
		m_address = address;
		m_port = port;
	}
	
	public FrontEndConnection(InetAddress address, int port, DatagramSocket socket) {
		m_address = address;
		m_port = port;
		m_socket = socket;
	}

	public boolean handShake() {
		Message message = new JoinMessage();
		sendMessage(message);
		
		message = receiveMessage();
		
		if (message instanceof JoinMessage) {
			if (((JoinMessage) message).getMayJoin())
				return true;
		}
		return false;
	}
	
	public Message receiveMessage() {
		Message message = null;
		byte[] b = new byte[1500];
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
		else if (message instanceof LeaveMessage)
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
		DatagramPacket packet = new DatagramPacket(b, b.length, m_address, m_port);
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
		else if (message instanceof LeaveMessage)
			System.out.println("sent leave message");
	}
	public InetAddress getAddress() {
		return m_address;
	}
	public int getPort() {
		return m_port;
	}
}
