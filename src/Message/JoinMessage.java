package Message;

import java.util.ArrayList;
import DCAD.GObject;

public class JoinMessage extends Message{
	private ArrayList<GObject> m_GObjects  = new ArrayList<GObject>();
	boolean mayJoin = false;
	public JoinMessage() {
	}

	public boolean getMayJoin() {
		return mayJoin;
	}
	
	public void setMayJoin(boolean bool) {
		mayJoin = bool;
	}
	
	public ArrayList getList() {
		return m_GObjects;
	}
	
	public void setList(ArrayList list) {
		m_GObjects = list;
	}
	
	@Override
	public Object getObj() {
		return mayJoin;
	}
}