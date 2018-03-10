package DCAD;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import AtLeastOnce.AtLeastOnce;
import Message.DrawMessage;
import Message.AckMessage;
import Message.ClientCheckUpMessage;
import Message.ConnectMessage;
import Message.DisconnectMessage;
import Message.Message;
import Message.MessageConvertion;
import Message.RemoveMessage;
import Message.FEPingMessage;

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
				
				if (message instanceof ConnectMessage)
					System.out.println("received connect message");
				else if (message instanceof DrawMessage)
					System.out.println("received draw message");
				else if (message instanceof RemoveMessage)
					System.out.println("received remove message");
				else if (message instanceof DisconnectMessage)
					System.out.println("received leave message");
				else if (message instanceof ClientCheckUpMessage)
					System.out.println("received clientCheckUp message");
				
				sendMessage(new AckMessage(message.getMessageID()));
				
		}
		else if (!(message instanceof AckMessage)) {
			sendMessage(new AckMessage(message.getMessageID()));
			return null;
		}
		else if (message instanceof AckMessage || message instanceof ConnectMessage) {
			System.out.println("received ACK message");
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
		double TRANSMISSION_FAILURE_RATE = 0.0;
		Random generator = new Random();
		double failure = generator.nextDouble();
		byte[] b = null;
		
		try {
			b = MessageConvertion.serialize(message);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		DatagramPacket packet = new DatagramPacket(b, b.length, m_serverAddress, m_port);
		
		if (!(message instanceof AckMessage) && !(message instanceof DisconnectMessage))
			m_ALO.addMessage(message);
		
		if (failure > TRANSMISSION_FAILURE_RATE) {
			try {
				m_socket.send(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
				
				if (message instanceof ConnectMessage)
					System.out.println("sent connect message with ID: " + message.getMessageID());
				else if (message instanceof DrawMessage)
					System.out.println("sent draw message with ID: " + message.getMessageID());
				else if (message instanceof RemoveMessage)
					System.out.println("sent remove message with ID: " + message.getMessageID());
				else if (message instanceof DisconnectMessage)
					System.out.println("sent disconnect message");
				else if (message instanceof AckMessage)
					System.out.println("sent ACK message");
				else if (message instanceof FEPingMessage)
					System.out.println("sent fePing message");
		}
		else
			System.out.println("message was lost");
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
