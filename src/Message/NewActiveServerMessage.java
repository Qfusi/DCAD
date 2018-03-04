package Message;

public class NewActiveServerMessage extends Message{
	private boolean m_isOkay = false;
	public NewActiveServerMessage() {
		
	}
	@Override
	public Object getObj() {
		return null;
	}
	public boolean getisOkay() {
		return m_isOkay;
	}
	public void setOkay(boolean isOkay) {
		m_isOkay = isOkay;
	}
}