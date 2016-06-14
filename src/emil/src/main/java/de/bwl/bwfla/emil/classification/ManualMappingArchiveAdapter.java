package de.bwl.bwfla.emil.classification;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import de.bwl.bwfla.common.datatypes.FileCollection;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.ObjectArchiveHelper;
import de.bwl.bwfla.emil.ArchiveAdapter;
import de.bwl.bwfla.emil.EmilSingleton;
import de.bwl.bwfla.emil.OverridableArchiveAdapter;

public class ManualMappingArchiveAdapter implements ArchiveAdapter, OverridableArchiveAdapter {
    protected static ObjectArchiveHelper objHelper = new ObjectArchiveHelper(EmilSingleton.CONF.objectArchive);

    protected final static Logger           LOG = Logger.getLogger(ManualMappingArchiveAdapter.class.getName());


    @Override
    public String getFileCollectionForObject(String objectId)
            throws NoSuchElementException, BWFLAException {
        try {
            FileCollection fc = objHelper.getObjectReference(objHelper.getArchives().get(0), objectId);
            if (fc == null)
                throw new BWFLAException("Returned FileCollection is null for '" + objectId + "'!");
            
            return fc.value();
            
        } catch (JAXBException e) {
            throw new BWFLAException("Cannot find object reference for '" + objectId + "'", e);
        }
    }

    @Override
    public String getMetadataForObject(String objectId)
            throws NoSuchElementException {
        String test = "{ \"status\": \"0\", \"title\": \"" + objectId + "\"}";
        return test; 
    }

    @Override
    public List<EnvironmentInfo> getEnvironmentsForObject(String objectId)
            throws NoSuchElementException, BWFLAException {
        Path base = Paths.get(EmilSingleton.CONF.cachedClassificationPath);
        Path objectCache = base.resolve(objectId);
        
        List<EnvironmentInfo> result = new ArrayList<EnvironmentInfo>();
        if (!Files.exists(objectCache)) {
            LOG.warning("No environment mapping data for object '" + objectId + "'");
            // return an empty list because we don't do characterisation here
            return result;
        }
        
        try {
            List<String> lines = Files.readAllLines(objectCache, StandardCharsets.UTF_8);
            for (String line : lines) {
                String[] data = line.split(" ", 2);
                
                result.add(new EnvironmentInfo(data[0], data[1]));
            }
            return result;
        } catch (IOException e) {
            throw new BWFLAException("An error occurred reading mapping database.", e);
        }
    }
    
    @Override
    public void setEnvironmentsForObject(String objectId, List<EnvironmentInfo> environments)
            throws BWFLAException {
        try {
            // write classifications information to file
            Path base = Paths.get(EmilSingleton.CONF.cachedClassificationPath);
            Path objectCache = base.resolve(objectId);
            
            Files.write(objectCache, "".getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            for (EnvironmentInfo ei : environments) {
                Files.write(objectCache,
                        ei.id.concat(" ").concat(ei.label).concat("\n")
                                .getBytes(),
                        StandardOpenOption.APPEND);
            }
        } catch (Throwable t) {
            throw new BWFLAException(t);
        }
    }
}
