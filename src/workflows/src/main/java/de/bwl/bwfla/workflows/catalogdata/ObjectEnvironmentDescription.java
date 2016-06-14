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

package de.bwl.bwfla.workflows.catalogdata;

import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import de.bwl.bwfla.common.datatypes.Drive;
import de.bwl.bwfla.common.datatypes.Drive.DriveType;
import de.bwl.bwfla.common.datatypes.EmulationEnvironment;
import de.bwl.bwfla.common.datatypes.Environment;
import de.bwl.bwfla.common.datatypes.utils.EmulationEnvironmentHelper;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.SystemEnvironmentHelper;
import de.bwl.bwfla.workflows.beans.common.RemoteEmulatorHelper;

public class ObjectEnvironmentDescription extends SystemEnvironmentDescription 
{
	private static final long	serialVersionUID	= 7978149856341748852L;
	transient protected final Logger log	= Logger.getLogger(this.getClass().getName());
	transient private int sortSelection;

	private int resolution;
	private int colordepth;
	private boolean internetconnectivity;
	private boolean crtsimulation;
	private boolean visible;
	private boolean accessible;

	private String objectId;
	private String objectArchiveHost;
	private String objectArchive;
	private DriveType type;

	private String	author;

	public static ObjectEnvironmentDescription fromString(String json)
	{
		return (ObjectEnvironmentDescription) DescriptionSerializer.fromString(json, ObjectEnvironmentDescription.class);
	}

	public ObjectEnvironmentDescription(SystemEnvironmentHelper envHelper, String envId, 
			String archiveHost, String archiveName, String objectId, DriveType type)
	{
		super(envHelper, envId);
		this.objectArchiveHost = archiveHost;
		this.objectArchive = archiveName;
		this.objectId = objectId;
		this.type = type;
		resolution = SystemEnvironmentHelper.ScreenRes.length - 1;
		colordepth = SystemEnvironmentHelper.ColorDepth.length - 1;
		internetconnectivity = true;
		this.setDescriptionType(DescriptionTypes.TYPE.OBJECT);
	}

	public String getId()
	{
		return objectId;
	}

	public String getArchiveid()
	{
		return objectId;
	}

	public String getAuthor()
	{
		return author;
	}

	public void setAuthor(String s)
	{
		author = s;
	}

	public int getResolution() {
		return resolution;
	}

	public void setResolution(int resolution) {
		this.resolution = resolution;
	}

	public int getColordepth() {
		return colordepth;
	}

	public void setColordepth(int colordepth) {
		this.colordepth = colordepth;
	}

	public boolean isInternetconnectivity() {
		return internetconnectivity;
	}

	public void setInternetconnectivity(boolean internetconnectivity) {
		this.internetconnectivity = internetconnectivity;
	}

	public boolean isCrtsimulation() {
		return crtsimulation;
	}

	public void setCrtsimulation(boolean crtsimulation) {
		this.crtsimulation = crtsimulation;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isAccessible() {
		return accessible;
	}

	public void setAccessible(boolean accessible) {
		this.accessible = accessible;
	}
	
	@Override
	public RemoteEmulatorHelper getEmulatorHelper() throws BWFLAException
	{
		Environment env = null;
		if(accessType == SystemEnvironmentDescriptionType.BY_REF)
		{
			env = envHelper.getPlatformById(emuEnvId);
			if(env == null)
				return null;
		} else
		{
			try {
				env = Environment.fromValue(config);
			} catch (JAXBException e) {
				e.printStackTrace();
				return null;
			}
		}	
		
		if(env instanceof EmulationEnvironment)
		{
			RemoteEmulatorHelper emuHelper; 
			if(objectArchiveHost != null && objectArchive != null)
			{
				emuHelper = new RemoteEmulatorHelper(env);
				emuHelper.getMediaManager().addArchiveBinding(objectArchiveHost, objectArchive, objectId, type);
			}
			else
			{
				env = EmulationEnvironmentHelper.registerDataSource(env, objectId, type);
				emuHelper = new RemoteEmulatorHelper(env);
			}
			return emuHelper;
		}
		else
		{
			throw new BWFLAException("Evironment type/instance not supported");
		}
	}

	public String getHtml()
	{
		String result = "";

		if(this.author != null)
		{
			result += this.author;
		}

		if(year != null)
		{
			if(result.length() > 0)
			{
				result += " - ";
			}

			result += year;
		}

		if(this.title != null)
		{
			if(result.length() > 0)
			{
				result += "<br/><br/>";
			}

			result += this.title;
		}

		return result;
	}

	public int compareTo(Description o) 
	{

		ObjectEnvironmentDescription arg = (ObjectEnvironmentDescription)o;
		switch (sortSelection) {
		case 0:
			return getTitle().compareTo(arg.getTitle());
		case 1:
			return getArchiveid().compareTo(arg.getArchiveid());
		case 2:
			return getYear().compareTo(arg.getYear());
		case 3:
			return getAuthor().compareTo(arg.getAuthor());
		default:
			return 0;

		}
	}
}
