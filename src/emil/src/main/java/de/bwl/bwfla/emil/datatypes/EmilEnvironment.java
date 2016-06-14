package de.bwl.bwfla.emil.datatypes;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmilEnvironment implements Comparable{
	private String parentEnvId;
	private String envId;
	private String os;
	private String title;
	private String description;
	private String version;
	private String status;
	private String emulator;
	private boolean objectEnvironment = false;
	
	
	private List<String> installedSoftwareIds = new ArrayList<String>();
	
	public String getParentEnvId() {
		return parentEnvId;
	}
	public void setParentEnvId(String envId) {
		this.parentEnvId = envId;
	}
	public String getEnvId() {
		return envId;
	}
	public void setEnvId(String envId) {
		this.envId = envId;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getOs() {
		return os;
	}
	public void setOs(String os) {
		this.os = os;
	}
	public String getEmulator() {
		return emulator;
	}
	public void setEmulator(String emulator) {
		this.emulator = emulator;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public List<String> getInstalledSoftwareIds() {
		return installedSoftwareIds;
	}
	public void setInstalledSoftwareIds(List<String> ids) {
		this.installedSoftwareIds = ids;
	}
	@Override
	public int compareTo(Object o) {
		return title.compareTo(((EmilEnvironment)o).title);
	}
	public boolean isObjectEnvironment() {
		return objectEnvironment;
	}
	public void setObjectEnvironment(boolean objectEnvironment) {
		this.objectEnvironment = objectEnvironment;
	}
}
