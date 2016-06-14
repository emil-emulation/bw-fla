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

package de.bwl.bwfla.common.utils;


import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.bind.JAXBException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPBinding;

import de.bwl.bwfla.common.datatypes.AbstractDataResource;
import de.bwl.bwfla.common.datatypes.EmulationEnvironment;
import de.bwl.bwfla.common.datatypes.Environment;
import de.bwl.bwfla.common.datatypes.NetworkEnvironment;
import de.bwl.bwfla.common.datatypes.Binding;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.jaxwsstubs.imagearchive.ImageArchiveWSRemote;
import de.bwl.bwfla.common.jaxwsstubs.imagearchive.ImageArchiveWSService;
import de.bwl.bwfla.common.jaxwsstubs.imagearchive.IwdMetaData;


public class SystemEnvironmentHelper implements Serializable
{	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected final Logger	log	= Logger.getLogger(this.getClass().getName());
	public final static String ScreenRes[] = {"unknown", "640x480", "800x600", "1024x786"};
	public final static String ColorDepth[] = {"unknown", "265 colors (8-bit)", "32k/64k colors (16-bit)", "32-bit True Color"};
	private List<Environment> environments = new ArrayList<Environment>();
	private List<Environment> derivates = new ArrayList<Environment>();
	private List<Environment> templates = new ArrayList<Environment>();
	private List<Environment> systems = new ArrayList<Environment>();
	// private List<Environment> user = new ArrayList<Environment>();
	
	private ImageArchiveWSRemote archive = null; 
	private String wsHost;
	

	public String toString()
	{
		return wsHost;
	}
	
	
	public SystemEnvironmentHelper(String wsHost)
	{
		this.wsHost = wsHost;
	}
	
	private void connectArchive() throws BWFLAException
	{
		if(archive != null)
			return;
		
		archive = getImageArchiveCon(wsHost);
		if(archive == null)
			throw new BWFLAException("could not connect to image archive @" + wsHost);
		
	}
	
	private static ImageArchiveWSRemote getImageArchiveCon(String host)
	{
		URL wsdl;
		ImageArchiveWSRemote archive;
		try 
		{
			wsdl = new URL("http://" + host + "/image-archive/ImageArchiveWS?wsdl");
			ImageArchiveWSService service = new ImageArchiveWSService(wsdl);
			archive = service.getImageArchiveWSPort();
		} 
		catch (Throwable t) 
		{
			// TODO Auto-generated catch block
			Logger.getLogger(SystemEnvironmentHelper.class.getName()).info("Can not initialize wsdl from http://" + host + "/image-archive/ImageArchiveWS?wsdl");
			t.printStackTrace();
			return null;
		}

		BindingProvider bp = (BindingProvider)archive;
		SOAPBinding binding = (SOAPBinding) bp.getBinding();
		binding.setMTOMEnabled(true);
		bp.getRequestContext().put("javax.xml.ws.client.receiveTimeout", "0");
		bp.getRequestContext().put("javax.xml.ws.client.connectionTimeout", "0");
		bp.getRequestContext().put("com.sun.xml.internal.ws.transport.http.client.streaming.chunk.size", 8192);
		bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "http://" + host + "/image-archive/ImageArchiveWS");

		return archive;
	}

	public List<EmulationEnvironment> getEnvironments() throws BWFLAException
	{
		connectArchive();
		reloadEnvironmentList();
		List<EmulationEnvironment> out = new ArrayList<EmulationEnvironment>();
		for(Environment env : environments)
		{
			if(env instanceof EmulationEnvironment)
				out.add((EmulationEnvironment)env);
		}
		return out;
	}
	
	public List<String> getBeanList() throws BWFLAException
	{
		List <String> _beanList = new ArrayList<String>();
		connectArchive();
		reloadEnvironmentList();
		
		for(Environment env: environments)
		{
			if (env instanceof EmulationEnvironment) {
				EmulationEnvironment emuEnv = (EmulationEnvironment) env;
				if(emuEnv.getEmulator() == null)
				{
					// FIXME tl
					log.info("no emu " + env.getId());// + env.getTitle());
					continue;
				}
				if(_beanList.contains(emuEnv.getEmulator().getBean()))
					continue;
				_beanList.add(emuEnv.getEmulator().getBean());
			}
			else if (env instanceof NetworkEnvironment) {
				if(!_beanList.contains("network"))
					_beanList.add("network");
			}
			else {
				log.info("This should never happen.");
			}
		}
		return _beanList;
	}
	
	public List<EmulationEnvironment> getTemplates() throws BWFLAException
	{
		List <EmulationEnvironment> _beanList = new ArrayList<EmulationEnvironment>();
		connectArchive();
		loadTemplates();

		for(Environment env: templates)
		{
			if (env instanceof EmulationEnvironment) {
				EmulationEnvironment emuEnv = (EmulationEnvironment) env;
	
				if(emuEnv.getEmulator() == null)
				{
					if(emuEnv.getDescription() == null)
					{
						log.info("DescriptionTag is mandatory: " + env.getId());
						continue;
					}
					log.info("no emu " + emuEnv.getDescription().getTitle());
					continue;
				}
				_beanList.add(emuEnv.copy());
			}
		}
		return _beanList;
	}

//	public List<Environment> getEnvironmentsByBean(String bean) throws BWFLAException
//	{
//		List<Environment> beanEnvironments = new ArrayList<Environment>();
//		for (Environment env : environments) {
//			if (env instanceof EmulationEnvironment) {
//				EmulationEnvironment emuEnv = (EmulationEnvironment) env;
//
//				if (emuEnv.getEmulator() == null)
//					continue;
//
//				if (emuEnv.getEmulator().getBean().equalsIgnoreCase(bean)) {
//					beanEnvironments.add(env.copy());
//				}
//			} else if (env instanceof NetworkEnvironment) {
//				if (bean.equalsIgnoreCase("network")) {
//					beanEnvironments.add(env.copy());
//				}
//			}
//		}
//		return beanEnvironments;
//	}
	
	private List<Environment> getImageByBean(List<Environment> envs, String bean)
	{
		List<Environment> beanEnvironments = new ArrayList<Environment>();
		for (Environment env : envs) {
			if (env instanceof EmulationEnvironment) {
				EmulationEnvironment emuEnv = (EmulationEnvironment) env;

				if(emuEnv.getEmulator() == null)
					continue;

				if (emuEnv.getEmulator().getBean().equalsIgnoreCase(bean))
				{
					beanEnvironments.add(env.copy());
				}
			}
			else if (env instanceof NetworkEnvironment)
			{
				if (bean.equalsIgnoreCase("network")) 
				{
					beanEnvironments.add(env.copy());
				}
			}
		}
		return beanEnvironments;
	}
	
	public List<Environment> getBaseImagesByBean(String bean) throws BWFLAException
	{
		return getImageByBean(environments, bean);
	}
	
	public List<Environment> getDerivateImagesByBean(String bean) throws BWFLAException
	{
		return getImageByBean(derivates, bean);
	}
	
	public List<Environment> getSystemImagesByBean(String bean) throws BWFLAException
	{
		return getImageByBean(systems, bean);
	}

	private void reloadEnvironmentList()
	{	
		List<Environment> _environments = new ArrayList<Environment>();
		List<String> envlist = archive.getImages("base");
		for(String env : envlist)
		{
			try {
				Environment emuEnv = Environment.fromValue(env);
				// log.info(emuEnv.getId() + "\n" + env);
				if (emuEnv != null) {
					if(emuEnv.getId() == null)
						continue;
					_environments.add(emuEnv);
				}
			} 
			catch (JAXBException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		environments = _environments;
		
		List<Environment> _derivates = new ArrayList<Environment>();
		envlist = archive.getImages("derivate");
		for(String env : envlist)
		{
			try {
				EmulationEnvironment emuEnv = EmulationEnvironment.fromValue(env);
				if(emuEnv != null)
				{
					if(emuEnv.getId() == null)
						continue;
					_derivates.add(emuEnv);
				}
			}
			catch (Exception e) {}
		}
		derivates = _derivates;
		
		List<Environment> _systems = new ArrayList<Environment>();
		envlist = archive.getImages("system");
		for(String env : envlist)
		{
			try {
				EmulationEnvironment emuEnv = EmulationEnvironment.fromValue(env);
				if(emuEnv != null)
				{
					if(emuEnv.getId() == null)
						continue;
					_systems.add(emuEnv);
				}
			}
			catch (Exception e) {}
		}
		
		systems = _systems;
		
//		
//		List<Environment> _user = new ArrayList<Environment>();
//		envlist = archive.getImages("user");
//		for(String env: envlist)
//		{
//			try {
//				EmulationEnvironment emuEnv = EmulationEnvironment.fromValue(env);
//				if(emuEnv != null)
//				{
//					if(emuEnv.getId() == null)
//						continue;
//					_user.add(emuEnv);
//				}
//			}
//			catch (Exception e) {}
//		}
//		
//		user = _user;
	}

	public static List<Pair<Integer, String>> getAvailableResolutions()
	{
		List<Pair<Integer, String>> resolutions = new ArrayList<Pair<Integer, String>>();
		for(int i = 0; i < ScreenRes.length; i++)
		{
			resolutions.add(new Pair<Integer, String>(i, ScreenRes[i]));
		}
		return resolutions;
	}

	public static List<Pair<Integer, String>> getAvailableColorDepths()
	{
		List<Pair<Integer, String>> colors = new ArrayList<Pair<Integer, String>>();
		for(int i = 0; i < ColorDepth.length; i++)
		{
			colors.add(new Pair<Integer, String>(i, ColorDepth[i]));
		}
		return colors;
	}
	
	private void loadTemplates() throws BWFLAException
	{
		List<Environment> _templates = new ArrayList<Environment>();
		List<String> envlist = archive.getTemplates();
		for(String env : envlist)
		{
			try {
				EmulationEnvironment emuEnv = EmulationEnvironment.fromValue(env);
				if(emuEnv != null)
					_templates.add(emuEnv);
			}
			catch (Exception e) {}
		}
		
		log.info("found " + _templates.size() + " templates");
		templates = _templates;
	}

	public Environment getPlatformById(String id) throws BWFLAException
	{
		connectArchive();
		
		String imageConf = archive.getImageById(id);
		if(imageConf == null)
			throw new BWFLAException("image with the following id cannot be located in the image archive: " + id);
				
		try {
			return EmulationEnvironment.fromValue(imageConf);
		}
		catch (Exception e) {
			throw new BWFLAException("can't load image with id " + id + ": "  + e.getMessage());
		}
	}

	public String registerEnvironment(String conf, File envFile, String type) throws BWFLAException
	{
		connectArchive();
		
		DataHandler dataHandler = new DataHandler(new FileDataSource(envFile));
		String ret = archive.registerImage(conf, dataHandler, type);
		
		if(ret != null) 
		{
			log.info("Successfully registered image at " + wsHost + ". New handle is " + ret);
			return ret;
		}
		
		return null;
	}
	
	public String registerImage(String conf, String file, boolean delete, String type) throws BWFLAException
	{
		connectArchive();
		return archive.registerImageUsingFile(conf, file, delete, type);
	}
	
	public List<String> getIncomingImagesList() throws BWFLAException
	{
		connectArchive();
		return archive.getIncomingImageList();
	}
	
	public List<IwdMetaData> getRecordings(String env) throws BWFLAException
	{
		connectArchive();
		return archive.getRecordings(env);
	}
	
	public boolean addRecording(String envId, String traceId, String data)
	{
		return archive.addRecordingFile(envId, traceId, data);
	}
	
	public String getRecording(String envId, String traceId) throws BWFLAException
	{
		this.connectArchive();
		return archive.getRecording(envId, traceId);
	}

	public Environment registerImageTemp(String config, String image) throws BWFLAException {
		EmulationEnvironment env = EmulationEnvironment.fromValue(config);
		if(env== null)
			return null;
		
		String id = UUID.randomUUID().toString() + String.valueOf(System.currentTimeMillis()).substring(0, 2);
		
		String ref = archive.publishImage(image, id);
		
		Iterator<AbstractDataResource> iterator = env.getAbstractDataResource().iterator();
		while (iterator.hasNext()) {
			AbstractDataResource ar = iterator.next();
			if(!(ar instanceof Binding))
				continue;
			Binding b = (Binding)ar;
			if(b.getId().equals("main_hdd"))
			{
				b.setUrl(ref);
				return env;
			}
		}
		throw new BWFLAException("updating image source failed");
	}
	
	public void releaseTempImage(String id, String image)
	{
		return; // archive.releaseTempImage(id, image);
	}
}
