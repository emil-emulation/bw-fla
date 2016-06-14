package de.bwl.bwfla.common.jaxwsstubs.objectarchive;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 2.4.6
 * 2015-11-23T11:52:33.846+01:00
 * Generated source version: 2.4.6
 * 
 */
@WebServiceClient(name = "ObjectArchiveFacadeWSService", 
                  wsdlLocation = "http://132.230.4.15:8080/object-archive/ObjectArchiveFacadeWS?wsdl",
                  targetNamespace = "http://objectarchive.bwfla.bwl.de/") 
public class ObjectArchiveFacadeWSService extends Service {

    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("http://objectarchive.bwfla.bwl.de/", "ObjectArchiveFacadeWSService");
    public final static QName ObjectArchiveFacadeWSPort = new QName("http://objectarchive.bwfla.bwl.de/", "ObjectArchiveFacadeWSPort");
    static {
        URL url = null;
        try {
            url = new URL("http://132.230.4.15:8080/object-archive/ObjectArchiveFacadeWS?wsdl");
        } catch (MalformedURLException e) {
            java.util.logging.Logger.getLogger(ObjectArchiveFacadeWSService.class.getName())
                .log(java.util.logging.Level.INFO, 
                     "Can not initialize the default wsdl from {0}", "http://132.230.4.15:8080/object-archive/ObjectArchiveFacadeWS?wsdl");
        }
        WSDL_LOCATION = url;
    }

    public ObjectArchiveFacadeWSService(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public ObjectArchiveFacadeWSService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public ObjectArchiveFacadeWSService() {
        super(WSDL_LOCATION, SERVICE);
    }
    
    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    public ObjectArchiveFacadeWSService(WebServiceFeature ... features) {
        super(WSDL_LOCATION, SERVICE, features);
    }

    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    public ObjectArchiveFacadeWSService(URL wsdlLocation, WebServiceFeature ... features) {
        super(wsdlLocation, SERVICE, features);
    }

    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    public ObjectArchiveFacadeWSService(URL wsdlLocation, QName serviceName, WebServiceFeature ... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     *
     * @return
     *     returns ObjectArchiveFacadeWSRemote
     */
    @WebEndpoint(name = "ObjectArchiveFacadeWSPort")
    public ObjectArchiveFacadeWSRemote getObjectArchiveFacadeWSPort() {
        return super.getPort(ObjectArchiveFacadeWSPort, ObjectArchiveFacadeWSRemote.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns ObjectArchiveFacadeWSRemote
     */
    @WebEndpoint(name = "ObjectArchiveFacadeWSPort")
    public ObjectArchiveFacadeWSRemote getObjectArchiveFacadeWSPort(WebServiceFeature... features) {
        return super.getPort(ObjectArchiveFacadeWSPort, ObjectArchiveFacadeWSRemote.class, features);
    }

}
