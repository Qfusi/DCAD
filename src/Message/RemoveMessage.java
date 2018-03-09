package Message;

import java.util.UUID;

public class RemoveMessage extends Message{
	public RemoveMessage(UUID messageID) {
		m_messageID = messageID;
	}

	@Override
	public Object getObj() {
		return null;
	}
}
