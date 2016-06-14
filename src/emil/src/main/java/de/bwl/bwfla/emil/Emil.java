package de.bwl.bwfla.emil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.specimpl.ResponseBuilderImpl;

import de.bwl.bwfla.api.eaas.EaasWS;
import de.bwl.bwfla.common.datatypes.Drive.DriveType;
import de.bwl.bwfla.common.datatypes.Drive;
import de.bwl.bwfla.common.datatypes.EaasState;
import de.bwl.bwfla.common.datatypes.EmulationEnvironment;
import de.bwl.bwfla.common.datatypes.Environment;
import de.bwl.bwfla.common.datatypes.FileCollection;
import de.bwl.bwfla.common.datatypes.FileCollectionEntry;
import de.bwl.bwfla.common.datatypes.SoftwareDescription;
import de.bwl.bwfla.common.datatypes.SoftwarePackage;
import de.bwl.bwfla.common.datatypes.utils.EmulationEnvironmentHelper;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.services.container.helpers.ContainerHelper;
import de.bwl.bwfla.common.services.container.helpers.ContainerHelperFactory;
import de.bwl.bwfla.common.services.container.types.Container;
import de.bwl.bwfla.common.services.container.types.HddContainer;
import de.bwl.bwfla.common.utils.EmulatorUtils;
import de.bwl.bwfla.common.utils.ObjectArchiveHelper;
import de.bwl.bwfla.common.utils.SoftwareArchiveHelper;
import de.bwl.bwfla.common.utils.SystemEnvironmentHelper;
import de.bwl.bwfla.common.utils.Zip32Utils;
import de.bwl.bwfla.emil.ArchiveAdapter.EnvironmentInfo;
import de.bwl.bwfla.emil.classification.OverridableCachingClassificationArchiveAdapter;
import de.bwl.bwfla.emil.datatypes.EmilEnvironment;
import de.bwl.bwfla.emil.datatypes.EmilEnvironmentList;
import de.bwl.bwfla.emil.datatypes.EnvironmentConfigurationDesc;
import de.bwl.bwfla.emil.datatypes.EnvironmentUpdateDescription;
import de.bwl.bwfla.emil.datatypes.FdmRequest;
import de.bwl.bwfla.emil.datatypes.FdmRequest.DataRef;
import de.bwl.bwfla.emil.datatypes.OverrideCharacterizationRequest;
import de.bwl.bwfla.emil.utils.JsonBuilder;
import de.bwl.bwfla.emil.datatypes.NewEnvironmentDesc;

@Path("Emil")
@ApplicationScoped
public class Emil
{
	private static final Logger LOG = Logger.getLogger(Emil.class.getName());
	
	public final static ArchiveAdapter archive = new OverridableCachingClassificationArchiveAdapter();

	/** Default buffer size for JSON responses (in chars). */
	private static final int DEFAULT_RESPONSE_CAPACITY = 512;

	/** Security options response */
	private static final Response WS_OPTIONS_RESPONSE = Response.ok().header("Access-Control-Allow-Origin", "*")
			.header("Access-Control-Allow-Headers", "origin, content-type, accept").build();
	
	/* ### Emil API ### */

	/**
	 * Looks up and returns a list of all digital objects as a JSON object:
	 * <pre>
	 * {
	 *      "status": "0",
	 *      "objects": [
	 *          {
	 *              "id": &ltObject's ID&gt,
	 *              "title": "Object's title",
	 *              "description": "Object's description",
	 *              "thumbnail": "Optional thumbnail URL"
	 *          },
	 *          ...
	 *      ]
	 * }
	 * </pre>
	 * 
	 * 
	 * @return A JSON object containing the list of all digital objects. 
	 */
	@GET
	@Path("/getObjectList")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getObjectList()
	{
		final ObjectArchiveHelper objHelper = EmilSingleton.objHelper;
		/*
		 * note: thumbnail parameter is optional 
		 {
  			"status": "0",
  			"objects": [
    			{"id": "xxx", "title": "abc", "description": "asdf"}, "thumbnail":"http://y.jpg"}, ... 
    		]
		 */
		
		try {
			String archiveName = objHelper.getArchives().get(0);
			List<String> objects = objHelper.getObjectList(archiveName);
			
			JsonBuilder json = new JsonBuilder(DEFAULT_RESPONSE_CAPACITY);
			json.beginObject();
			json.add("status", "0");
			json.name("objects");
			json.beginArray();
			for(String id : objects)
			{
				json.beginObject();
				json.add("id", id);
				json.add("title", id);
				json.add("description", id);
				json.endObject();
			}
			json.endArray();
			json.endObject();
			return Emil.createResponse(Status.OK, json.toString());
		}
		catch (Exception e)
		{
			return Emil.internalErrorResponse(e);
		}
	}
	
	/**
	 * Looks up and returns metadata for specified object.
	 * @param objectId The object's ID to look up metadata for.
	 * @return A JSON object with object's metadata when found, else an error message.
	 */
	@GET
	@Path("/getObjectMetadata")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getObjectMetadata(@QueryParam("objectId") String objectId)
	{
		try {
			return Emil.createResponse(Status.OK, archive.getMetadataForObject(objectId));
		}
		catch (NoSuchElementException e) {
			return Emil.errorMessageResponse("Could not locate specified object: '"	+ objectId + "'");
		}
		catch (Throwable t) {
			return Emil.internalErrorResponse(t);
		}
	}

	/** Deprecated! See Emil.getObjectEnvironments() */
	@GET
	@Path("/loadEnvs")
	@Produces(MediaType.APPLICATION_JSON)
	public Response loadEnvs(@QueryParam("objectId") String objectId)
	{
		// TODO: remove it!
		return this.getObjectEnvironments(objectId);
	}
	
	/**
	 * Looks up and returns environments for specified object.
	 * When the specified object is found, a JSON response will be returned, containing:
	 * <pre>
	 * {
	 *      "status": "0",
	 *      "environments": [
	 *          { "id": &ltEnvironment's ID&gt, "label": "Environment's label" },
	 *          ...
	 *      ]
	 * }
	 * </pre>
	 * 
	 * When the specified object is not found or an internal error occurs,
	 * a JSON response containing the corresponding message will be returned:
	 * <pre>
	 * {
	 *      "status": "1",
	 *      "message": "Error message."
	 * }
	 * </pre>
	 * 
	 * @param objectId The object's ID to look up environments for.
	 * @return A JSON object with supported environments when found, else an error message.
	 */
	@GET
	@Path("/getObjectEnvironments")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getObjectEnvironments(@QueryParam("objectId") String objectId)
	{
		try {
			List<EnvironmentInfo> environments = archive.getEnvironmentsForObject(objectId);
			JsonBuilder json = new JsonBuilder(DEFAULT_RESPONSE_CAPACITY);
			json.beginObject();
			json.add("status", "0");
			json.name("environments");
			json.beginArray();

			for (EnvironmentInfo env : environments) {
				json.beginObject();
				json.add("id", env.id);
				json.add("label", env.label);
				json.endObject();
			}

			json.endArray();
			json.endObject();
			json.finish();

			return Emil.createResponse(Status.OK, json.toString());
		}
		catch (NoSuchElementException e) {
			return Emil.errorMessageResponse("Could not locate specified object: '" + objectId + "'");
		}
		catch (Throwable t) {
			return Emil.internalErrorResponse(t);
		}
	}

	/**
	 * Creates and configures a new session using specified environment.
	 * When the session is successfully created, a JSON response will be returned, containing:
	 * <pre>
	 * {
	 *      "status": "0",
	 *      "id": &ltSession's ID&gt,
	 *      "iframeurl": &ltIFrame's URL&gt
	 * }
	 * </pre>
	 * 
	 * When an internal error occurs, a JSON response containing the corresponding message will be returned:
	 * <pre>
	 * {
	 *      "status": "1",
	 *      "message": "Error message."
	 * }
	 * </pre>
	 * 
	 * @param envId The environment's ID to use for session.
	 * @return A JSON object describing the created session, else an error message.
	 */
	@GET
	@Path("/configureEnv")
	@Produces(MediaType.APPLICATION_JSON)
	public Response configureEnv(@QueryParam("envId") String envId)
	{
		final SystemEnvironmentHelper environmentHelper = EmilSingleton.envHelper;
		try {
			Environment chosenEnv = environmentHelper.getPlatformById(envId);
			if(chosenEnv == null)
				return Emil.errorMessageResponse("could not find environment: " + envId);

			final EaasWS eaas = EmilSingleton.getEaasWS();
			final String sessionId = eaas.createSession(chosenEnv.toString(), null, "7777");
			if (sessionId == null)
				return Emil.internalErrorResponse("Session initialization has failed, obtained 'null' as session id.");

			JsonBuilder json = new JsonBuilder(DEFAULT_RESPONSE_CAPACITY);
			json.beginObject();
			json.add("status", "0");
			json.add("id", sessionId);
			json.add("type", "configure");
			json.add("iframeurl", EmilSingleton.CONF.embedGw + "?sessionId=" + sessionId);
			json.endObject();
			json.finish();

			return Emil.createResponse(Status.OK, json.toString());
		}
		catch (Throwable t) {
			return Emil.internalErrorResponse(t);
		}
	}

	/** Deprecated! See Emil.startEnvWithDigitalObject() */
	@GET
	@Path("/init")
	@Produces(MediaType.APPLICATION_JSON)
	public Response init(@QueryParam("objectId") String objectId, @QueryParam("envId") String envId)
	{
		// TODO: remove init() when the UI was updated with this API call!
		return this.startEnvWithDigitalObject(objectId, envId);
	}
	
	/**
	 * Creates and configures a new session using specified environment and digital object.
	 * When the session is successfully created, a JSON response will be returned, containing:
	 * <pre>
	 * {
	 *      "status": "0",
	 *      "id": &ltSession's ID&gt,
	 *      "driveId": &ltDrive's ID containing the digital object&gt,
	 *      "iframeurl": &ltIFrame's URL&gt,
	 *      "helpmsg": "Optional help information for media change."
	 * }
	 * </pre>
	 * 
	 * When an internal error occurs, a JSON response containing the corresponding message will be returned:
	 * <pre>
	 * {
	 *      "status": "1",
	 *      "message": "Error message."
	 * }
	 * </pre>
	 * 
	 * @param envId The environment's ID to use for session.
	 * @param objectId The object's ID to use for session.
	 * @return A JSON object describing the created session, else an error message.
	 */
	@GET
	@Path("/startEnvWithDigitalObject")
	@Produces(MediaType.APPLICATION_JSON)
	public Response startEnvWithDigitalObject(@QueryParam("envId") String envId, @QueryParam("objectId") String objectId)
	{
		final SystemEnvironmentHelper environmentHelper = EmilSingleton.envHelper;
		final ObjectArchiveHelper objHelper = EmilSingleton.objHelper;
		try {
			String chosenObjRef = archive.getFileCollectionForObject(objectId);
			FileCollection fc = FileCollection.fromValue(chosenObjRef);
			DriveType type = fc.files.get(0).getType();
			Environment chosenEnv = environmentHelper.getPlatformById(envId);
			if(chosenEnv == null)
				return Emil.errorMessageResponse("could not find environment: " + envId);

			String archiveName = objHelper.getArchives().get(0);   
			EmulationEnvironment objBoundEnv = (EmulationEnvironment)chosenEnv;

			int driveId = EmulationEnvironmentHelper.addArchiveBinding(
					objBoundEnv, EmilSingleton.CONF.objectArchive, archiveName, objectId, type);

			final EaasWS eaas = EmilSingleton.getEaasWS();
			final String sessionId = eaas.createSession(objBoundEnv.toString(), null, "7777");
			if (sessionId == null)
				return Emil.internalErrorResponse("Session initialization has failed, obtained 'null' as session id");

			JsonBuilder json = new JsonBuilder(DEFAULT_RESPONSE_CAPACITY);
			json.beginObject();
			json.add("status", "0");
			json.add("id", sessionId);
			json.add("driveId", Integer.toString(driveId));
			json.add("iframeurl", EmilSingleton.CONF.embedGw + "?sessionId=" + sessionId);
			if (driveId == -1) {
				String bean = objBoundEnv.getEmulator().getBean();
				String helpmsg = EmulationEnvironmentHelper.getMediaChangeHelp(bean);
				if (helpmsg != null)
					json.add("helpmsg", helpmsg);
			}

			json.endObject();
			json.finish();

			return Emil.createResponse(Status.OK, json.toString());
		}
		catch (NoSuchElementException e) {
			return Emil.errorMessageResponse("Could not locate specified object: '" + objectId + "'");
		}
		catch (Throwable t) {
			return Emil.internalErrorResponse(t);
		}
	}

	@GET
	@Path("/reboot")
	@Produces(MediaType.APPLICATION_JSON)
	public Response reboot(@QueryParam("sessionId") String sessionId) {
		// TODO: implement
		return Response.status(Status.OK)
				.header("Access-Control-Allow-Origin", "*")
				.entity("TODO: not implemented yet").build();
	}

	/**
	 * Takes and returns a screenshot of the running emulation session.
	 * @param sessionId The session ID to take the screenshot of.
	 * @return A HTML response containing the PNG encoded screenshot.
	 */
	@GET
	@Path("/screenshot")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response screenshot(@QueryParam("sessionId") String sessionId) {

		try {
			final EaasWS eaas = EmilSingleton.getEaasWS();
			String state = eaas.getSessionState(sessionId);

			if (!state.equalsIgnoreCase(EaasState.SESSION_RUNNING.value())) {
				return Response.status(Status.NOT_ACCEPTABLE).header("Access-Control-Allow-Origin", "*").build();
			} else {

				int numRetries = 20;
				DataHandler dh = null;
				eaas.takeScreenshot(sessionId);
				// Wait for the screenshot to become available
				while ((dh = eaas.getNextScreenshot(sessionId)) == null) {
					try {
						Thread.sleep(250L);
					}
					catch (InterruptedException exception) {
						exception.printStackTrace();
						return Response.status(Status.NOT_ACCEPTABLE).header("Access-Control-Allow-Origin", "*").build();
					}

					if (--numRetries < 0) {
						System.out.println("time out");
						return Response.status(Status.NOT_ACCEPTABLE).header("Access-Control-Allow-Origin", "*").build();
					}
				}
				InputStream is = dh.getInputStream();
				return Response.status(Status.OK).header("content-disposition", "attachment; filename=\"screenshot.png\"").header("Access-Control-Allow-Origin", "*").entity(is).build();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return Response.status(Status.SERVICE_UNAVAILABLE).entity(e.getMessage()).build();
		}
	}

	/**
	 * Looks up and returns a list of all Emil's environments as a JSON object:
	 * <pre>
	 * {
	 *      "status": "0",
	 *      "environments": [
	 *          {
	 *              "envId": &ltEnvironment's ID&gt,
	 *              "os": "Environment's OS name",
	 *              "title": "Environment's title",
	 *              "description": "Environment's description",
	 *              "version": "Environment's version",
	 *              "emulator": "Environment's emulator"
	 *          },
	 *          ...
	 *      ]
	 * }
	 * </pre>
	 * 
	 * 
	 * @return A JSON object containing the environment descriptions. 
	 */
	@GET
	@Path("/getEmilEnvironments")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEmilEnvironments()
	{
		final EmilEnvironmentList emilEnvList = new EmilEnvironmentList();
		final List<EmilEnvironment> environments = emilEnvList.getEnvironments();
		final java.nio.file.Path envArchivePath = Paths.get(EmilSingleton.CONF.emilEnvironmentsPath);
		try (DirectoryStream<java.nio.file.Path> files = Files.newDirectoryStream(envArchivePath)) {
			for (java.nio.file.Path envpath : files) {
				if (Files.isDirectory(envpath))
					continue;
				
				EmilEnvironment env = EmilUtils.getEmilEnvironmentByPath(envpath);
				if (env != null)
					environments.add(env);
			}
		}
		catch (Exception exception) {
			return Emil.internalErrorResponse(exception);
		}
		
		emilEnvList.setStatus("0");
		emilEnvList.sort();
		
		return Emil.createResponse(Status.OK, emilEnvList);
	}

	@OPTIONS
	@Path("/updateDescription")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateDescriptionOptions() {
		return WS_OPTIONS_RESPONSE;
	}

	/**
	 * Updates the description of a specified Emil environment.
	 * This method expects a JSON object containing the description changes:
	 * <pre>
	 * {
	 *      "envId": &ltEnvironment's ID&gt,
	 *      "title": "New title",
	 *      "description": "New description"
	 * }
	 * </pre>
	 * 
	 * When an internal error occurs, a JSON response containing
	 * the corresponding message will be returned:
	 * <pre>
	 * {
	 *      "status": "1",
	 *      "message": "Error message."
	 * }
	 * </pre>
	 * 
	 * @param desc A JSON object containing description changes.
	 * @return A JSON object containing the result message.
	 */
	@POST
	@Path("/updateDescription")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateDescription(EnvironmentUpdateDescription desc)
	{
		final String curEnvId = desc.getEnvId();
		final String basedir = EmilSingleton.CONF.emilEnvironmentsPath;
		EmilEnvironment newEmilEnv = EmilUtils.getEmilEnvironmentById(basedir, curEnvId);
		if (newEmilEnv == null)
			return Emil.errorMessageResponse("No emil environment found with ID: " + curEnvId);

		newEmilEnv.setTitle(desc.getTitle());
		newEmilEnv.setDescription(desc.getDescription());
		EmilUtils.saveEmilEnvironment(newEmilEnv, basedir);

		final String json = "{\"status\":\"0\"}";
		return Emil.createResponse(Status.OK, json);
	}

	@OPTIONS
	@Path("/saveEnvConfiguration")
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveEnvConfigrationOptions() {
		return WS_OPTIONS_RESPONSE;
	}	

	/**
	 * Stops the specified session and saves it's configuration as a new Emil environment.
	 * This method expects a JSON object containing:
	 * <pre>
	 * {
	 *      "sessionId": &ltSession's ID&gt,
	 *      "envId": &ltEnvironment's ID&gt,
	 *      "message": "Configuration description"
	 * }
	 * </pre>
	 * 
	 * When an internal error occurs, a JSON response containing
	 * the corresponding message will be returned:
	 * <pre>
	 * {
	 *      "status": "1",
	 *      "message": "Error message."
	 * }
	 * </pre>
	 * 
	 * @param desc The description of a new environment.
	 * @return A JSON object containing the result message.
	 */
	@POST
	@Path("/saveEnvConfiguration")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveEnvConfigration(EnvironmentConfigurationDesc desc)
	{
		try {
			final EaasWS eaas = EmilSingleton.getEaasWS();
			String state = eaas.getSessionState(desc.getSessionId());
			final String sessionId = desc.getSessionId();

			if (state.equalsIgnoreCase(EaasState.SESSION_RUNNING.value())) {

				eaas.stop(sessionId);

				String expState = EaasState.SESSION_STOPPED.value();
				for (int i = 0; i < 30; ++i) {
					state = eaas.getSessionState(sessionId);
					if (state.equalsIgnoreCase(expState))
						break;

					Thread.sleep(500);
				}
			} 

			state = eaas.getSessionState(sessionId);
			if (!state.equalsIgnoreCase(EaasState.SESSION_STOPPED.value()))
				return Emil.internalErrorResponse("Stopping session and saving changes failed!");

			final String curEnvId = desc.getEnvId();
			final String basedir = EmilSingleton.CONF.emilEnvironmentsPath;
			EmilEnvironment newEmilEnv = EmilUtils.getEmilEnvironmentById(basedir, curEnvId);
			if (newEmilEnv == null)
				return Emil.internalErrorResponse("No emil environment found with ID: " + curEnvId);

			final String imageArchiveHost = EmilSingleton.CONF.imageArchive;
			final String newEnvId = eaas.saveEnvironment(sessionId, imageArchiveHost, "Emil Derivate", "derivate");

			newEmilEnv.setParentEnvId(newEmilEnv.getEnvId());
			newEmilEnv.setEnvId(newEnvId);
			newEmilEnv.setDescription(desc.getMessage() + "\n--\n" + newEmilEnv.getDescription());
			EmilUtils.replaceEmilEnvironment(newEmilEnv, basedir);

			String message = "request for emulator stop is sent to session: " + desc.getSessionId();
			return Emil.successMessageResponse(message);
		}
		catch (Throwable throwable) {
			return Emil.internalErrorResponse(throwable);
		}
	}

	/**
	 * Stops the specified emulation session.
	 * @param sessionId The ID of the session to stop.
	 * @return A JSON response containing the result message.
	 */
	@GET
	@Path("/stop")
	@Produces(MediaType.APPLICATION_JSON)
	public Response stop(@QueryParam("sessionId") String sessionId)
	{
		try {
			final EaasWS eaas = EmilSingleton.getEaasWS();
			String state = eaas.getSessionState(sessionId);
			if (!state.equalsIgnoreCase(EaasState.SESSION_RUNNING.value())) {
				String message = "current state for the session '" + sessionId + "' is " 
						+ "'" + state + "' and not '" + EaasState.SESSION_RUNNING.value() + "'"
						+ ", will not send stop request";
				
				return Emil.errorMessageResponse(message);
			}
			else {
				eaas.stop(sessionId);
				
				String message = "request for emulator stop is sent to session: " + sessionId;
				return Emil.successMessageResponse(message);
			}
		}
		catch (Throwable throwable) {
			return Emil.internalErrorResponse(throwable);
		}
	}
	
	/** Deprecated! See Emil.getDigitalObjectMediaDescription() */
	@GET
	@Path("/getCollectionList")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getObjectFileCollection(@QueryParam("objectId") String objectId)
	{
		// TODO: remove this!
		return this.getDigitalObjectMediaDescription(objectId);
	}
	
	/**
	 * Returns a description of media corresponding to a specified digital object:
	 * <pre>
	 * {
	 *      "status": "0",
	 *      "media": [
	 *          {
	 *              "mediumtype": &ltMedium's type&gt,
	 *              "labels": [ "label-1", ..., "label-n" ]
	 *          },
	 *          ...
	 *      ]
	 * }
	 * </pre>
	 * 
	 * @param objectId The digital object's ID.
	 * @return A JSON response containing media description, or an error message.
	 */
	@GET
	@Path("/getDigitalObjectMediaDescription")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDigitalObjectMediaDescription(@QueryParam("objectId") String objectId)
	{
		try {
			final String chosenObjRef = archive.getFileCollectionForObject(objectId);
			if(chosenObjRef == null)
				return Emil.errorMessageResponse("no file collection found for object " + objectId);
			
			System.out.println("chosenObjRef " + chosenObjRef);
			FileCollection fc = FileCollection.fromValue(chosenObjRef);
			DriveType type = fc.files.get(0).getType();

			JsonBuilder json = new JsonBuilder(DEFAULT_RESPONSE_CAPACITY);
			json.beginObject();
			json.add("status", "0");
			json.name("media");
			json.beginArray();
			json.beginObject();
			json.add("mediumtype", type.name());
			json.name("labels");
			json.beginArray();
			for (FileCollectionEntry fce : fc.files)
				json.value(fce.getId());
			
			json.endArray();
			json.endObject();
			json.endArray();
			json.endObject();
			json.finish();
			
			return Emil.createResponse(Status.OK, json.toString());
		}
		catch (Exception exception) {
			return Emil.internalErrorResponse(exception);
		}
	}

	/**
	 * Changes digital object's medium in an emulation session. Since a digital object
	 * can be composed of multiple media, a medium's name must also be specified.
	 * @param sessionId The session's ID.
	 * @param driveId The drive's ID for changing medium.
	 * @param objectId The ID of a digital object.
	 * @param label The name of a new medium.
	 * @return A JSON response containing the result message.
	 */
	@GET
	@Path("/changeMedia")
	@Produces(MediaType.APPLICATION_JSON)
	public Response changeMedia(@QueryParam("sessionId") String sessionId, 
			@QueryParam("objectId") String objectId,
			@QueryParam("driveId") String driveId,
			@QueryParam("label") String label) 
	{
		String chosenObjRef;
		String objurl = null;
		try {
			chosenObjRef = archive.getFileCollectionForObject(objectId);
			if(chosenObjRef == null)
				return Emil.errorMessageResponse("no file collection found for object " + objectId);
			
			FileCollection fc = FileCollection.fromValue(chosenObjRef);
			for(FileCollectionEntry fce : fc.files)
				if(fce.getId().equals(label))
				{
					objurl = fce.getId();
					break;
				}
		} catch (NoSuchElementException | BWFLAException | JAXBException e1) {
			e1.printStackTrace();
			return Emil.internalErrorResponse("failed loading object meta data");
		}

		if(objurl == null)
			return Emil.internalErrorResponse("could not resolve object lable");

		try {
			final EaasWS eaas = EmilSingleton.getEaasWS();
			eaas.changeMedium(sessionId, Integer.parseInt(driveId), "binding://" + objectId + "/" + objurl);
		} catch (NumberFormatException | BWFLAException e) {
			e.printStackTrace();
			return Emil.internalErrorResponse("could not initialize eaas gateway: " + e.getMessage());
		}

		return Emil.successMessageResponse("");
	}

	@GET
	@Path("/fdmStopAndDownload")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response fdmStopAndDownload(@QueryParam("sessionId") String sessionId, 
			@QueryParam("containerId") String containerId) 
	{
		try {
			EaasWS eaas = EmulatorUtils.getEaas(new URL(EmilSingleton.CONF.eaasGw));
			String state = eaas.getSessionState(sessionId);

			if (!state.equalsIgnoreCase(EaasState.SESSION_RUNNING.value())) {
				return Response.status(Status.NOT_ACCEPTABLE).build();
			} else {
				eaas.stop(sessionId);

				for(int i = 0, DELAY_MS = 100; i < 5 * DELAY_MS; ++i) // wait for 5 sec
				{
					state = eaas.getSessionState(sessionId);
					if(state.equalsIgnoreCase(EaasState.SESSION_STOPPED.value()))
						break;

					Thread.sleep(DELAY_MS);
				}

				if(!state.equalsIgnoreCase(EaasState.SESSION_STOPPED.value()))
					Response.status(Status.SERVICE_UNAVAILABLE).entity("server temporarily lacks resources for request execution, please try later").build();


				File detachedContainerFile = File.createTempFile("detached_medium_", null);
				DataHandler dh = eaas.detachMedium(sessionId, Integer.parseInt(containerId));

				try(InputStream is = dh.getInputStream(); OutputStream os = new FileOutputStream(detachedContainerFile))
				{
					IOUtils.copy(is, os);
					os.flush();
				}

				HddContainer container = new HddContainer();
				container.setFile(detachedContainerFile);

				ContainerHelper helper = ContainerHelperFactory.getContainerHelper(container);
				if(helper == null)
				{
					return Response.status(Status.NO_CONTENT).build();
				}

				File filesDir = helper.extractFromContainer(container);
				if(filesDir == null)
				{
					return Response.status(Status.NO_CONTENT).build();
				}

				File[] files = filesDir.listFiles();
				File resultingFile = File.createTempFile("extracted_container_", ".zip");
				resultingFile.delete();
				if(files.length != 0)
				{		
					Zip32Utils.zip(resultingFile, filesDir);
					InputStream is = new FileInputStream(resultingFile);
					return Response.status(Status.OK).header("content-disposition", "attachment; filename=\"ext-disk.zip\"").entity(is).build();	
				}

			}
		} catch (Throwable t) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		return Response.status(Status.NO_CONTENT).build();
	}

	@POST
	@Path("/fdmInit")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	synchronized public static Response fdmInit(FdmRequest fdmRequest)
	{

		/*  {
		 *      "environment" : "test",
		 *      "files" : 
		 *          [
		 *             {"type":"plain", "url" : "test" }, 
		 *             {"type":"zip", "url" : "test" } 
		 *          ]
		 *  }
		 * 
		 */

		try
		{
			if(fdmRequest == null)
				return Response.status(Status.BAD_REQUEST).entity("request parameter missing").build();

			Map<String, String> sessionInfo = new HashMap<>();
			EaasWS eaas = null;

			SystemEnvironmentHelper environmentHelper = new SystemEnvironmentHelper(EmilSingleton.CONF.imageArchive);
			Environment chosenEnv = environmentHelper.getPlatformById(fdmRequest.getEnvirnment());
			if(chosenEnv == null)
				return Emil.internalErrorResponse("failed loading environment id: " + fdmRequest.getEnvirnment());

			eaas = EmulatorUtils.getEaas(new URL(EmilSingleton.CONF.eaasGw));
			String sessionId = null;
			sessionId = eaas.createSession(chosenEnv.toString(), null, "7777");
			if (sessionId == null)
				return Emil.internalErrorResponse("failed creating a session");

			String state = null; 
			for(int i = 0, DELAY_MS = 100; i < 5 * DELAY_MS; ++i) // wait for 5 sec
			{
				state = eaas.getSessionState(sessionId);
				if(state.equalsIgnoreCase(EaasState.SESSION_READY.value()))
					break;

				Thread.sleep(DELAY_MS);
			}

			if(!state.equalsIgnoreCase(EaasState.SESSION_READY.value()))
				Response.status(Status.SERVICE_UNAVAILABLE).entity("server temporarily lacks resources for request execution, please try later").build();

			sessionInfo.put("id", sessionId);
			sessionInfo.put("iframeurl", EmilSingleton.CONF.embedGw + "?sessionId=" + sessionId);

			if(fdmRequest.getFiles() == null || fdmRequest.getFiles().size() == 0)
				return Response.status(Status.OK).entity(sessionInfo).build();

			String type = Drive.DriveType.DISK.name();
			List<File> files = new ArrayList<>();

			File tempDir = com.google.common.io.Files.createTempDir();
			for(DataRef ref : fdmRequest.getFiles())
			{
				System.out.println("data ref: " + ref.toString());
				URL url = new URL(ref.getUrl());

				if(ref.getType().equalsIgnoreCase("plain")) 
				{
					String filename = ref.getName();
					if(filename == null)
						filename = ref.getUrl().substring( ref.getUrl().lastIndexOf('/')+1, ref.getUrl().length());

					File f = new File(tempDir, filename);
					FileUtils.copyURLToFile(url, f);
					files.add(f);
				}
				else if(ref.getType().equalsIgnoreCase("zip"))
				{
					File zipDir = new File(tempDir, ref.getUrl().substring( ref.getUrl().lastIndexOf('/')+1, ref.getUrl().length()));
					File zipFile = File.createTempFile("zip_", null);
					FileUtils.copyURLToFile(url, zipFile);
					Zip32Utils.unzip(zipFile, zipDir);
					files.add(zipDir);
				}
			}

			Container c = EmulationEnvironmentHelper.createFilesContainer((EmulationEnvironment) chosenEnv, type, files);
			if(c == null)
				return Emil.internalErrorResponse("failed to created upload container");

			int containerId = EmulationEnvironmentHelper.attachContainer(eaas, sessionId, type, c);

			sessionInfo.put("containerId", containerId + "");
			return Response.status(Status.OK).entity(sessionInfo).build();
		}
		catch(RejectedExecutionException e)
		{
			return Response.status(Status.SERVICE_UNAVAILABLE).entity("server temporarily lacks resources for request execution, please try later").build();
		}
		catch(Throwable e)
		{	
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("unexpected error occured on server side, please try again/later").build();
		}
	}



	/* ### Software Archive API ### */

	/**
	 * Looks up and returns the description for a specified software package.
	 * When the software package is found, a JSON response will be returned, containing:
	 * <pre>
	 * {
	 *      "status": "0",
	 *      "id": &ltSoftwarePackage's ID&gt,
	 *      "label": "Short description"
	 * }
	 * </pre>
	 * 
	 * When an internal error occurs, a JSON response containing
	 * the corresponding message will be returned:
	 * <pre>
	 * {
	 *      "status": "1",
	 *      "message": "Error message."
	 * }
	 * </pre>
	 * 
	 * @param softwareId The software package's ID to look up.
	 * @return A JSON response containing software package's description when found,
	 *         else an error message.
	 */
	@GET
	@Path("/getSoftwarePackageDescription")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSoftwarePackageDescription(@QueryParam("softwareId") String softwareId)
	{
		final SoftwareArchiveHelper swarchive = EmilSingleton.swHelper;
		try {
			SoftwareDescription desc = swarchive.getSoftwareDescriptionById(softwareId);
			if (desc == null)
				return Emil.errorMessageResponse("Software with ID '" + softwareId + "' was not found!");

			// Construct response
			JsonBuilder json = new JsonBuilder(DEFAULT_RESPONSE_CAPACITY);
			json.beginObject();
			json.add("status", "0");
			json.add("id", desc.getSoftwareId());
			json.add("label", desc.getLabel());
			json.endObject();
			json.finish();

			return Emil.createResponse(Status.OK, json.toString());
		}
		catch (Throwable throwable) {
			return Emil.internalErrorResponse(throwable);
		}
	}

	/**
	 * Looks up and returns the descriptions for all software packages.
	 * A JSON response will be returned, containing:
	 * <pre>
	 * {
	 *      "status": "0",
	 *      "descriptions": [
	 *          { "id": &ltSoftwarePackage's ID&gt, "label": "Short description" },
	 *          ...
	 *      ]
	 * }
	 * </pre>
	 * 
	 * When an internal error occurs, a JSON response containing
	 * the corresponding message will be returned:
	 * <pre>
	 * {
	 *      "status": "2",
	 *      "message": "Error message."
	 * }
	 * </pre>
	 * 
	 * @return A JSON response containing a list of descriptions
	 *         for all software packages or an error message.
	 */
	@GET
	@Path("/getSoftwarePackageDescriptions")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSoftwarePackageDescriptions()
	{
		final SoftwareArchiveHelper swarchive = EmilSingleton.swHelper;
		try {
			List<SoftwareDescription> descriptions = swarchive.getSoftwareDescriptions();
			if (descriptions == null)
				return Emil.errorMessageResponse("Software archive could not be read!");

			// Construct response
			JsonBuilder json = new JsonBuilder(1024);
			json.beginObject();
			json.add("status", "0");
			json.name("descriptions");
			json.beginArray();

			for (SoftwareDescription desc : descriptions) {
				json.beginObject();
				json.add("id", desc.getSoftwareId());
				json.add("label", desc.getLabel());
				json.endObject();
			}

			json.endArray();
			json.endObject();
			json.finish();

			return Emil.createResponse(Status.OK, json.toString());
		}
		catch (Throwable throwable) {
			return Emil.internalErrorResponse(throwable);
		}
	}

	/**
	 * Creates and configures a new session using specified environment and software package.
	 * When the session is successfully created, a JSON response will be returned, containing:
	 * <pre>
	 * {
	 *      "status": "0",
	 *      "id": &ltSession's ID&gt,
	 *      "driveId": &ltDrive's ID containing the digital object&gt,
	 *      "iframeurl": &ltIFrame's URL&gt,
	 *      "helpmsg": "Optional help message for media change."
	 * }
	 * </pre>
	 * 
	 * When an internal error occurs, a JSON response containing the corresponding message will be returned:
	 * <pre>
	 * {
	 *      "status": "1",
	 *      "message": "Error message."
	 * }
	 * </pre>
	 * 
	 * @param envId The environment's ID to use for session.
	 * @param softwareId The software package's ID to use for session.
	 * @return A JSON object describing the created session, else an error message.
	 */
	@GET
	@Path("/startEnvWithSoftwarePackage")
	@Produces(MediaType.APPLICATION_JSON)
	public Response startEnvWithSoftwarePackage(@QueryParam("envId") String envId, @QueryParam("softwareId") String softwareId)
	{
		final SoftwareArchiveHelper swarchive = EmilSingleton.swHelper;
		try {
			// Start with object ID referenced by the passed software ID.
			final SoftwarePackage software = swarchive.getSoftwarePackageById(softwareId);
			if (software == null)
				return Emil.errorMessageResponse("Software with ID '" + softwareId + "' was not found!");

			final String objectId = software.getObjectId();
			return this.startEnvWithDigitalObject(envId, objectId);
		}
		catch (Throwable throwable) {
			return Emil.internalErrorResponse(throwable);
		}
	}


	@OPTIONS
	@Path("/saveNewEnvironment")
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveNewEnvironmentOptions() {
		return WS_OPTIONS_RESPONSE;
	}

	/**
	 * Stops the specified session and saves it's configuration as a new Emil environment.
	 * This method expects a JSON object containing a description of the new environment:
	 * <pre>
	 * {
	 *      "sessionId": &ltSession's ID&gt,
	 *      "envId": &ltID of the current environment&gt,
	 *      "softwareId": &ltSoftwarePackage's ID&gt,
	 *      "title": "New title",
	 *      "description": "New description"
	 * }
	 * </pre>
	 * 
	 * If a new environment is saved successfully, a JSON object will be returned containing:
	 * <pre>
	 * {
	 *      "status": "0",
	 *      "envId": &ltID of the new environment&gt,
	 *      "message": "Info message.",
	 * }
	 * </pre>
	 * 
	 * When an internal error occurs, a JSON object containing
	 * the corresponding message will be returned:
	 * <pre>
	 * {
	 *      "status": "1",
	 *      "message": "Error message."
	 * }
	 * </pre>
	 * 
	 * @param desc The description of a new environment.
	 * @return A JSON object containing the result or an error message.
	 */
	@POST
	@Path("/saveNewEnvironment")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveNewEnvironment(NewEnvironmentDesc desc)
	{
		final String sessionId = desc.getSessionId();

		try {
			final EaasWS eaas = EmilSingleton.getEaasWS();
			String expState = EaasState.SESSION_RUNNING.value();
			String state = eaas.getSessionState(sessionId);
			if (!state.equalsIgnoreCase(expState) && !state.equalsIgnoreCase(EaasState.SESSION_STOPPED.value())) {
				String message = "Wrong session state! Expected was state '"
						+ expState + "', but current state is '" + state + "'.";

				return Emil.internalErrorResponse(message);
			}
			else {
				// Find current EmilEnvironment
				final String curEnvId = desc.getEnvId();
				final String basedir = EmilSingleton.CONF.emilEnvironmentsPath;
				EmilEnvironment newEmilEnv = EmilUtils.getEmilEnvironmentById(basedir, curEnvId);
				if (newEmilEnv == null)
					return Emil.internalErrorResponse("No emil environment found with ID: " + curEnvId);
				
				if(!state.equalsIgnoreCase(EaasState.SESSION_STOPPED.value()))
					eaas.stop(sessionId);

				// Wait for the emulator to stop
				expState = EaasState.SESSION_STOPPED.value();
				for (int i = 0; i < 30; ++i) {
					state = eaas.getSessionState(sessionId);
					if (state.equalsIgnoreCase(expState))
						break;

					Thread.sleep(500);
				}

				if (!state.equalsIgnoreCase(expState))
					return Emil.internalErrorResponse("Stopping emulator timed out!");

				// Save new internal EmulationEnvironment
				final String imageArchiveHost = EmilSingleton.CONF.imageArchive;
				final String newEnvId = eaas.saveEnvironment(sessionId, imageArchiveHost, desc.getTitle(), "derivate");

				// Update EmilEnvironment with new settings and save it
				newEmilEnv.setParentEnvId(curEnvId);
				newEmilEnv.setEnvId(newEnvId);
				newEmilEnv.setTitle(desc.getTitle());
				newEmilEnv.setDescription(desc.getDescription());
				newEmilEnv.getInstalledSoftwareIds().add(desc.getSoftwareId());
				EmilUtils.saveEmilEnvironment(newEmilEnv, EmilSingleton.CONF.emilEnvironmentsPath);

				JsonBuilder json = new JsonBuilder(DEFAULT_RESPONSE_CAPACITY);
				json.beginObject();
				json.add("status", "0");
				json.add("envId", newEnvId);
				json.add("message", "New environment was saved!");
				json.endObject();
				json.finish();
				
				return Emil.createResponse(Status.OK, json.toString());
			}
		}
		catch (Throwable throwable) {
			return Emil.internalErrorResponse(throwable);
		}
	}


	/* ### ADMIN Interfaces ### */

	/**
	 * Characterizes the specified digital object and returns a list of supported environments.
	 * When the characterization is successful, a JSON response will be returned, containing:
	 * <pre>
	 * {
	 *      "status": "0",
	 *      "environments": [
	 *          { "id": &ltEnvironment's ID&gt, "label": "Environment's label" },
	 *          ...
	 *      ]
	 * }
	 * </pre>
	 * 
	 * When the specified object is not found or an internal error occurs,
	 * a JSON response containing the corresponding message will be returned:
	 * <pre>
	 * {
	 *      "status": "1",
	 *      "message": "Error message."
	 * }
	 * </pre>
	 * 
	 * @param objectId The object's ID to look up environments for.
	 * @return A JSON object with supported environments when found, else an error message.
	 */
	@GET
	@Path("/characterizeObject")
	@Produces(MediaType.APPLICATION_JSON)
	public Response characterizeObject(@QueryParam("objectId") String objectId) {
		/*
		 * Note: This is the same code as loadEnvs(String objectId) but
		 * overrides the archive variable to always be the
		 * ClassificationArchiveAdapter. This always forces characterization,
		 * circumventing any caching. 
		 */
		try {
		    ArchiveAdapter archive = new OverridableCachingClassificationArchiveAdapter();
			List<EnvironmentInfo> environments = archive.getEnvironmentsForObject(objectId);
			
			JsonBuilder json = new JsonBuilder(DEFAULT_RESPONSE_CAPACITY);
			json.beginObject();
			json.add("status", "0");
			json.name("environments");
			json.beginArray();

			for (EnvironmentInfo env : environments) {
				json.beginObject();
				json.add("id", env.id);
				json.add("label", env.label);
				json.endObject();
			}

			json.endArray();
			json.endObject();
			json.finish();

			return Emil.createResponse(Status.OK, json.toString());
		}
		catch (NoSuchElementException e) {
			return Emil.errorMessageResponse("Could not locate specified object: '"	+ objectId + "'");
		}
		catch (Throwable throwable) {
			return Emil.internalErrorResponse(throwable);
		}
	}

	@OPTIONS
	@Path("/overrideObjectCharacterization")
	@Produces(MediaType.APPLICATION_JSON)
	public Response overrideObjectCharacterizationOptions() {
		return WS_OPTIONS_RESPONSE;
	}

	@POST
	@Path("/overrideObjectCharacterization")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response overrideObjectCharacterization(OverrideCharacterizationRequest request) {
		String objectId = request.getObjectId();
		List<EnvironmentInfo> environments = request.getEnvironments();

		if (OverridableArchiveAdapter.class.isInstance(archive)) {
			try {
				((OverridableArchiveAdapter)archive).setEnvironmentsForObject(objectId, environments);
				return Emil.successMessageResponse("");
			} catch (Exception e) {
				return Emil.errorMessageResponse(e.getMessage());
			}
		} else {
			return Emil.internalErrorResponse("The configured archive does not allow overriding the characterization mapping.");
		}
	}
	
	
	/* =============== Internal Helpers =============== */
	
	private static Response internalErrorResponse(Throwable cause)
	{
		cause.printStackTrace();
		
		return Emil.internalErrorResponse(cause.getMessage());
	}
	
	private static Response internalErrorResponse(String message)
	{
		message = "Server has encountered an internal error: '"
				+ message + "'. Please try again later.";
		
		final String json = Emil.createJsonResponse("2", message);
		return Emil.createResponse(Status.INTERNAL_SERVER_ERROR, json);
	}

	private static Response errorMessageResponse(String message)
	{
		final String json = Emil.createJsonResponse("1", message);
		return Emil.createResponse(Status.OK, json);
	}

	private static Response successMessageResponse(String message)
	{
		final String json = Emil.createJsonResponse("0", message);
		return Emil.createResponse(Status.OK, json);
	}

	private static Response createResponse(Status status, Object object)
	{
		ResponseBuilder builder = new ResponseBuilderImpl();
		builder.status(status);
		builder.entity(object);
		builder.header("Access-Control-Allow-Origin", "*");
		return builder.build();
	}
	
	private static String createJsonResponse(String status, String message)
	{
		try {
			JsonBuilder json = new JsonBuilder(DEFAULT_RESPONSE_CAPACITY);
			json.beginObject();
			json.add("status", status);
			json.add("message", message);
			json.endObject();
			json.finish();
			
			return json.toString();
		}
		catch (Exception exception) {
			LOG.warning("An error occured while composing a JSON message!");
			exception.printStackTrace();
		}
		
		return "{\"status\":\"" + status + "\"}";
	}
}
