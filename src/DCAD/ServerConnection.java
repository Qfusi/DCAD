package DCAD;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.UUID;
import AtLeastOnce.AtLeastOnce;
import Message.*;

public class ServerConnection {
	private DatagramSocket m_socket;
	private InetAddress m_serverAddress;
	private int m_port;
	private AtLeastOnce m_ALO;
	private ArrayList<UUID> m_receivedMessages = new ArrayList<UUID>();

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
		m_ALO = new AtLeastOnce(this);
	}

	//Send connect, waits on answer and moves along if connected( no negative answer suspected)
	public boolean handshake(GUI gui) {
		Message message = new ConnectMessage(UUID.randomUUID());
		sendMessage(message);
		message = receiveMessage();

		if (message instanceof ConnectMessage) {
			m_ALO.removeMessage(message.getMessageID());
			if (((ConnectMessage) message).getMayJoin()) {
				gui.reDrawEverything(((ConnectMessage) message).getList());
				return true;
			}
		}
		return false;
	}

	public Message receiveMessage() {
		Message message = null;
		// IF EOFEXCEPTION OCCURS THIS BYTE ARRAY HAS BEEN EXCEEDED!!!!!!!!!!!!!!!!
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

		// if message ID is new and it't not an ACK -> add to received messages and return null
		if (!checkIfAlreadyRecieved(message.getMessageID()) && !(message instanceof AckMessage)) {
			m_receivedMessages.add(message.getMessageID());

			sendMessage(new AckMessage(message.getMessageID()));
		}
		else if (!(message instanceof AckMessage)) {
			sendMessage(new AckMessage(message.getMessageID()));
			return null;
		}
		else if (message instanceof AckMessage || message instanceof ConnectMessage) {
			m_ALO.removeMessage(message.getMessageID());
			return null;
		}

		//catch all checkup messages, user should not notice them. 
		if(message instanceof ClientCheckUpMessage) {
			sendMessage(new ClientCheckUpMessage(UUID.randomUUID(), true));
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

		DatagramPacket packet = new DatagramPacket(b, b.length, m_serverAddress, m_port);

		if (!(message instanceof AckMessage) && !(message instanceof DisconnectMessage))
			m_ALO.addMessage(message);


		try {
			m_socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void requestDisconnect() {
		m_ALO.stopRunning();
		for (int i = 10; i > 0; i--)
			sendMessage(new DisconnectMessage(m_socket.getLocalPort()));
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
}
