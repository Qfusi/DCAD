
package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.UUID;

import AtLeastOnce.AtLeastOnce;
import Message.*;

public class FrontEndConnection {
	private final InetAddress m_address;
	private final int m_port;
	private DatagramSocket m_socket;
	private AtLeastOnce m_ALO;
	private ArrayList<UUID> m_receivedMessages = new ArrayList<UUID>();

	//keeps track of the one to one connection to the FE, messages will be received and send from this module
	public FrontEndConnection(InetAddress address, int port, DatagramSocket socket) {
		m_address = address;
		m_port = port;
		m_socket = socket;
		m_ALO = new AtLeastOnce(this);
	}

	public Message receiveMessage() {
		Message message = null;
		// IF EOFEXCEPTION OCCURS THIS BYTE ARRAY HAS BEEN EXCEEDED!!!!!!!!!!!!!!!!!
		byte[] b = new byte[2048];
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

		if (!checkIfAlreadyRecieved(message.getMessageID()) && !(message instanceof AckMessage) && !(message instanceof ConnectMessage)) {
			m_receivedMessages.add(message.getMessageID());

			AckMessage ack = new AckMessage(message.getMessageID());
			ack.setPort(message.getPort());
			ack.setAddress(message.getAddress());
			sendMessage(ack);

		} else if (!(message instanceof AckMessage) && !(message instanceof ConnectMessage)) {
			AckMessage ack = new AckMessage(message.getMessageID());
			ack.setPort(message.getPort());
			ack.setAddress(message.getAddress());
			sendMessage(ack);
			return null;
		} else if (message instanceof AckMessage) {
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

		//if not ACK,Disconnect,NewActiveServer ore FEPing message add to AOL list which send until an ACKmessage is received. This is not needed for these message types. 
		if (!(message instanceof AckMessage) && !(message instanceof DisconnectMessage) && !(message instanceof NewActiveServerMessage) && !(message instanceof FEPingMessage)) {
			m_ALO.addMessage(message);
		}

		try {
			m_socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
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

	public void addToALO(Message message) {
		m_ALO.addMessage(message);
	}

	public void removeFEPing() {
		m_ALO.removeFEPing();
	}

	public InetAddress getAddress() {
		return m_address;
	}

	public int getPort() {
		return m_port;
	}

	public void removeCrashedClientsMessagesBridge(int port) {
		m_ALO.removeCrashedClientsMessages(port);
	}
}
