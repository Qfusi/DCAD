package Message;

import java.util.UUID;

public class AckMessage extends Message{
	public AckMessage(UUID messageID) {
		m_messageID = messageID;
	}
	
	@Override
	public Object getObj() {
		// TODO Auto-generated method stub
		return null;
	}
}
