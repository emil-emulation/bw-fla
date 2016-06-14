package de.bwl.bwfla.emil.datatypes;

import java.util.List;

import de.bwl.bwfla.emil.ArchiveAdapter.EnvironmentInfo;

public class OverrideCharacterizationRequest {
    protected String objectId;
    public String getObjectId() {
        return objectId;
    }
    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }
    public List<EnvironmentInfo> getEnvironments() {
        return environments;
    }
    public void setEnvironments(List<EnvironmentInfo> environments) {
        this.environments = environments;
    }
    protected List<EnvironmentInfo> environments;
}
