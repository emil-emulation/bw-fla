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

package de.bwl.bwfla.eaas.allocation.spi.plugins.bladecluster;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import de.bwl.bwfla.common.exceptions.OutOfResourcesException;
import de.bwl.bwfla.eaas.allocation.ComputeInstance;
import de.bwl.bwfla.eaas.allocation.IPlugin;
import de.bwl.bwfla.eaas.allocation.ResourceSpecs;
import de.bwl.bwfla.eaas.allocation.spi.Lockable;
import de.bwl.bwfla.eaas.allocation.spi.plugins.ComputeNode;
import de.bwl.bwfla.eaas.conf.BladeConnection;


@XmlRootElement
@XmlSeeAlso(BladeConnection.class)
public class BladeClusterPlugin extends IPlugin
{
	private static final long	serialVersionUID	= 6648068231738391292L;
	private final Logger console = Logger.getLogger(BladeClusterPlugin.class.getName());

	private ArrayList<ComputeNodeWrapper> nodes;
	private AtomicInteger numRunningNodes;
	
	private final Map<ComputeInstance, ComputeNodeWrapper> instances = new ConcurrentHashMap<ComputeInstance, ComputeNodeWrapper>();
	
	private final TimerTask checkTask = new BladesCheckTask();
	private final Timer timer = new Timer();
	private BladeConnection	bladeConnection;
		
	private static final long BLADES_CHECK_PERIOD = TimeUnit.MILLISECONDS.convert(20, TimeUnit.SECONDS);
	
	
	public BladeClusterPlugin(Object connection)
	{	
		if(!(connection instanceof BladeConnection))
			throw new IllegalArgumentException("passed connection object is of type: '" + connection.getClass().getCanonicalName() + "' , whereas expected type is: '" + BladeConnection.class.getCanonicalName() + "'");
		
		this.bladeConnection = (BladeConnection) connection;
		
		final int numNodes = bladeConnection.nodes.size();   
		this.nodes = new ArrayList<ComputeNodeWrapper>(numNodes);
		this.numRunningNodes = new AtomicInteger(numNodes);
		
		for (ComputeNode node : bladeConnection.nodes)
			nodes.add(new ComputeNodeWrapper(node));
		
		timer.schedule(checkTask, 0, BLADES_CHECK_PERIOD);
	}

	@Override
	public ComputeInstance getComputeInstance(ResourceSpecs specs) throws OutOfResourcesException
	{
		if (specs == null)
			throw new IllegalArgumentException("Resource's specs were not specified!");

		final Random rngen = new Random(System.nanoTime());
		final int delta = (rngen.nextBoolean()) ? 1 : -1;
		final int maxIndex = numRunningNodes.get();
		
		assert(maxIndex >= 0);
		if(maxIndex == 0)
			throw new OutOfResourcesException();
			
		int index = rngen.nextInt(maxIndex);
		
		for (int retries = maxIndex; retries > 0; --retries) {
		
			final ComputeNodeWrapper wrapper = nodes.get(index);
			
			if (!wrapper.tryLockShared()) {
				// The current node is modified by some
				// other thread. Try the next one!
				continue;
			}

			try {
				ComputeNode node = wrapper.getNode();
				if (!node.isRunning()) {
					// This node is already marked
					// as failed. Try the next one!
					continue;
				}
				
				// Try to reserve a compute-instance
				ComputeInstance instance = node.reserveResources(specs);
				if (instance != null) {
					instances.put(instance, wrapper);
					return instance;
				}
			}
			finally {
				wrapper.unlockShared();
				
				// Update index for next iteration
				
				index += delta;
						
				if (index < 0)
					index = maxIndex - 1;
				
				else if (index >= maxIndex)
					index = 0;
			}
		}

		console.warning("BladeCluster is out of resources!");
		throw new OutOfResourcesException();
	}

	@Override
	public void freeComputeInstance(ComputeInstance instance)
	{
		if (instance == null)
			throw new IllegalArgumentException("Compute instance was not specified!");
		
		ComputeNodeWrapper wrapper = instances.remove(instance);
		if (wrapper == null) {
			console.warning("Attempt to remove a not allocated instance with address: " + instance.getUrl());
			return;
		}
		
		wrapper.lockShared();
		try {
			ComputeNode node = wrapper.getNode();
			if (node.isRunning())
				node.releaseResources(instance);
		}
		finally {
			wrapper.unlockShared();
		}
	}
	
	
	/* =============== Internal Stuff =============== */
	
	private static final class ComputeNodeWrapper extends Lockable
	{
		private ComputeNode node;
		
		public ComputeNodeWrapper(ComputeNode node)
		{
			this.node = node;
		}
		
		public ComputeNode getNode()
		{
			return node;
		}
		
		public void setNode(ComputeNode node)
		{
			this.node = node;
		}
	}
	
	private class BladesCheckTask extends TimerTask
	{
		private static final int DEFAULT_PORT = 80;
		private static final int MAX_TIMEOUT  = 5000;
		
		@Override
		public void run()
		{
			final int maxidx = nodes.size();

			// Check all known nodes
			for (int curidx = 0; curidx < maxidx; ++curidx) {
				
				ComputeNodeWrapper wrapper = nodes.get(curidx);
				ComputeNode curnode = wrapper.getNode();
				
				boolean isNodeRunning = this.isNodeReachable(curnode);
				if (isNodeRunning == curnode.isRunning())
					continue;  // State not changed!
				
				if (!isNodeRunning) {
					// Node was running, but now failed
					this.markNodeAsFailed(wrapper);

					console.warning("A blade-node failed:  " + curnode.getAddress());
					
					// Do some cleanup operations
					for (ComputeInstance instance : curnode.getComputeInstances())
						instances.remove(instance);
					
					curnode.reset();
					
					// Recheck the node
					// at current index!
					--curidx; 
				}
				else {
					// Node is again running
					this.markNodeAsRunning(wrapper);
					
					console.warning("Failed blade-node is again running:  " + curnode.getAddress());
				}
			}
		}
		
		private boolean isNodeReachable(ComputeNode node)
		{
			final URL url = node.getAddress();
			final String host = url.getHost();
			final int urlport = url.getPort();
			final int port = (urlport < 0) ? DEFAULT_PORT : urlport;
			
			// Try to connect to the node...
			final Socket socket = new Socket();
			try {
			    InetSocketAddress address = new InetSocketAddress(host, port);
				socket.connect(address, MAX_TIMEOUT);
				return true;
			}
			catch (IOException exception) {
				// Ignore all exceptions!
			}
			finally {
			    try {
                    socket.close();
                }
                catch(IOException exception) {
                    // Should not happen!
                    exception.printStackTrace();
                }
			}
			
			return false;
		}
		
		private void markNodeAsFailed(ComputeNodeWrapper source)
		{
			source.lock();
			try {
				final int index = numRunningNodes.decrementAndGet();
				ComputeNodeWrapper target = nodes.get(index);
				ComputeNode node = source.getNode();
				node.markAsFailed();
				
				if (source == target)
					return;  // Same object!
				
				target.lock();
				source.setNode(target.getNode());
				target.setNode(node);
				target.unlock();
			}
			finally {
				source.unlock();
			}
		}
		
		private void markNodeAsRunning(ComputeNodeWrapper source)
		{
			// Since this objects are accessed in a
			// single thread, we don't need locking!

			final int index = numRunningNodes.get();
			ComputeNode node = source.getNode();
			ComputeNodeWrapper target = nodes.get(index);
			if (source != target) {
				source.setNode(target.getNode());
				target.setNode(node);
			}

			node.markAsRunning();
			numRunningNodes.incrementAndGet();
		}
	}
	
	
	//////////// *********** NEXT FIVE METHODS ARE FOR JAX-B ONLY, WILL ALL BE REMOVED WHEN USER-MANAGEMENT IS IMPLEMENTED *********** ///////////
	
	@SuppressWarnings("unused")
	private BladeClusterPlugin()
	{
		// XXX: no code, please leave empty
	}

	@XmlElement
	public BladeConnection getBladeConnection()
	{
		return bladeConnection;
	}

	public void setBladeConnection(BladeConnection bladeConnection)
	{
		this.bladeConnection = bladeConnection;
		this.initialize(bladeConnection);
	}
	
	private void initialize(BladeConnection bladeConnection)
	{
		final int numNodes = bladeConnection.nodes.size();   
		this.nodes = new ArrayList<ComputeNodeWrapper>(numNodes);
		this.numRunningNodes = new AtomicInteger(numNodes);
		
		for (ComputeNode node : bladeConnection.nodes)
			nodes.add(new ComputeNodeWrapper(node));
		
		timer.schedule(checkTask, 0, BLADES_CHECK_PERIOD);
	}

	@Override
	public void run()
	{
		// XXX: no code, please leave empty
	}
	
	////////////******************************************************************************************************************** /////////////
}