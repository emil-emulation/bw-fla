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

package de.bwl.bwfla.api.eaas;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import de.bwl.bwfla.common.exceptions.BWFLAException;

/**
 * This class was generated by Apache CXF 2.4.6
 * 2015-05-13T18:54:24.173+02:00
 * Generated source version: 2.4.6
 * 
 */
@WebService(targetNamespace = "http://bwfla.bwl.de/api/eaas", name = "EaasWS")
public interface EaasWS {

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "getSessionState", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.GetSessionState")
    @WebMethod
    @ResponseWrapper(localName = "getSessionStateResponse", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.GetSessionStateResponse")
    public java.lang.String getSessionState(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0
    ) throws BWFLAException;

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "stop", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.Stop")
    @WebMethod
    @ResponseWrapper(localName = "stopResponse", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.StopResponse")
    public boolean stop(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0
    ) throws BWFLAException;

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "getRuntimeConfiguration", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.GetRuntimeConfiguration")
    @WebMethod
    @ResponseWrapper(localName = "getRuntimeConfigurationResponse", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.GetRuntimeConfigurationResponse")
    public java.lang.String getRuntimeConfiguration(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0
    ) throws BWFLAException;

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "updateMonitorValues", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.UpdateMonitorValues")
    @WebMethod
    @ResponseWrapper(localName = "updateMonitorValuesResponse", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.UpdateMonitorValuesResponse")
    public boolean updateMonitorValues(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0
    ) throws BWFLAException;

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "isReplayModeEnabled", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.IsReplayModeEnabled")
    @WebMethod
    @ResponseWrapper(localName = "isReplayModeEnabledResponse", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.IsReplayModeEnabledResponse")
    public boolean isReplayModeEnabled(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0
    ) throws BWFLAException;

    @RequestWrapper(localName = "takeScreenshot", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.TakeScreenshot")
    @WebMethod
    @ResponseWrapper(localName = "takeScreenshotResponse", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.TakeScreenshotResponse")
    public void takeScreenshot(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0
    ) throws BWFLAException;

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "getSessionPlayerProgress", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.GetSessionPlayerProgress")
    @WebMethod
    @ResponseWrapper(localName = "getSessionPlayerProgressResponse", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.GetSessionPlayerProgressResponse")
    public int getSessionPlayerProgress(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0
    ) throws BWFLAException;

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "attachMedium", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.AttachMedium")
    @WebMethod
    @ResponseWrapper(localName = "attachMediumResponse", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.AttachMediumResponse")
    public int attachMedium(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        javax.activation.DataHandler arg1,
        @WebParam(name = "arg2", targetNamespace = "")
        java.lang.String arg2
    ) throws BWFLAException;

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "isRecordModeEnabled", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.IsRecordModeEnabled")
    @WebMethod
    @ResponseWrapper(localName = "isRecordModeEnabledResponse", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.IsRecordModeEnabledResponse")
    public boolean isRecordModeEnabled(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0
    ) throws BWFLAException;

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "getHotplugableDrives", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.GetHotplugableDrives")
    @WebMethod
    @ResponseWrapper(localName = "getHotplugableDrivesResponse", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.GetHotplugableDrivesResponse")
    public java.util.List<java.lang.String> getHotplugableDrives(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0
    ) throws BWFLAException;

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "createSession", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.CreateSession")
    @WebMethod
    @ResponseWrapper(localName = "createSessionResponse", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.CreateSessionResponse")
    public java.lang.String createSession(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        java.lang.String arg1,
        @WebParam(name = "arg2", targetNamespace = "")
        java.lang.String arg2
    );

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "getAllMonitorValues", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.GetAllMonitorValues")
    @WebMethod
    @ResponseWrapper(localName = "getAllMonitorValuesResponse", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.GetAllMonitorValuesResponse")
    public java.util.List<java.lang.String> getAllMonitorValues(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0
    ) throws BWFLAException;

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "prepareSessionRecorder", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.PrepareSessionRecorder")
    @WebMethod
    @ResponseWrapper(localName = "prepareSessionRecorderResponse", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.PrepareSessionRecorderResponse")
    public boolean prepareSessionRecorder(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0
    ) throws BWFLAException;

    @RequestWrapper(localName = "addActionFinishedMark", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.AddActionFinishedMark")
    @WebMethod
    @ResponseWrapper(localName = "addActionFinishedMarkResponse", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.AddActionFinishedMarkResponse")
    public void addActionFinishedMark(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0
    ) throws BWFLAException;

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "changeMedium", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.ChangeMedium")
    @WebMethod
    @ResponseWrapper(localName = "changeMediumResponse", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.ChangeMediumResponse")
    public int changeMedium(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        int arg1,
        @WebParam(name = "arg2", targetNamespace = "")
        java.lang.String arg2
    ) throws BWFLAException;

    @RequestWrapper(localName = "releaseSession", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.ReleaseSession")
    @WebMethod
    @ResponseWrapper(localName = "releaseSessionResponse", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.ReleaseSessionResponse")
    public void releaseSession(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0
    );

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "getNextScreenshot", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.GetNextScreenshot")
    @WebMethod
    @ResponseWrapper(localName = "getNextScreenshotResponse", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.GetNextScreenshotResponse")
    public javax.activation.DataHandler getNextScreenshot(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0
    ) throws BWFLAException;

    @RequestWrapper(localName = "stopSessionRecording", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.StopSessionRecording")
    @WebMethod
    @ResponseWrapper(localName = "stopSessionRecordingResponse", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.StopSessionRecordingResponse")
    public void stopSessionRecording(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0
    ) throws BWFLAException;

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "getColdplugableDrives", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.GetColdplugableDrives")
    @WebMethod
    @ResponseWrapper(localName = "getColdplugableDrivesResponse", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.GetColdplugableDrivesResponse")
    public java.util.List<java.lang.String> getColdplugableDrives(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0
    ) throws BWFLAException;

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "getMonitorValue", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.GetMonitorValue")
    @WebMethod
    @ResponseWrapper(localName = "getMonitorValueResponse", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.GetMonitorValueResponse")
    public java.lang.String getMonitorValue(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        java.lang.Integer arg1
    ) throws BWFLAException;

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "getSessionTrace", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.GetSessionTrace")
    @WebMethod
    @ResponseWrapper(localName = "getSessionTraceResponse", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.GetSessionTraceResponse")
    public java.lang.String getSessionTrace(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0
    ) throws BWFLAException;

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "prepareSessionPlayer", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.PrepareSessionPlayer")
    @WebMethod
    @ResponseWrapper(localName = "prepareSessionPlayerResponse", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.PrepareSessionPlayerResponse")
    public boolean prepareSessionPlayer(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        java.lang.String arg1,
        @WebParam(name = "arg2", targetNamespace = "")
        boolean arg2
    ) throws BWFLAException;

    @RequestWrapper(localName = "connectNic", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.ConnectNic")
    @WebMethod
    @ResponseWrapper(localName = "connectNicResponse", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.ConnectNicResponse")
    public void connectNic(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        java.lang.String arg1,
        @WebParam(name = "arg2", targetNamespace = "")
        java.lang.String arg2
    ) throws BWFLAException;

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "getMonitorValues", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.GetMonitorValues")
    @WebMethod
    @ResponseWrapper(localName = "getMonitorValuesResponse", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.GetMonitorValuesResponse")
    public java.util.List<java.lang.String> getMonitorValues(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        java.util.List<java.lang.Integer> arg1
    ) throws BWFLAException;

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "getControlURL", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.GetControlURL")
    @WebMethod
    @ResponseWrapper(localName = "getControlURLResponse", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.GetControlURLResponse")
    public java.lang.String getControlURL(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        de.bwl.bwfla.api.eaas.ConnectionType arg1,
        @WebParam(name = "arg2", targetNamespace = "")
        de.bwl.bwfla.api.eaas.AbstractCredentials arg2
    ) throws BWFLAException;

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "saveEnvironment", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.SaveEnvironment")
    @WebMethod
    @ResponseWrapper(localName = "saveEnvironmentResponse", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.SaveEnvironmentResponse")
    public java.lang.String saveEnvironment(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        java.lang.String arg1,
        @WebParam(name = "arg2", targetNamespace = "")
        java.lang.String arg2,
        @WebParam(name = "arg3", targetNamespace = "")
        java.lang.String arg3
    ) throws BWFLAException;

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "start", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.Start")
    @WebMethod
    @ResponseWrapper(localName = "startResponse", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.StartResponse")
    public boolean start(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0
    ) throws BWFLAException;

    @RequestWrapper(localName = "defineTraceMetadataChunk", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.DefineTraceMetadataChunk")
    @WebMethod
    @ResponseWrapper(localName = "defineTraceMetadataChunkResponse", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.DefineTraceMetadataChunkResponse")
    public void defineTraceMetadataChunk(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        java.lang.String arg1,
        @WebParam(name = "arg2", targetNamespace = "")
        java.lang.String arg2
    ) throws BWFLAException;

    @RequestWrapper(localName = "addTraceMetadataEntry", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.AddTraceMetadataEntry")
    @WebMethod
    @ResponseWrapper(localName = "addTraceMetadataEntryResponse", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.AddTraceMetadataEntryResponse")
    public void addTraceMetadataEntry(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        java.lang.String arg1,
        @WebParam(name = "arg2", targetNamespace = "")
        java.lang.String arg2,
        @WebParam(name = "arg3", targetNamespace = "")
        java.lang.String arg3
    ) throws BWFLAException;

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "detachMedium", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.DetachMedium")
    @WebMethod
    @ResponseWrapper(localName = "detachMediumResponse", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.DetachMediumResponse")
    public javax.activation.DataHandler detachMedium(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        int arg1
    ) throws BWFLAException;

    @RequestWrapper(localName = "startSessionRecording", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.StartSessionRecording")
    @WebMethod
    @ResponseWrapper(localName = "startSessionRecordingResponse", targetNamespace = "http://bwfla.bwl.de/api/eaas", className = "de.bwl.bwfla.api.eaas.StartSessionRecordingResponse")
    public void startSessionRecording(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0
    ) throws BWFLAException;
}
