package de.bwl.bwfla.emil.classification;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import de.bwl.bwfla.classification.ImageClassificationHelper;
import de.bwl.bwfla.classification.ImageClassificationHelper.EmuEnvType;
import de.bwl.bwfla.common.datatypes.EmulationEnvironment;
import de.bwl.bwfla.common.datatypes.FileCollection;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.SystemEnvironmentHelper;
import de.bwl.bwfla.emil.EmilSingleton;

public class ClassificationMixinArchiveAdapter
        extends ManualMappingArchiveAdapter {
    protected SystemEnvironmentHelper envHelper = new SystemEnvironmentHelper(EmilSingleton.CONF.imageArchive);
    
    
    
    
    
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
        ImageClassificationHelper.getEnvironmentForEmuEnvType(envs);


        List<EnvironmentInfo> result = new ArrayList<EnvironmentInfo>();
        for (EmuEnvType type : envs) {
            switch (type) {
            case EMUENV_DOS:
                result.addAll(getEnvironmentsForEnvType("dos"));
                break;
            case EMUENV_WIN311:
                result.addAll(getEnvironmentsForEnvType("win311"));
                break;
            case EMUENV_WIN9X:
                result.addAll(getEnvironmentsForEnvType("win9x"));
                break;
            case EMUENV_WINXP:
                result.addAll(getEnvironmentsForEnvType("winxp"));
                break;
            case EMUENV_MACPPC:
                result.addAll(getEnvironmentsForEnvType("macppc"));
                break;
            case EMUENV_MACMK68:
                result.addAll(getEnvironmentsForEnvType("macm68k"));
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

    protected List<EnvironmentInfo> getEnvironmentsForEnvType(String envType) throws BWFLAException {
        List<EnvironmentInfo> result = new ArrayList<EnvironmentInfo>();
        List<EmulationEnvironment> environments = envHelper.getEnvironments();

        for (EmulationEnvironment environment : environments) {
            try {
                if (environment.getDescription().getOs().equals(envType)) {
                    result.add(new EnvironmentInfo(environment.getId(), environment.getDescription().getTitle()));
                }
            } catch (NullPointerException e) {
                continue;
            }
        }
        return result;
    }
}
