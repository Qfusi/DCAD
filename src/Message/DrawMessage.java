package Message;
import DCAD.GObject;

public class DrawMessage extends Message{
	private GObject m_obj;
	public DrawMessage(GObject obj) {
		m_obj = obj;
	}
	@Override
	public Object getObj() {
		return m_obj;
	}
}
