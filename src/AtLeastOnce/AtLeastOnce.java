package AtLeastOnce;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import DCAD.ServerConnection;
import Message.Message;
import Message.fePingMessage;
import Server.FrontEndConnection;



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
						m_SC.sendMessage(s );
					else if (m_FC != null)// if it is a FrontEndConnection
						m_FC.sendMessage(s);
				}
			}
		}
	}

	public void addMessage(Message message) {
		// checks the ID of the message and every message in the list, adds the
		// message if no match is found
		UUID id = message.getMessageID();
		boolean add = true;
		for (Message m : m_messages) {
			if (id.equals(m.getMessageID()) || m instanceof fePingMessage)
				add = false;
		}
		if (add)
			m_messages.add(message);
	}

	public void removeMessage(UUID id) {
		// Removes the message if the ID matches any of the IDs in the list
		Message remove = null;
		for (Message m : m_messages) {
			if (id.equals(m.getMessageID())) {
				remove = m;
				break;
			}
		}
		m_messages.remove(remove);
	}
	
	public void removeFEPing() {
		Message remove = null;
		for (Message m : m_messages) {
			if (m instanceof fePingMessage) {
				remove = null;
			}
		}
		m_messages.remove(remove);
	}
	
	public void stopRunning() {
		running = false;
	}
}
