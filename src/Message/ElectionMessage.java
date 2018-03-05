package Message;

public class ElectionMessage extends Message{
	private int m_ID;
	boolean m_isReply;
	
	public ElectionMessage(int id, boolean reply) {
		m_ID = id;
		m_isReply = reply;
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
	
	@Override
	public Object getObj() {
		return null;
	}
}