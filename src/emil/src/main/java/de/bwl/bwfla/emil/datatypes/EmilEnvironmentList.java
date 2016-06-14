package de.bwl.bwfla.emil.datatypes;

import java.util.ArrayList;
import java.util.Collections;

import javax.persistence.Entity;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmilEnvironmentList {
	private String status;
	private ArrayList<EmilEnvironment> environments;
	
	public EmilEnvironmentList()
	{
		environments = new ArrayList<EmilEnvironment>();
	}

	public ArrayList<EmilEnvironment> getEnvironments() {
		return environments;
	}

	public void setEnvironments(ArrayList<EmilEnvironment> environments) {
		this.environments = environments;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public void sort()
	{
		Collections.sort(environments);
	}
}
