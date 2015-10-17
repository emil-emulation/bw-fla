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

package de.bwl.bwfla.eaas.allocation;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Logger;
import javax.xml.ws.BindingProvider;
import de.bwl.bwfla.api.cluster.ResourceState;
import de.bwl.bwfla.api.emucomp.EaasComponentWS;
import de.bwl.bwfla.api.emucomp.EaasComponentWSService;
import de.bwl.bwfla.api.emucomp.EmulatorWS;
import de.bwl.bwfla.api.emucomp.EmulatorWSService;
import de.bwl.bwfla.common.datatypes.EmuCompState;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.exceptions.OutOfResourcesException;



public class ResourceAllocator implements IAllocator
{
    private static final Logger            LOG      	  = Logger.getLogger(ResourceAllocatorWS.class.getSimpleName());
    private final Map<String, NodeSession> clientSessions = new ConcurrentHashMap<>(ClientThreadPool.getMaxPoolSize() * 10);
    private final Set<String>              closedSessions = new ConcurrentSkipListSet<>();
    private final Timer                    scheduler      = new Timer();
    private final List<IPlugin>            plugins;
    
    private class ExpiredSessionRemovalTask extends TimerTask
    {
        public static final int CLEANUP_PERIOD_MILLIS  = (10) * 60 * 1000;
        private final ResourceAllocator outer          = ResourceAllocator.this;
        
        @Override
        public void run()
        {   
            long now = System.currentTimeMillis();
            
            Iterator<String> it = outer.closedSessions.iterator();
            while(it.hasNext())
            {
                String closedSessionId = it.next();
                NodeSession closedSession = outer.clientSessions.get(closedSessionId);

                assert(closedSession.endTime != null);
                if(now - closedSession.endTime.getTime() > CLEANUP_PERIOD_MILLIS)
                {
                    outer.clientSessions.remove(closedSessionId);
                    it.remove();
                }
            }
        }
    };
    
    private static boolean waitUntilInitialized(String eaasCompId, ComputeInstance instance)
    {
		EmuCompState state = null;
			
		try
		{
			EmulatorWSService emulService = new EmulatorWSService(new URL(instance.getUrl() + "/EmulatorWS?wsdl"));
			EmulatorWS emul = emulService.getEmulatorWSPort();
			BindingProvider bp = (BindingProvider) emul;
			bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, instance.getUrl().toString() + "/EmulatorWS");

			for(int tries = 0, MAX_TRIES = 750; tries < MAX_TRIES; ++tries)
			{
				Thread.sleep(200);
				
				try
				{
					state = EmuCompState.fromValue(emul.getEmulatorState(eaasCompId));
				}
				catch(BWFLAException e)
				{
					e.printStackTrace();
					continue;
				}
				
				switch(state)
				{
					case EMULATOR_READY:
						return true;

					case EMULATOR_BUSY:
						continue;
						
					case EMULATOR_UNDEFINED:
						break;

					case EMULATOR_CLIENT_FAULT:
						throw new IllegalArgumentException("client has specified wrong input parameters for the component initialization");
						
					default:
						LOG.severe("resource was in invalid state at the allocation point: " + state.value()); 
						return false;
				} 
			}
		}
		catch(MalformedURLException | InterruptedException e)
		{
			e.printStackTrace();
		}
    	
		return false;
    }
    
    private static String initEmulationtSession(ComputeInstance instance, String resourceType, String configXml)
    {
		try
		{
			EaasComponentWSService service = new EaasComponentWSService(new URL(instance.getUrl() + "/EaasComponentWS?wsdl"));
			EaasComponentWS eaasComponent = service.getEaasComponentWSPort();
			BindingProvider bp = (BindingProvider) eaasComponent;
			bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, instance.getUrl().toString() + "/EaasComponentWS");
			String eaasCompId = eaasComponent.initialize(resourceType, configXml);
			
			return resourceType.equalsIgnoreCase("networkswitch") || (resourceType.equalsIgnoreCase("emulator") && ResourceAllocator.waitUntilInitialized(eaasCompId, instance)) ? eaasCompId : null;
		}
		catch(MalformedURLException e)
		{
			e.printStackTrace();
			return null;
		}
    }

    private static void endEmulationSession(ComputeInstance instance, String internSessionId)
    {
    	try
		{
	    	EaasComponentWSService service = new EaasComponentWSService(new URL(instance.getUrl() + "/EaasComponentWS?wsdl"));
	    	EaasComponentWS eaasComponent = service.getEaasComponentWSPort();
	    	BindingProvider bp = (BindingProvider) eaasComponent;
			bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, instance.getUrl().toString() + "/EaasComponentWS");
	    	eaasComponent.destroy(internSessionId);
		}
    	catch(MalformedURLException e)
		{
			e.printStackTrace();
		}
    }
    
                                            /* * * PUBLIC METHODS * * */

    public ResourceAllocator(List<IPlugin> plugins)
    {
        if(plugins == null) 
            throw new IllegalArgumentException("'plugins' argument cannot be 'null'");

        this.plugins = plugins;        
        this.scheduler.schedule(new ExpiredSessionRemovalTask(), ExpiredSessionRemovalTask.CLEANUP_PERIOD_MILLIS * 2, ExpiredSessionRemovalTask.CLEANUP_PERIOD_MILLIS);
    }

    @Override
    public String allocate(final String resourceType, final String configXml) throws OutOfResourcesException
    {
        final NodeSession session = new NodeSession();
        final String clientSessionId = String.valueOf(Thread.currentThread().getId()) + System.currentTimeMillis();
        clientSessions.put(clientSessionId, session);

        Runnable runnable = new Runnable()
        {
            final ResourceAllocator outer = ResourceAllocator.this;
            
            @Override
            public void run()
            {
                session.lock.writeLock().lock();

                try
                {
                    if(closedSessions.contains(clientSessionId))
                        return;

                    // TODO: in future, analyze method input parameters to form "specs"
                    ResourceSpecs specs = new ResourceSpecs();
                    specs.setNumCpuCores(1);

                    ComputeInstance instance = null;
                    for(IPlugin plugin: outer.plugins)
                        try
                        {
                            instance = plugin.getComputeInstance(specs);
                            String internSessionId = ResourceAllocator.initEmulationtSession(instance, resourceType, configXml);
                            
                            if(internSessionId == null)
                            	continue;
                            
                            synchronized(session)
                            {
                                session.instance = instance;
                                session.plugin = plugin;
                                session.internSessionId = internSessionId;
                                session.state = ResourceState.RESOURCE_READY;
                                return;
                            }
                        }
	                    catch(IllegalArgumentException e)
	                    {
	                    	synchronized(session)
                            {
	                    		session.endTime = new Date();
                                closedSessions.add(clientSessionId);
                                session.state = ResourceState.CLIENT_FAULT;
                                return;
                            }
	                    }
                        catch(OutOfResourcesException e)
                        {
                            LOG.warning("following plugin lacks requested resources: " + plugin.getClass().getCanonicalName());
                            continue;
                        }
                        finally
                        {
                            if(instance != null && session.state != ResourceState.RESOURCE_READY)
                                plugin.freeComputeInstance(instance);
                        }

                    session.endTime = new Date();
                    closedSessions.add(clientSessionId);
                    session.state = ResourceState.OUT_OF_RESOURCES;
                    
                }
                finally
                {
                    session.lock.writeLock().unlock();
                }
            }
        };

        session.thread = ClientThreadPool.submit(runnable);
        return clientSessionId;
    }

    @Override
    public void release(final String clientSessionId)
    {
        if(clientSessionId == null) 
            throw new IllegalArgumentException("'clientSessionId' string argument cannot be 'null'");

        Runnable runnable = new Runnable()
        {
            final ResourceAllocator outer = ResourceAllocator.this;
            
            @Override
            public void run() 
            {
                final NodeSession session = clientSessions.get(clientSessionId);
                
                if(session == null)
                    return;
                
                session.lock.writeLock().lock();
                
                try
                {
                    if(!outer.closedSessions.contains(clientSessionId))
                    {
                        if(session.state == ResourceState.RESOURCE_READY)
                            ResourceAllocator.endEmulationSession(session.instance, session.internSessionId);

                        session.endTime = new Date();
                        closedSessions.add(clientSessionId);
                        session.state = ResourceState.RESOURCE_RELEASED;
                        session.plugin.freeComputeInstance(session.instance);
                    }
                }
                finally
                {
                    session.lock.writeLock().unlock();
                }
            }
        };

        ClientThreadPool.submit(runnable);
    }
    
    @Override
    public EmuSession getInternalSession(String clientSessionId)
    {
        if(clientSessionId == null || clientSessionId.isEmpty()) 
            throw new IllegalArgumentException("'clientSessionId' string argument cannot be 'null' or empty");

        NodeSession session = clientSessions.get(clientSessionId);
        if(session == null) 
            throw new IllegalArgumentException("cannot access session, passed identifier does not correspond to any session: " + clientSessionId);

        synchronized(session)
        {
            ResourceState state = session.state;
            if(session.thread.isCancelled()) 
                state = ResourceState.OUT_OF_RESOURCES;

            return new EmuSession(session.internSessionId, (session.instance != null ? session.instance.getUrl() : null), state);
        }
    }
}