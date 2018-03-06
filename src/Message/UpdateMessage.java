package Message;

import java.util.ArrayList;
import DCAD.GObject;

public class UpdateMessage extends Message{
	private ArrayList<GObject> m_GObjects  = new ArrayList<GObject>();
	public UpdateMessage(ArrayList<GObject> list) {
		m_GObjects = list;
	}
	public ArrayList<GObject> getList() {
		return m_GObjects;
	}
	
	@Override
	public Object getObj() {
		return null;
	}
}