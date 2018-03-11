package Message;
import java.util.UUID;

import DCAD.GObject;

//DrawMessage - When a client draws in the GUI he will send this message to the server, which will then broadcast it to all connected clients
public class DrawMessage extends Message{
	private GObject m_obj;
	public DrawMessage(GObject obj, UUID messageID) {
		m_obj = obj;
		m_messageID = messageID;
	}
	
	@Override
	public Object getObj() {
		return m_obj;
	}
}
