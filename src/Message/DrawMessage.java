package Message;
import java.util.UUID;

import DCAD.GObject;

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
