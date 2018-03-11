package Message;

import java.util.UUID;

import DCAD.GObject;

//RemoveMessage - When right-clicking to remove an object in the GUI the client will sent this message to the server, which will broadcast to all connected clients
public class RemoveMessage extends Message{
	private GObject m_obj;
	public RemoveMessage(GObject obj, UUID messageID) {
		m_obj = obj;
		m_messageID = messageID;
	}

	@Override
	public Object getObj() {
		return m_obj;
	}
}
