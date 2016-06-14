package de.bwl.bwfla.common.datatypes;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.transform.stream.StreamSource;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fileCollection", propOrder = {"files", "label"}, namespace="http://bwfla.bwl.de/common/datatypes")
@XmlRootElement(namespace = "http://bwfla.bwl.de/common/datatypes")
public class FileCollection {
	@XmlElement(name="file", namespace="http://bwfla.bwl.de/common/datatypes")
	public List<FileCollectionEntry> files = new ArrayList<FileCollectionEntry>();

	@XmlElement(namespace="http://bwfla.bwl.de/common/datatypes")
	public String label;
	
	public FileCollection()
	{
		label = null;
	}
	
	public FileCollection(String label)
	{
		this.label = label;
	}
	
    public static FileCollection fromValue(String data) throws JAXBException {
    	JAXBContext jc = JAXBContext.newInstance(FileCollection.class);
    	Unmarshaller unmarshaller = jc.createUnmarshaller();
    	return (FileCollection)unmarshaller.unmarshal(new StreamSource(new StringReader(data)));
    }
    
    public String value() throws JAXBException {
    	JAXBContext jc = JAXBContext.newInstance(this.getClass());
    	Marshaller marshaller = jc.createMarshaller();
    	StringWriter w = new StringWriter();
    	marshaller.marshal(this, w);
    	return w.toString();
    }
    
    public FileCollection copy()
    {
    	try {
    		return fromValue(this.value());
    	}
    	catch(JAXBException e) { 
    		// impossible 
    		return null;
    	}
    }
}
