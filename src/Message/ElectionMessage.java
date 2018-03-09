package Message;

public class ElectionMessage extends Message{
	private int m_ID;
	private boolean m_isReply;
	private long m_updateTimestamp;
	
	public ElectionMessage(int id, boolean reply, long timestamp) {
		m_ID = id;
		m_isReply = reply;
		m_updateTimestamp = timestamp;
	}

	public int getID() {
		return m_ID;
	}

	public void setID(int m_ID) {
		this.m_ID = m_ID;
	}
	
	public boolean isReply() {
		return m_isReply;
	}
	
	public void setIsReply(boolean reply) {
		m_isReply = reply;
	}
	
	public long getUpdateTimestamp() {
		return m_updateTimestamp;
	}
	
	@Override
	public Object getObj() {
		return null;
	}
}