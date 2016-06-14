package de.bwl.bwfla.common.datatypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "archiveBinding", namespace = "http://bwfla.bwl.de/common/datatypes", propOrder = {
	    "archiveHost",
	    "objectId",
	    "archive"
	})
@XmlRootElement(namespace = "http://bwfla.bwl.de/common/datatypes")
public class ArchiveBinding extends AbstractDataResource{
	 @XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes", required = true)
	  protected String archiveHost;
	 
	 @XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes", required = true)
	 protected String objectId;
	 
	 @XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes", required = true)
	 protected String archive;

	public ArchiveBinding()
	{
		archiveHost = null;
		objectId = null;
		archive = null;
	}

	public String getArchive() {
		return archive;
	}

	public void setArchive(String archive) {
		this.archive = archive;
	}

	public ArchiveBinding(String host, String archive, String objectId) {
		this.archiveHost = host;
		this.archive = archive;
		this.objectId = objectId;
	}

	public String getArchiveHost() {
		return archiveHost;
	}

	public void setArchiveHost(String archiveHost) {
		this.archiveHost = archiveHost;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}
}
