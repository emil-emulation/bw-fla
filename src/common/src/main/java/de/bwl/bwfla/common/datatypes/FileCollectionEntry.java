package de.bwl.bwfla.common.datatypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import de.bwl.bwfla.common.datatypes.Drive.DriveType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "file", propOrder = {"type"}, namespace="http://bwfla.bwl.de/common/datatypes")
public class FileCollectionEntry extends Binding {
	
	@XmlElement(required = true, namespace="http://bwfla.bwl.de/common/datatypes")
	protected DriveType type;
	
	public FileCollectionEntry()
	{
		this.url = null;
		this.type = null;
		this.id = null;
	}
	
	public FileCollectionEntry(String ref, DriveType type, String label) {
		this.url = ref;
		this.type = type;
		this.id = label;
	}
	
	public DriveType getType() {
		return type;
	}

	public void setType(DriveType type) {
		this.type = type;
	}
}
