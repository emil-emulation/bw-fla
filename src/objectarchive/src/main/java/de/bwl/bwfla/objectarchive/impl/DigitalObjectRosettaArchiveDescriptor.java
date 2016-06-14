package de.bwl.bwfla.objectarchive.impl;


public class DigitalObjectRosettaArchiveDescriptor extends DigitalObjectArchiveDescriptor {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String url; 
	
	
	public DigitalObjectRosettaArchiveDescriptor()
	{
		setType(ArchiveType.EMIL_ROSETTA);
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
}
