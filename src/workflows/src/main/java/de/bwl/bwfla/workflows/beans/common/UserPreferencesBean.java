package de.bwl.bwfla.workflows.beans.common;

public class UserPreferencesBean {
	private boolean enableCRTEmulationCheckbox = false;
	private String keyboardModel;
	private String keyboardLayout;

	public boolean isEnableCRTEmulationCheckbox() {
		return enableCRTEmulationCheckbox;
	}

	public void setEnableCRTEmulationCheckbox(boolean enableCRTEmulationCheckbox) {
		this.enableCRTEmulationCheckbox = enableCRTEmulationCheckbox;
	}

	public String getKeyboardModel() {
		return keyboardModel;
	}

	public void setKeyboardModel(String keyboardModel) {
		this.keyboardModel = keyboardModel;
	}

	public String getKeyboardLayout() {
		return keyboardLayout;
	}

	public void setKeyboardLayout(String keyboardLayout) {
		this.keyboardLayout = keyboardLayout;
	}
}
