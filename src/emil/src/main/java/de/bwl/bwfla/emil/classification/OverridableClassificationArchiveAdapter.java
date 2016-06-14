package de.bwl.bwfla.emil.classification;

import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.bind.JAXBException;

import de.bwl.bwfla.common.exceptions.BWFLAException;

public class OverridableClassificationArchiveAdapter extends ClassificationMixinArchiveAdapter {
    @Override
    public List<EnvironmentInfo> getEnvironmentsForObject(String objectId)
            throws NoSuchElementException, BWFLAException {
        List<EnvironmentInfo> environments = super.getEnvironmentsForObject(objectId);
        if (environments.size() == 0) {
            // there are no overrides, let's classify the object now
            try {
                environments = this.classifyObject(objectId);
            } catch (JAXBException e) {
                throw new BWFLAException(e);
            }
        }
        
        return environments;
    }
}
