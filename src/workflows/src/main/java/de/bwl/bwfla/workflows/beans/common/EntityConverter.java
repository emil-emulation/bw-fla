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

package de.bwl.bwfla.workflows.beans.common;

import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.WeakHashMap;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/*
 * This class is taken from
 * http://stackoverflow.com/questions/21837334/pautocomplete-for-a-global-entity-converter/21837399#21837399
 */

@FacesConverter("de.bwl.bwfla.workflows.beans.common.EntityConverter")
public class EntityConverter implements Converter {
	/*
	 * Use WeakHashMap here to have weak references to the objects.
	 * This means that the GC will NOT consider this map as reference to the
	 * objects and will collect the entries as soon as all *other* references
	 * get out of scope.
	 * 
	 * Keep this in mind if you change the map direction to uuid -> object
	 * or add a second map to have bi-directional access!
	 */
	private static Map<Object, String> entities = new WeakHashMap<Object, String>();
	
	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		for (Entry<Object, String> entry : entities.entrySet()) {
			if (entry.getValue().equals(value)) {
				return entry.getKey();
			}
		}
		return null;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		synchronized (entities) {
			if (!entities.containsKey(value)) {
				String uuid = UUID.randomUUID().toString();
				entities.put(value, uuid);
				return uuid;
			} else {
				return entities.get(value);
			}
		}
	}
}
