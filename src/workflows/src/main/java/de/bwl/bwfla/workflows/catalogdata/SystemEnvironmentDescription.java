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

import de.bwl.bwfla.common.datatypes.Environment;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.SystemEnvironmentHelper;
import de.bwl.bwfla.workflows.beans.common.RemoteEmulatorHelper;

/* this class describes user system (full system preservation) */

public class SystemEnvironmentDescription extends DescriptionSerializer 
{	
	/**
	 * 
	 */
	private static final long serialVersionUID = -65762844518049671L;

	protected enum SystemEnvironmentDescriptionType { BY_REF, BY_VALUE };
	
	protected SystemEnvironmentDescriptionType accessType;
	protected String config;
	
	// fixme: this should be final 
	protected String emuEnvId;

	protected SystemEnvironmentHelper envHelper; 

	protected String title;
	protected String year;
	protected String access = "public"; 
	
	public String getAccess() {
		return access;
	}

	public void setAccess(String access) {
		this.access = access;
	}

	public String getYear()
	{
		return year;
	}

	public void setYear(String s)
	{
		year = s;
	}
	
	public String getId()
	{
		return emuEnvId;
	}
	
	public static SystemEnvironmentDescription fromString(String json)
	{
		return (SystemEnvironmentDescription) DescriptionSerializer.fromString(json, SystemEnvironmentDescription.class);
	}
	
	public void updateEmulationEnvironmentId(SystemEnvironmentHelper helper, String uuid)
	{
		accessType = SystemEnvironmentDescriptionType.BY_REF;
		emuEnvId = uuid;
		envHelper = helper;
		config = null;
	}
	
	public SystemEnvironmentDescription(SystemEnvironmentHelper helper, String id)
	{
		super();
		accessType = SystemEnvironmentDescriptionType.BY_REF;
		emuEnvId = id;
		envHelper = helper;
		config = null;
		this.setDescriptionType(DescriptionTypes.TYPE.SYSTEM);
	}
	
	public SystemEnvironmentDescription(String emuConfig)
	{
		super();
		accessType = SystemEnvironmentDescriptionType.BY_VALUE;
		config = emuConfig;
		emuEnvId = null;
		envHelper = null;
		this.setDescriptionType(DescriptionTypes.TYPE.SYSTEM);
	}
	
	public RemoteEmulatorHelper getEmulatorHelper() throws BWFLAException
	{
		if(accessType == SystemEnvironmentDescriptionType.BY_REF)
		{
			Environment env = envHelper.getPlatformById(emuEnvId);
			if(env == null)
				return null;
			return new RemoteEmulatorHelper(env);
		}
		else
			return RemoteEmulatorHelper.createRemoteEmuFromConfig(config);
	}
	
	public void setTitle(String s)
	{
		title = s;
	}


	public String getTitle()
	{
		return title;
	}

	@Override
	public int compareTo(Description o) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public String getHtml()
	{
		String result = "";

		if(this.title != null)
		{
			if(result.length() > 0)
			{
				result += "";
			}

			result += this.title;
		}

		return result;
	}

	
}
