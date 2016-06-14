package de.bwl.bwfla.emil;

import java.util.List;
import java.util.NoSuchElementException;

import javax.persistence.Entity;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import de.bwl.bwfla.common.datatypes.FileCollection;
import de.bwl.bwfla.common.exceptions.BWFLAException;

public interface ArchiveAdapter {
    public String getFileCollectionForObject(String objectId) throws NoSuchElementException, BWFLAException;
    public String getMetadataForObject(String objectId) throws NoSuchElementException, BWFLAException;
    public List<EnvironmentInfo> getEnvironmentsForObject(String objectId) throws NoSuchElementException, BWFLAException;
    
    @Entity
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class EnvironmentInfo {
        public EnvironmentInfo(String _id, String _label) {
            this.id = _id;
            this.label = _label;
        }
        EnvironmentInfo() {
        }
        public String id;
        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
        public String getLabel() {
            return label;
        }
        public void setLabel(String label) {
            this.label = label;
        }
        public String label;
    }
}
