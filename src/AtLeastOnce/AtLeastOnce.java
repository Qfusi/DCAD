package AtLeastOnce;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import DCAD.ServerConnection;
import Message.Message;
import Message.NewActiveServerMessage;
import Message.DrawMessage;
import Message.FEPingMessage;
import Server.FrontEndConnection;

//class controls which message are send or not, is created by each client once and by each server. 
//Messages will be stored in list and removed if ACKSync is received and handled with same id. 

public class AtLeastOnce implements Runnable {

	private List<Message> m_messages = Collections.synchronizedList(new ArrayList<Message>());
	private ServerConnection m_SC;
	private FrontEndConnection m_FC;
	private boolean running = true;

	public AtLeastOnce(ServerConnection SC) {
		new Thread(this).start();
		m_SC = SC;
	}

	public AtLeastOnce(FrontEndConnection FC) {
		new Thread(this).start();
		m_FC = FC;
	}

	@Override
	public void run() {
		while (running) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			synchronized (m_messages) {
				Message s;
				// Loop that sends out (not yet acked) messages
				for (Iterator<Message> itr = m_messages.iterator(); itr.hasNext();) {
					s = itr.next();

					if (m_SC != null)//if it is a ServerConnection
						m_SC.sendMessage(s);
					else if (m_FC != null)// if it is a FrontEndConnection
						m_FC.sendMessage(s);
				}
			}
		}
	}

	public void addMessage(Message message) {
		// checks the ID of the message and every message in the list, adds the
		// message if no match is found
		Message temp = message;
		boolean add = true;

		for (Message m : m_messages) {
			if (temp.getMessageID().equals(m.getMessageID())) {
				add = false;
			}
		}
		if (add) {
			m_messages.add(temp);
		} 
	}

	public void removeMessage(UUID id) {
		// Removes the message if the ID matches any of the IDs in the list
		synchronized (m_messages) {
			Message remove = null;
			for (Message m : m_messages) {
				if (id.equals(m.getMessageID())) {
					remove = m;
					break;
				}
			}
			m_messages.remove(remove);
		}
	}

	public void removeFEPing() {
		//??????????????????????????????????????????????????
		Message remove = null;
		for (Message m : m_messages) {
			if (m instanceof FEPingMessage) {
				remove = null;
			}
		}
		m_messages.remove(remove);
	}

	public void stopRunning() {
		running = false;
	}

	public void removeCrashedClientsMessages(int port) {
		while(true) {
			Message removeMes = null;
			for (Message m : m_messages) {
				if (port == m.getPort()) {
					removeMes = m;
					break;
				}
			}
			if(removeMes == null) {

				break; 
			}
			else {
				m_messages.remove(removeMes);
			}
		}
	}
}
