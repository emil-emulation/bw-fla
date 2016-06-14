package de.bwl.bwfla.emil;

import javax.xml.bind.annotation.XmlRootElement;

import de.bwl.bwfla.common.utils.config.Configuration;

@XmlRootElement
public class EmilConf extends Configuration {
    private static final long serialVersionUID = 194946519L;
    
    public String               imageArchive;
    public String               objectArchive;
    public String               softwareArchive;
    public String               embedGw;
    public String               eaasGw;
    
    public String               cachedClassificationPath;
    public String               emilEnvironmentsPath;
}
