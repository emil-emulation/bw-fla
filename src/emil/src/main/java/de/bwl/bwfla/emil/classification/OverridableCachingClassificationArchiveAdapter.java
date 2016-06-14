package de.bwl.bwfla.emil.classification;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.NoSuchElementException;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emil.ArchiveAdapter;
import de.bwl.bwfla.emil.EmilSingleton;


public class OverridableCachingClassificationArchiveAdapter
        extends ClassificationMixinArchiveAdapter implements ArchiveAdapter {
    @Override
    public List<EnvironmentInfo> getEnvironmentsForObject(String objectId) throws BWFLAException {
        List<EnvironmentInfo> result = null;
        try {
            // let's see if we have a manual mapping for the object
            // This is also the classification cache. As we only classify
            // if there are no overrides, a classification becomes the new
            // "override" and will thus serve as a cache the next time this
            // function is called.
            result = super.getEnvironmentsForObject(objectId);
            if (result.size() == 0) {
                throw new NoSuchElementException();
            }
            return result;
        } catch (NoSuchElementException e) {
            // if no data for the object id, classify it
            try {
                List<EnvironmentInfo> environmentInfos = this.classifyObject(objectId);
                
                // write classifications information to file
                Path base = Paths.get(EmilSingleton.CONF.cachedClassificationPath);
                Path objectCache = base.resolve(objectId);
                
                Files.write(objectCache, "".getBytes(),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
                for (EnvironmentInfo ei : environmentInfos) {
                    Files.write(objectCache,
                            ei.id.concat(" ").concat(ei.label).concat("\n")
                                    .getBytes(),
                            StandardOpenOption.APPEND);
                }
                
                return environmentInfos;
            } catch (Throwable t) {
                t.printStackTrace();
                throw new BWFLAException(t);
            }
        } catch (Throwable t) {
            // this indicated an existing override but in an invalid format
            throw t;
        }
    }
    
    

}
