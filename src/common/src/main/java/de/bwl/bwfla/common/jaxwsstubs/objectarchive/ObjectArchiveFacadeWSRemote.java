package de.bwl.bwfla.common.jaxwsstubs.objectarchive;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * This class was generated by Apache CXF 2.4.6
 * 2015-11-23T11:52:33.802+01:00
 * Generated source version: 2.4.6
 * 
 */
@WebService(targetNamespace = "http://interfaces.common.bwfla.bwl.de/", name = "ObjectArchiveFacadeWSRemote")
public interface ObjectArchiveFacadeWSRemote {

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "getArchives", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.objectarchive.GetArchives")
    @WebMethod
    @ResponseWrapper(localName = "getArchivesResponse", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.objectarchive.GetArchivesResponse")
    public java.util.List<java.lang.String> getArchives();

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "getObjectReference", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.objectarchive.GetObjectReference")
    @WebMethod
    @ResponseWrapper(localName = "getObjectReferenceResponse", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.objectarchive.GetObjectReferenceResponse")
    public java.lang.String getObjectReference(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        java.lang.String arg1
    );

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "getObjectList", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.objectarchive.GetObjectList")
    @WebMethod
    @ResponseWrapper(localName = "getObjectListResponse", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.objectarchive.GetObjectListResponse")
    public java.util.List<java.lang.String> getObjectList(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0
    );
}
