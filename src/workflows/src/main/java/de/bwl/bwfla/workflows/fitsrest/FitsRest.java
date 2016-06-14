package de.bwl.bwfla.workflows.fitsrest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import de.bwl.bwfla.common.exceptions.BWFLAException;


@ApplicationScoped
@Path("FitsRest")
public class FitsRest
{
	private static final Logger            				LOG      	  		= Logger.getLogger(FitsRest.class.getSimpleName());
	
	private static int									sessId				= 0x1111;
	private static final Map<String, Future<String>>	clients				= new HashMap<>();

	private static final int							CORE_NUMBER			= Runtime.getRuntime().availableProcessors();
	private static final int							NORM_THREAD_NUMBER	= CORE_NUMBER * 3;
	private static final int							MAX_POOL_SIZE		= CORE_NUMBER * 5;
	private static final int							MAX_QUEUE_SIZE		= 100;
	private static final long							KEEP_ALIVE_MIN		= 15;
	private static final ThreadPoolExecutor				executor			= new ThreadPoolExecutor(NORM_THREAD_NUMBER, MAX_POOL_SIZE, KEEP_ALIVE_MIN, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(MAX_QUEUE_SIZE), new ThreadPoolExecutor.AbortPolicy());
	
	@POST
    @Path("/init")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
	synchronized public static Response intialize(FitsRequest fitsRequest)
	{
		try
		{
			if(fitsRequest == null)
				return Response.status(Status.BAD_REQUEST).entity("request parameter missing").build();
				
			String xmlId = fitsRequest.getXmlId();
			if(xmlId == null || xmlId.isEmpty())
				return Response.status(Status.BAD_REQUEST).entity("client has specified incorrect input value as parameter (empty/null string)").build();
			
			Future<String> xmlFuture = executor.submit(new FitsTask(xmlId));
			String clientId = String.valueOf(sessId++);
			clients.put(clientId, xmlFuture);
			
			Map<String, String> result = new HashMap<>();
			result.put("sessId", clientId);
			return Response.status(Status.OK).entity(result).build();
		}
		catch(RejectedExecutionException e)
		{
			LOG.warning("submitted task was cancelled due to high thread-executor load");
			return Response.status(Status.SERVICE_UNAVAILABLE).entity("server temporarily lacks resources for request execution, please try later").build();
		}
		catch(Throwable e)
		{	
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("unexpected error occured on server side, please try again/later").build();
		}
	}
	
	@GET
    @Path("/getXML")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_XML)
	synchronized public static Response getXML(@QueryParam("sessId") String sessId)
	{	
		try
		{	
			if(sessId == null || sessId.isEmpty())
				return Response.status(Status.BAD_REQUEST).entity("client has not specified 'sessId' URL-parameter received on previous 'init' step").build();
			
			Future<String> xmlFuture = clients.get(sessId);
			if(xmlFuture == null)
				return Response.status(Status.BAD_REQUEST).entity("could not identify client with the provided session id: " + sessId).build();
			
			if(!xmlFuture.isDone())
				return Response.status(Status.NO_CONTENT).entity("resulting FITS-characterization XML for this session id is not available yet, please try later").build();
		
			try
			{
				String xml = xmlFuture.get();
				return Response.status(Status.OK).entity(xml).build();
			}
			catch(ExecutionException e)
			{
				Throwable realCause = e.getCause();
				
				if(realCause instanceof BWFLAException)
					return Response.status(Status.BAD_REQUEST).entity(realCause.getMessage()).build();
				
				throw realCause;
			}
		}
		catch(Throwable t)
		{
			t.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("unexpected error occured on server side, please try again/later").build();
		}
		finally
		{
			clients.remove(sessId);
		}
	}
}