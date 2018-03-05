
package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import Message.DrawMessage;
import Message.JoinMessage;
import Message.DisconnectMessage;
import Message.Message;
import Message.MessageConvertion;
import Message.NewActiveServerMessage;
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

	public boolean handShake(InetAddress address, int port) {
		Message message = new NewActiveServerMessage(address, port);
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
		// IF EOFEXCEPTION OCCURS THIS BYTE ARRAY HAS BEEN EXCEEDED!!!!!!!!!!!!!!!!!
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
			System.out.println("(UDP side) received join message");
		else if (message instanceof DrawMessage)
			System.out.println("(UDP side) received draw message");
		else if (message instanceof RemoveMessage)
			System.out.println("(UDP side) received remove message");
		else if (message instanceof DisconnectMessage)
			System.out.println("(UDP side) received disconnect message");

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
			System.out.println("(UDP side) sent join message");
		else if (message instanceof DrawMessage)
			System.out.println("(UDP side) sent draw message");
		else if (message instanceof RemoveMessage)
			System.out.println("(UDP side) sent remove message");
		else if (message instanceof DisconnectMessage)
			System.out.println("(UDP side) sent disconnect message");
		else if (message instanceof NewActiveServerMessage)
			System.out.println("(UDP side) sent New Active Server message");
	}
	public InetAddress getAddress() {
		return m_address;
	}
	public int getPort() {
		return m_port;
	}
}
