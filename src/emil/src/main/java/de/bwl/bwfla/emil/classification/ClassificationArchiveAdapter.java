package de.bwl.bwfla.emil.classification;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.bind.JAXBException;

import de.bwl.bwfla.classification.ImageClassificationHelper;
import de.bwl.bwfla.classification.ImageClassificationHelper.EmuEnvType;
import de.bwl.bwfla.common.datatypes.FileCollection;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.ObjectArchiveHelper;
import de.bwl.bwfla.emil.ArchiveAdapter;
import de.bwl.bwfla.emil.EmilSingleton;
import de.bwl.bwfla.emil.ArchiveAdapter.EnvironmentInfo;

public class ClassificationArchiveAdapter implements ArchiveAdapter {
    protected static ObjectArchiveHelper objHelper = new ObjectArchiveHelper(EmilSingleton.CONF.objectArchive);
    
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
        try {
            return classifyObject(objectId);
        } catch (Throwable t) {
            // TODO Auto-generated catch block
            t.printStackTrace();
            throw new BWFLAException(t);
        } 
    }

    /**
     * @param objectId
     * @return
     * @throws JAXBException
     * @throws BWFLAException
     */
    protected List<EnvironmentInfo> classifyObject(String objectId)
            throws JAXBException, BWFLAException {
        FileCollection fc = FileCollection.fromValue(this.getFileCollectionForObject(objectId));
        List<EmuEnvType> envs = ImageClassificationHelper.classify(fc);
        System.out.println("Result of characterisation:");
        for (EmuEnvType type : envs) {
            System.out.println(type.toString());
        }
        ImageClassificationHelper.getEnvironmentForEmuEnvType(envs);

        List<EnvironmentInfo> result = new ArrayList<EnvironmentInfo>();
        for (EmuEnvType type : envs) {
            switch (type) {
            case EMUENV_DOS:
                result.add(new EnvironmentInfo("2010", "MS DOS 6.20 with MSCDEX on Qemu"));
                break;
            case EMUENV_WIN311:
                result.add(new EnvironmentInfo("2012", "Win 3.11 on VirtualBox"));
                break;
            case EMUENV_WIN9X:
                result.add(new EnvironmentInfo("4404", "Win 98 (SE) on VBox"));
                break;
            case EMUENV_WINXP:
                result.add(new EnvironmentInfo("4003", "Win XP Pro 32bit english on VBox"));
                break;
            case EMUENV_MACPPC:
                result.add(new EnvironmentInfo("3004", "Mac OS 9 on Sheepshaver"));
                break;
            case EMUENV_MACMK68:
                result.add(new EnvironmentInfo("1005", "Mac OS 7.5 on BasiliskII"));
                break;
            case EMUENV_UNKNOWN:
            case EMUENV_UNIX32LE:
            case EMUENV_UNIX64LE:
            case EMUENV_UNIX32BE:
            case EMUENV_UNIX64BE:
            case EMUENV_OSXMAC32:
            case EMUENV_OSXMAC64:
                break;
            }
        }
        return result;
    }
}
