
package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import AtLeastOnce.AtLeastOnce;
import Message.DrawMessage;
import Message.AckMessage;
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
	private AtLeastOnce m_ALO;
	private ArrayList<UUID> m_receivedMessages = new ArrayList<UUID>();

	public FrontEndConnection(InetAddress address, int port) {
		m_address = address;
		m_port = port;

		m_ALO = new AtLeastOnce(this);
	}

	public FrontEndConnection(InetAddress address, int port, DatagramSocket socket) {
		m_address = address;
		m_port = port;
		m_socket = socket;

		m_ALO = new AtLeastOnce(this);
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

		if (!checkIfAlreadyRecieved(message.getMessageID()) && !(message instanceof AckMessage)) {
			m_receivedMessages.add(message.getMessageID());
			sendMessage(new AckMessage(message.getMessageID()));

			if (message instanceof ConnectMessage)
				System.out.println("(UDP side) -=RECEIVED=- connect message");
			else if (message instanceof DrawMessage)
				System.out.println("(UDP side) -=RECEIVED=- draw message");
			else if (message instanceof RemoveMessage)
				System.out.println("(UDP side) -=RECEIVED=- remove message");
			else if (message instanceof DisconnectMessage)
				System.out.println("(UDP side) -=RECEIVED=- disconnect message");
		} else if (!(message instanceof AckMessage)) {
			sendMessage(new AckMessage(message.getMessageID()));
			return null;
		} else if (message instanceof AckMessage) {
			System.out.println("(UDP side) -=RECEIVED=- remove message");
			m_ALO.removeMessage(message.getMessageID());
			return null;
		}

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

		double TRANSMISSION_FAILURE_RATE = 0.0;
		Random generator = new Random();
		double failure = generator.nextDouble();

		if (!(message instanceof AckMessage) && !(message instanceof DisconnectMessage)
				&& !(message instanceof NewActiveServerMessage))
			m_ALO.addMessage(message);

		if (failure > TRANSMISSION_FAILURE_RATE) {
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
			else if (message instanceof AckMessage)
				System.out.println("(UDP side) -=SENT=- Ack message");
		} else
			System.out.println("message was lost");
	}

	public boolean checkIfAlreadyRecieved(UUID id) {
		// Checks the ID in order to avoid handling the same message (same id)
		// more than once
		for (UUID i : m_receivedMessages) {
			if (i == id)
				return true;
		}
		return false;
	}

	public InetAddress getAddress() {
		return m_address;
	}

	public int getPort() {
		return m_port;
	}
}
