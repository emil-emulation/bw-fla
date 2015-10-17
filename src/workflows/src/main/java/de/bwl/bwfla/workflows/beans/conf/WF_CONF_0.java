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

package de.bwl.bwfla.workflows.beans.conf;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import de.bwl.bwfla.workflows.beans.common.BwflaFormBean;
import de.bwl.bwfla.workflows.beans.common.UINotify;
import de.bwl.bwfla.workflows.conf.WorkflowSingleton;
import de.bwl.bwfla.workflows.conf.WorkflowsConf;



@ManagedBean
@ViewScoped
public class WF_CONF_0 extends BwflaFormBean 
{
	private static final long			serialVersionUID	= 6944252299492997510L;
	private static final Field[]		declaredFields		= WorkflowsConf.class.getDeclaredFields();
	private final Map<String, String>	configPairs			= new TreeMap<>();
	
	@Override
	public void initialize() 
	{
		super.initialize();
		
		WorkflowsConf currentConf = WorkflowSingleton.CONF != null ? WorkflowSingleton.CONF : new WorkflowsConf();  
		if(declaredFields != null)
			for (Field field : declaredFields) 
				if (java.lang.reflect.Modifier.isPublic(field.getModifiers()))
					try
					{
						String name = field.getName();
						if(name.equalsIgnoreCase("emailNotifier"))
							continue;
						
						String value = (String) field.get(currentConf);
						configPairs.put(name, value);
					}
					catch(Exception  e)
					{
						e.printStackTrace();
						return;
					}
	}

	@Override
	public String forward() 
	{
		WorkflowsConf newConf = new WorkflowsConf(); 
		if(declaredFields != null)
		{
			for (Field field : declaredFields) {
				if(java.lang.reflect.Modifier.isPublic(field.getModifiers())) {
					try
					{
						String name = field.getName();
						if(name.equalsIgnoreCase("emailNotifier"))
							continue;
						
						String value = configPairs.get(name);
						field.set(newConf, value);
					}
					catch(Exception e)
					{
						e.printStackTrace();
						return "/pages/bwfla.xhtml";
					}
				}
			}
			
			WorkflowSingleton.saveConf(newConf);
			
			UINotify.success("Updated configuration.");
		}
			
		return "/pages/bwfla.xhtml";
	}

	public Map<String, String> getConfigPairs() 
	{
		return configPairs;
	}

	public List<String> getConfKeyList()
	{
		return new ArrayList<String>(configPairs.keySet());
	}
}