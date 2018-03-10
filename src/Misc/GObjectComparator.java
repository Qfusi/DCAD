package Misc;

import java.util.Comparator;

import DCAD.GObject;

public class GObjectComparator implements Comparator<GObject>{

	@Override
	public int compare(GObject o1, GObject o2) {
		return Long.compare(o1.getTimestamp(), o2.getTimestamp());
	}

}
