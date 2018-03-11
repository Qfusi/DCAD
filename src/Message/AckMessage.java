package Message;

import java.util.UUID;

//AckMessage - when either a server or client receives a message (other than another ack) they will reply with one of these messages containing the received message ID
public class AckMessage extends Message{
	public AckMessage(UUID messageID) {
		m_messageID = messageID;
	}
	
	@Override
	public Object getObj() {
		return null;
	}
}
