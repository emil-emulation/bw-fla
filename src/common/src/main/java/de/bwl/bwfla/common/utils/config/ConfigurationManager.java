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

package de.bwl.bwfla.common.utils.config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;


public class ConfigurationManager
{
	protected static final Logger LOG = Logger.getLogger(ConfigurationManager.class.getName());
	
	public static <C extends Configuration> String getConfFilePath(Class<C> confClass)
	{
		try
		{
			return (confClass.newInstance()).getConfFilePath();
		}
		catch(InstantiationException | IllegalAccessException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	synchronized public static <C extends Configuration> C load(Class<C> confClass)
	{
		try
		{
			JAXBContext context = JAXBContext.newInstance(confClass);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			
			try(InputStream is = new FileInputStream(getConfFilePath(confClass)))
			{
				return (C) unmarshaller.unmarshal(is);
			}
		}
		catch(Exception e) 
		{
			LOG.severe("failed unmarshalling configuration file " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	synchronized public static void save(Configuration conf)
	{
		try 
		{
			JAXBContext context = JAXBContext.newInstance(conf.getClass());
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			
			try(FileOutputStream os = new FileOutputStream(conf.getConfFilePath()))
			{
				os.getChannel().truncate(0);
				marshaller.marshal(conf, os);
			}
		}
		catch(JAXBException | IOException e) 
		{
			e.printStackTrace();
		}
	}
}