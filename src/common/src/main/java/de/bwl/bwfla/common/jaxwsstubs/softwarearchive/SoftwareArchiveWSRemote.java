package de.bwl.bwfla.common.jaxwsstubs.softwarearchive;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * This class was generated by Apache CXF 2.4.6
 * 2016-04-26T15:58:49.441+02:00
 * Generated source version: 2.4.6
 * 
 */
@WebService(targetNamespace = "http://interfaces.common.bwfla.bwl.de/", name = "SoftwareArchiveWSRemote")
public interface SoftwareArchiveWSRemote
{
    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "getSoftwareDescriptions", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.softwarearchive.GetSoftwareDescriptions")
    @WebMethod
    @ResponseWrapper(localName = "getSoftwareDescriptionsResponse", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.softwarearchive.GetSoftwareDescriptionsResponse")
    public java.util.List<de.bwl.bwfla.common.datatypes.SoftwareDescription> getSoftwareDescriptions();

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "getName", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.softwarearchive.GetName")
    @WebMethod
    @ResponseWrapper(localName = "getNameResponse", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.softwarearchive.GetNameResponse")
    public java.lang.String getName();

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "getSoftwarePackages", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.softwarearchive.GetSoftwarePackages")
    @WebMethod
    @ResponseWrapper(localName = "getSoftwarePackagesResponse", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.softwarearchive.GetSoftwarePackagesResponse")
    public java.util.List<java.lang.String> getSoftwarePackages();

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "getSoftwareDescriptionById", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.softwarearchive.GetSoftwareDescriptionById")
    @WebMethod
    @ResponseWrapper(localName = "getSoftwareDescriptionByIdResponse", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.softwarearchive.GetSoftwareDescriptionByIdResponse")
    public de.bwl.bwfla.common.datatypes.SoftwareDescription getSoftwareDescriptionById(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0
    );

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "getSoftwarePackageById", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.softwarearchive.GetSoftwarePackageById")
    @WebMethod
    @ResponseWrapper(localName = "getSoftwarePackageByIdResponse", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.softwarearchive.GetSoftwarePackageByIdResponse")
    public de.bwl.bwfla.common.datatypes.SoftwarePackage getSoftwarePackageById(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0
    );
    
    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "getNumSoftwareSeatsById", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.softwarearchive.GetNumSoftwareSeatsById")
    @WebMethod
    @ResponseWrapper(localName = "getNumSoftwareSeatsByIdResponse", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.softwarearchive.GetNumSoftwareSeatsByIdResponse")
    public int getNumSoftwareSeatsById(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0
    );
}