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
		System.out.println("0");
		UUID id = message.getMessageID();
		System.out.println("------ xD xD xD  :::: " + id);
		boolean add = true;
		System.out.println("size of list: " + m_messages.size());
		for (Message m : m_messages) {
			if (id == m.getMessageID()) {
				add = false;
				System.out.println("1");
				System.out.println(id + " VSVSVSVSVSVSVSVSV " + m.getMessageID());
			}
		}
		if (add) {
			m_messages.add(message);
			if (message instanceof DrawMessage) {
				System.out.println("======================ADDED MESSAGE TO ALO - PORT: " + message.getPort());
				System.out.println("======================ADDED : " + message.getMessageID());
			}
		} 
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
                System.out.println("no more message to remove(AOL)");
                break; 
            }
            else {
            System.out.println("Removed message with port " + removeMes.getPort() + " and id: "+ removeMes.getMessageID() + " (AOL)");
                m_messages.remove(removeMes);
            }

        }

    }
}
