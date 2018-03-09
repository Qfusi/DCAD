package Message;

import java.util.ArrayList;
import DCAD.GObject;

public class UpdateMessage extends Message{
	private ArrayList<GObject> m_GObjects  = new ArrayList<GObject>();
	private long m_timestamp;
	public UpdateMessage(ArrayList<GObject> list, long time) {
		m_GObjects = list;
		m_timestamp = time;
	}
	public ArrayList<GObject> getList() {
		return m_GObjects;
	}
	public long getTimestamp() {
		return m_timestamp;
	}
	
	@Override
	public Object getObj() {
		return null;
	}
}