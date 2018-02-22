package FrontEnd;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;

import DCAD.GObject;
import Message.MessageConvertion;
import Message.Message;
import Message.DrawMessage;
import Message.JoinMessage;

public class FrontEnd {
	private static Scanner m_s;
	private DatagramSocket m_socket;

	public static void main(String[] args) {
		FrontEnd instance = new FrontEnd(readFile());
		instance.listenForMessages();
	}

	private FrontEnd(int portNumber) {
		try {
			m_socket = new DatagramSocket(portNumber);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	private void listenForMessages() {
		System.out.println("Listening for messages...");
		while (true) {
			// THE SIZE OF THIS BYTE ARRAY IS MUCHO IMPORTANTE
			byte[] buf = new byte[1500];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			Message message = null;

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

			if (message instanceof JoinMessage) {
				System.out.println("received join message of: " + packet.getLength() + " bytes");
				sendMessage(packet.getAddress(), packet.getPort(), message);
			} else if (message instanceof DrawMessage) {
				System.out.println("received draw message of: " + packet.getLength() + " bytes");
				sendMessage(packet.getAddress(), packet.getPort(), message);
			}
		}
	}

	public void sendMessage(InetAddress address, int port, Message message) {
		byte[] b = null;
		try {
			b = MessageConvertion.serialize(message);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		DatagramPacket packet = new DatagramPacket(b, b.length, address, port);
		try {
			m_socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("sent");
	}

	private static int readFile() {
		try {
			m_s = new Scanner(new FileReader("resources/frontEndFile"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		int s = 0;

		while (m_s.hasNext()) {
			s = m_s.nextInt();
		}

		return s;
	}
}
