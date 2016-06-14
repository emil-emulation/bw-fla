package de.bwl.bwfla.objectarchive.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BsbFileCollection implements Serializable {

	public List<BsbFileCollectionEntry> files = new ArrayList<BsbFileCollectionEntry>();
	public String id;
}
