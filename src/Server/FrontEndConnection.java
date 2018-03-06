
package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import Message.DrawMessage;
import Message.ConnectMessage;
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

		if (message instanceof ConnectMessage)
			System.out.println("(UDP side) -=RECEIVED=- connect message");
		else if (message instanceof DrawMessage)
			System.out.println("(UDP side) -=RECEIVED=- draw message");
		else if (message instanceof RemoveMessage)
			System.out.println("(UDP side) -=RECEIVED=- remove message");
		else if (message instanceof DisconnectMessage)
			System.out.println("(UDP side) -=RECEIVED=- disconnect message");

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
		if (message instanceof ConnectMessage)
			System.out.println("(UDP side) -=SENT=- connect message");
		else if (message instanceof DrawMessage)
			System.out.println("(UDP side) -=SENT=- draw message");
		else if (message instanceof RemoveMessage)
			System.out.println("(UDP side) -=SENT=- remove message");
		else if (message instanceof DisconnectMessage)
			System.out.println("(UDP side) -=SENT=- disconnect message");
		else if (message instanceof NewActiveServerMessage)
			System.out.println("(UDP side) -=SENT=- New Active Server message");
	}
	public InetAddress getAddress() {
		return m_address;
	}
	public int getPort() {
		return m_port;
	}
}
