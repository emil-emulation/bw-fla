package de.bwl.bwfla.workflows.beans.common;

import java.util.LinkedHashMap;
import java.util.Map;

import de.bwl.bwfla.workflows.beans.common.XkbConfigRegistry.Layout;
import de.bwl.bwfla.workflows.beans.common.XkbConfigRegistry.Model;

public class XkbConfigRegistryHelper {
	private static Map<String, String> modelsMap;
	private static Map<String, String> layoutsMap;
	
	public static Map<String, String> getModels() {
		if (modelsMap == null) {		
			XkbConfigRegistry xkbConfigRegistry = XkbConfigRegistry.getXkbConfigRegistry();
			modelsMap = new LinkedHashMap<String, String>();
			if(xkbConfigRegistry == null)
				return modelsMap;
				
			for (Model model : xkbConfigRegistry.modelList) {
				String description = model.configItem.name + " (" + model.configItem.vendor + ")";
				// Key and value are swapped for f:selectItems; see: http://stackoverflow.com/tags/selectonemenu/info
				modelsMap.put(description, model.configItem.name);
			} 
		}
		
		return modelsMap;
	}
	
	public static Map<String, String> getLayouts() {
		if (layoutsMap == null) {
			
			XkbConfigRegistry xkbConfigRegistry = XkbConfigRegistry.getXkbConfigRegistry();
			layoutsMap = new LinkedHashMap<String, String>();
			if(xkbConfigRegistry == null)
				return layoutsMap;
			
			for (Layout layout : xkbConfigRegistry.layoutList) {
				// Key and value are swapped for f:selectItems; see: http://stackoverflow.com/tags/selectonemenu/info
				layoutsMap.put(layout.configItem.description, layout.configItem.name);
			}
		}
		
		return layoutsMap;
	}
}
