/**
 *
 * @author brom
 */

package DCAD;

import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JFrame;

import Message.DrawMessage;
import Message.RemoveMessage;

public class GUI extends JFrame implements WindowListener, ActionListener, MouseListener, MouseMotionListener {

	JButton ovalButton = new JButton("Oval");
	JButton rectangleButton = new JButton("Rect");
	JButton lineButton = new JButton("Line");
	JButton filledOvalButton = new JButton("Filled oval");
	JButton filledRectangleButton = new JButton("Filled Rect");
	JButton redButton = new JButton("Red");
	JButton blueButton = new JButton("Blue");
	JButton greenButton = new JButton("Green");
	JButton whiteButton = new JButton("White");
	JButton pinkButton = new JButton("Pink");

	private GObject template = new GObject(Shape.OVAL, Color.RED, 363, 65, 25, 25);
	private GObject current = null;
	private ServerConnection m_SC;

	private LinkedList<GObject> objectList = new LinkedList<GObject>();

	public GUI(int xpos, int ypos) {
		setSize(xpos, ypos);
		setTitle("FTCAD");

		Container pane = getContentPane();
		pane.setBackground(Color.BLACK);

		pane.add(ovalButton);
		pane.add(rectangleButton);
		pane.add(lineButton);
		pane.add(filledOvalButton);
		pane.add(filledRectangleButton);
		pane.add(redButton);
		pane.add(blueButton);
		pane.add(greenButton);
		pane.add(whiteButton);
		pane.add(pinkButton);

		pane.setLayout(new FlowLayout());
		setVisible(true);
	}

	public void addToListener() {
		addWindowListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);

		ovalButton.addActionListener(this);
		rectangleButton.addActionListener(this);
		lineButton.addActionListener(this);
		filledOvalButton.addActionListener(this);
		filledRectangleButton.addActionListener(this);
		redButton.addActionListener(this);
		blueButton.addActionListener(this);
		greenButton.addActionListener(this);
		whiteButton.addActionListener(this);
		pinkButton.addActionListener(this);

	}

	// WindowListener methods
	public void windowActivated(WindowEvent e) {
		repaint();
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {
		m_SC.requestDisconnect();
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
		repaint();
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
		repaint();
	}

	// MouseListener methods
	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			if (e.getX() > 0 && e.getY() > 91) {
				current = new GObject(template.getShape(), template.getColor(), e.getX(), e.getY(), 0, 0);
			} else
				current = null;
		}
		repaint();
	}

	public void mouseClicked(MouseEvent e) {
		// User clicks the right mouse button:
		// undo an operation by removing the most recently added object.
		if (e.getButton() == MouseEvent.BUTTON3 && objectList.size() > 0) {

			GObject toRemove = objectList.getLast();
			removeObject(toRemove);
			m_SC.sendMessage(new RemoveMessage(toRemove, UUID.randomUUID()));
		}
		repaint();
	}

	public void mouseReleased(MouseEvent e) {
		if (current != null) {
			current.setTimestamp(System.currentTimeMillis());
			current.setID(UUID.randomUUID());

			addObject(current);

			m_SC.sendMessage(new DrawMessage(current, UUID.randomUUID()));
			current = null;
		}
		repaint();
	}

	// MouseMotionListener methods
	public void mouseMoved(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
		if (current != null && e.getX() > 0 && e.getY() > 91) {
			current.setDimensions(e.getX() - current.getX(), e.getY() - current.getY());
		}
		repaint();
	}

	// ActionListener methods
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == ovalButton) {
			template.setShape(Shape.OVAL);
		} else if (e.getSource() == rectangleButton) {
			template.setShape(Shape.RECTANGLE);
		} else if (e.getSource() == lineButton) {
			template.setShape(Shape.LINE);
		} else if (e.getSource() == filledOvalButton) {
			template.setShape(Shape.FILLED_OVAL);
		} else if (e.getSource() == filledRectangleButton) {
			template.setShape(Shape.FILLED_RECTANGLE);
		} else if (e.getSource() == redButton) {
			template.setColor(Color.RED);
		} else if (e.getSource() == blueButton) {
			template.setColor(Color.BLUE);
		} else if (e.getSource() == greenButton) {
			template.setColor(Color.GREEN);
		} else if (e.getSource() == whiteButton) {
			template.setColor(Color.WHITE);
		} else if (e.getSource() == pinkButton) {
			template.setColor(Color.PINK);
		}
		repaint();
	}

	public void update(Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 60, getSize().width, getSize().height - 60);

		template.draw(g);

		for (ListIterator<GObject> itr = objectList.listIterator(); itr.hasNext();) {
			itr.next().draw(g);
		}

		if (current != null) {
			current.draw(g);
		}
	}

	public void paint(Graphics g) {
		super.paint(g); // The superclass (JFrame) paint function draws the GUI
		// components.
		update(g);
	}

	public void passSC(ServerConnection connection) {
		m_SC = connection;
	}

	//when object is new or recived this method selects the index where the object should be stored in the list. It has a 5 milsec limit or else the objects will be handled as concurrent.
	public void addObject(GObject obj) {

		if (checkIfAlreadyReceived(obj.getID()) == false) {


			if(!(objectList.isEmpty())) {//if somethings in list


				//if last obj in list has a  smaller/younger timestamp than new obj (with more than +/-5 milsec diffrence ) -> new obj is older(bigger timestamp) and should be at the end of list
				if(obj.getTimestamp() > objectList.get(objectList.size()-1).getTimestamp() + 5){
					objectList.addLast(obj);
					repaint();
					return;
				}
				//if first obj in list has a bigger/older timestamp than new obj (with more than +/-5 milsec diffrence )-> new obj is younger(smaller timestamp) and should be at the end of list
				else if(obj.getTimestamp() < objectList.get(0).getTimestamp()-5) {
					objectList.add(0, obj);
					repaint();
					return;
				}


				for(int i = objectList.size() - 2; i >= 0; i--) {//iteration from last element to first

					//checks if element on index is in +/-5 milsec radius of new objects timestamp -> its concurrent and should be placed there at this index 
					if((obj.getTimestamp() + 5 >= objectList.get(i).getTimestamp() && obj.getTimestamp() - 5 <= objectList.get(i).getTimestamp())) {
						objectList.add(i, obj);//gets added 
						repaint();
						return;
					}
					//if not close enough (+/-5 milsec) but the time stamp of element is smaller than the new object it needs to be placed at index +1 to sort. 
					else if (obj.getTimestamp() - 5 >= objectList.get(i).getTimestamp() ) {
						objectList.add(i+1, obj);
						repaint();
						return;
					}
				}
			}
			//empty list, add
			else {
				objectList.addLast(obj);
				return;
			}
			repaint();
		}
	}

	public void removeObject(GObject obj) {
		if (!objectList.isEmpty()) {
			for (int i = 0; i < objectList.size(); i++) {
				if (obj.getID().equals(objectList.get(i).getID())) {
					objectList.remove(i);
				}
			}
		}
		repaint();
	}

	public void reDrawEverything(ArrayList<GObject> list) {
		for (GObject obj : list)
			objectList.addLast(obj);
		repaint();
	}

	public boolean checkIfAlreadyReceived(UUID id) {
		// Checks the ID in order to avoid handling the same message (same id)
		// more than once
		for (GObject obj : objectList) {
			if (obj.getID().equals(id)) {
				return true;
			}
		}
		return false;
	}
}
