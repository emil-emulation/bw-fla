package de.bwl.bwfla.workflows.beans.common;

import java.util.List;

public class ChosenMediaForDeviceBean {
	/**
	 * Create a new ChosenMediaForDeviceBean
	 * @param deviceName Name of the device e.g. "Floppy".
	 * @param chosenMedia Pre-selected value of the availableMedia values, null if none should be pre selected.
	 * @param availableMedia List of the available media for this device.
	 */
	public ChosenMediaForDeviceBean(String deviceName, String chosenMedia, List<String> availableMedia) {
		this.deviceName = deviceName;
		this.chosenMedia = chosenMedia;
		this.availableMedia = availableMedia;
	}

	private String deviceName;
	private String chosenMedia;
	private List<String> availableMedia;
	private boolean changed = false;

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getChosenMedia() {
		return chosenMedia;
	}

	public void setChosenMedia(String chosenMedia) {
	    if (!chosenMedia.isEmpty() && !chosenMedia.equals(this.chosenMedia)) {
	        changed = true;
	    }
		this.chosenMedia = chosenMedia;
	}

	public List<String> getAvailableMedia() {
		return availableMedia;
	}

	public void setAvailableMedia(List<String> availableMedia) {
		this.availableMedia = availableMedia;
	}
	
	public boolean isChanged() {
	    return this.changed;
	}

    public void resetChanged() {
        this.changed = false;        
    }
}
