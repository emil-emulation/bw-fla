/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The Emulation-as-a-Service framework is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package de.bwl.bwfla.common.jaxwsstubs.imagearchive;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * This class was generated by Apache CXF 2.4.6
 * 2015-08-08T17:29:21.670+02:00
 * Generated source version: 2.4.6
 * 
 */
@WebService(targetNamespace = "http://interfaces.common.bwfla.bwl.de/", name = "ImageArchiveWSRemote")
public interface ImageArchiveWSRemote {

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "getRecording", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.imagearchive.GetRecording")
    @WebMethod
    @ResponseWrapper(localName = "getRecordingResponse", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.imagearchive.GetRecordingResponse")
    public java.lang.String getRecording(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        java.lang.String arg1
    );

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "releaseImage", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.imagearchive.ReleaseImage")
    @WebMethod
    @ResponseWrapper(localName = "releaseImageResponse", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.imagearchive.ReleaseImageResponse")
    public boolean releaseImage(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0
    );

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "getRecordings", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.imagearchive.GetRecordings")
    @WebMethod
    @ResponseWrapper(localName = "getRecordingsResponse", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.imagearchive.GetRecordingsResponse")
    public java.util.List<de.bwl.bwfla.common.jaxwsstubs.imagearchive.IwdMetaData> getRecordings(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0
    );

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "registerImageUsingFile", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.imagearchive.RegisterImageUsingFile")
    @WebMethod
    @ResponseWrapper(localName = "registerImageUsingFileResponse", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.imagearchive.RegisterImageUsingFileResponse")
    public java.lang.String registerImageUsingFile(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        java.lang.String arg1,
        @WebParam(name = "arg2", targetNamespace = "")
        boolean arg2,
        @WebParam(name = "arg3", targetNamespace = "")
        java.lang.String arg3
    );

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "getImageById", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.imagearchive.GetImageById")
    @WebMethod
    @ResponseWrapper(localName = "getImageByIdResponse", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.imagearchive.GetImageByIdResponse")
    public java.lang.String getImageById(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0
    );

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "publishImage", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.imagearchive.PublishImage")
    @WebMethod
    @ResponseWrapper(localName = "publishImageResponse", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.imagearchive.PublishImageResponse")
    public java.lang.String publishImage(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        java.lang.String arg1
    );

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "getIncomingImageList", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.imagearchive.GetIncomingImageList")
    @WebMethod
    @ResponseWrapper(localName = "getIncomingImageListResponse", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.imagearchive.GetIncomingImageListResponse")
    public java.util.List<java.lang.String> getIncomingImageList();

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "registerImage", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.imagearchive.RegisterImage")
    @WebMethod
    @ResponseWrapper(localName = "registerImageResponse", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.imagearchive.RegisterImageResponse")
    public java.lang.String registerImage(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        javax.activation.DataHandler arg1,
        @WebParam(name = "arg2", targetNamespace = "")
        java.lang.String arg2
    );

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "getImages", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.imagearchive.GetImages")
    @WebMethod
    @ResponseWrapper(localName = "getImagesResponse", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.imagearchive.GetImagesResponse")
    public java.util.List<java.lang.String> getImages(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0
    );

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "getTemplates", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.imagearchive.GetTemplates")
    @WebMethod
    @ResponseWrapper(localName = "getTemplatesResponse", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.imagearchive.GetTemplatesResponse")
    public java.util.List<java.lang.String> getTemplates();

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "addRecordingFile", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.imagearchive.AddRecordingFile")
    @WebMethod
    @ResponseWrapper(localName = "addRecordingFileResponse", targetNamespace = "http://interfaces.common.bwfla.bwl.de/", className = "de.bwl.bwfla.common.jaxwsstubs.imagearchive.AddRecordingFileResponse")
    public boolean addRecordingFile(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        java.lang.String arg1,
        @WebParam(name = "arg2", targetNamespace = "")
        java.lang.String arg2
    );
}
