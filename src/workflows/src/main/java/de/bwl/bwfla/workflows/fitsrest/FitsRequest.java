package de.bwl.bwfla.workflows.fitsrest;

import javax.persistence.Entity;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;



@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class FitsRequest
{
	private String	xmlId;

	@JsonProperty("xmlId")
	public String getXmlId()
	{
		return xmlId;
	}

	public void setXmlId(String xmlId)
	{
		this.xmlId = xmlId;
	}
}