/* 
 * Copyright 2009 Harvard University Library
 * 
 * This file is part of FITS (File Information Tool Set).
 * 
 * FITS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * FITS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with FITS.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.harvard.hul.ois.fits.mapping;

import java.util.Hashtable;
import java.util.List;
import org.jdom.Element;

public class AttributeMap {
	
	private String name;
	private Hashtable<String,String> maps = new Hashtable<String,String>();
	
	@SuppressWarnings("unchecked")
	public AttributeMap(Element element) {
		name = element.getAttributeValue("name");
		//Get element mappings
		List<Element> childMaps = element.getChildren("map");
		for(Element map : childMaps) {
			String from = map.getAttributeValue("from");
			String to   = map.getAttributeValue("to");
			maps.put(from,to);
		}
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Hashtable<String, String> getMaps() {
		return maps;
	}
	public void setMaps(Hashtable<String, String> maps) {
		this.maps = maps;
	}
	
	public void addMap(String from, String to) {
		maps.put(from,to);
	}

}
