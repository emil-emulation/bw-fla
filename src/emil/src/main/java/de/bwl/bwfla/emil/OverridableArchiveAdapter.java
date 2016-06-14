package de.bwl.bwfla.emil;

import java.util.List;

import de.bwl.bwfla.common.exceptions.BWFLAException;

public interface OverridableArchiveAdapter extends ArchiveAdapter {
    public void setEnvironmentsForObject(String objectId,
            List<EnvironmentInfo> environments) throws BWFLAException;
}
