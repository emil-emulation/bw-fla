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

package de.bwl.bwfla.workflows.beans.access;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;

import de.bwl.bwfla.common.datatypes.EmulationEnvironment;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.Pair;
import de.bwl.bwfla.workflows.beans.common.BwflaFormBean;
import de.bwl.bwfla.workflows.beans.common.ImageUtils;
import de.bwl.bwfla.workflows.beans.common.RepeatPaginator;
import de.bwl.bwfla.workflows.beans.common.WorkflowResources;
import de.bwl.bwfla.workflows.catalogdata.MetaDataFacade;
import de.bwl.bwfla.workflows.conf.WorkflowSingleton;


@ManagedBean
@ViewScoped
public class WF_A_0 extends BwflaFormBean implements Serializable
{	
	private static final long serialVersionUID = -2323912800593361682L;
	@Inject private WF_A_data wfData;
	private String chosen_object = null;
	private int sortSelection = 0;
	private String filterString = "";
	private List<Pair<Long, String>> availableEnviroments;
	private String metaDataDir;
	private MetaDataFacade metaDataArchive;
	private RepeatPaginator descriptionPaginator;
    private boolean requirePrefs = false;
	
	private boolean configureBean()
	{
		metaDataDir = WorkflowSingleton.CONF.metaDir;
		if(metaDataDir == null)
			panic("metaDataDir not set: fix bwfla_workflows");
		
		metaDataArchive = new MetaDataFacade(metaDataDir);
		wfData.getStorage().envHelper = WorkflowSingleton.envHelper;
		return true;
	}
	
	@Override
	public void initialize()
	{
		super.initialize();

		if(!configureBean())
			return;
	
		filterDescriptors();
		loadAvailableEnviroments();
	}

	public void loadAvailableEnviroments() 
	{
		availableEnviroments = new ArrayList<Pair<Long, String>>();
		availableEnviroments.add(new Pair<Long, String>(0L, "Windows XP"));
		availableEnviroments.add(new Pair<Long, String>(1L, "Ubuntu 13.12"));
		availableEnviroments.add(new Pair<Long, String>(2L, "Mac OS"));
	}

	public void sortDescriptors() 
	{
		metaDataArchive.sortDescriptors(this.sortSelection);
		this.setDescriptionPaginator(new RepeatPaginator(metaDataArchive.getDescriptors()));
	}

	public void filterDescriptors() 
	{
		metaDataArchive.filterDescriptors(this.filterString,this.sortSelection);
		this.setDescriptionPaginator(new RepeatPaginator(metaDataArchive.getDescriptors()));
	}

	public void chooseObject(String aId) 
	{
		this.chosen_object = aId;
	}

	public String thumbURL(String aId) 
	{
		try 
		{
			// FIXME: we should not make hard connections to file name patterns
			File destDir = new File(metaDataDir, aId);
			String filepath = destDir.toString() + "/" + aId + ".jpg";
			File imgFile = new File(filepath);
			if(!imgFile.exists())
			{
				log.warning("no thumbnail found for this object, using a default one");
				return "/faces/javax.faces.resource/images/dummy-cdrom.jpg";
			}
			else
				return ImageUtils.publishImage(imgFile, true); 
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			return "/faces/javax.faces.resource/images/dummy-cdrom.jpg";
		}
	}

	public String navigate() throws MalformedURLException, URISyntaxException
	{
		wfData.getStorage().chosenDescriptor = metaDataArchive.getDescriptor(chosen_object);
		if(wfData.getStorage().chosenDescriptor == null)
		{
			log.info("cannot find a descriptor for the chosen object");
			return "";
		}
		
		try {
			wfData.getStorage().emuHelper = wfData.getStorage().chosenDescriptor.getEmulatorHelper();
		} catch (BWFLAException e) {
			panic(e.getMessage());
		}
		
		EmulationEnvironment env = wfData.getStorage().emuHelper.getEmulationEnvironment();
		if(wfData.getStorage().emuHelper.requiresUserPrefs())
		{
			System.out.println("requires userprefs");
			requirePrefs = true;
			if(!this.isDidUserSetPrefs())
			{
				return "";
			}
			
			setUserPreferences(env);
		}
		
		wfData.getStorage().emuHelper.initialize();
		
		this.resourceManager.register(WorkflowResources.WF_RES.EMU_COMP, wfData.getStorage().emuHelper);
		return "/pages/workflow-access/WF_A_2.xhtml?faces-redirect=true";
	}

	public int getSortSelection() 
	{
		return sortSelection;
	}

	public void setSortSelection(int sortSelection) 
	{
		this.sortSelection = sortSelection;
	}

	public String getFilterString() 
	{
		return filterString;
	}

	public void setFilterString(String filterString) 
	{
		this.filterString = filterString;
	}

	@Override
	public String forward()
	{
		return "/pages/start.xhtml";
	}

	public RepeatPaginator getDescriptionPaginator() 
	{
		return descriptionPaginator;
	}

	public void setDescriptionPaginator(RepeatPaginator descriptionPaginator) 
	{
		this.descriptionPaginator = descriptionPaginator;
	}
	
	public boolean isRequirePrefs()
	{
		return requirePrefs;
	}
}
