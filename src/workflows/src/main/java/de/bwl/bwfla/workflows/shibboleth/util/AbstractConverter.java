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

//package de.bwl.bwfla.workflows.shibboleth.util;
//
//import java.io.Serializable;
//import javax.faces.component.UIComponent;
//import javax.faces.context.FacesContext;
//import javax.faces.convert.Converter;
//import javax.faces.convert.ConverterException;
//import edu.kit.scc.webreg.entity.BaseEntity;
//import edu.kit.scc.webreg.service.BaseService;
//
//public abstract class AbstractConverter implements Converter, Serializable {
//
//	private static final long serialVersionUID = 1L;
//
//	protected abstract BaseService<? extends BaseEntity> getService();
//	
//	@Override
//	public Object getAsObject(FacesContext ctx, UIComponent component, String value)
//			throws ConverterException {
//        if (value == null || value.length() == 0) {
//            return null;
//        }
//        Long id = new Long(value);
//        Object o = getService().findById(id);
//		return o;
//	}
//
//	@Override
//	public String getAsString(FacesContext ctx, UIComponent component, Object value)
//			throws ConverterException {
//        if (value == null) {
//            return "";
//        }
//        return ((BaseEntity)value).getId().toString();
//	}	
//}
