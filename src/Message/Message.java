package Message;
import java.io.Serializable;

public abstract class Message implements Serializable{
	public Message() {
	}
	
	public abstract Object getObj();
}
