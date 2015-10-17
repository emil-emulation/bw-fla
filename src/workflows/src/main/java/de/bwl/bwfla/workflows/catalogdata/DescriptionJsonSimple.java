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

import de.bwl.bwfla.workflows.beans.common.RemoteEmulatorHelper;
import de.bwl.bwfla.workflows.catalogdata.DescriptionTypes.TYPE;

public class DescriptionJsonSimple implements Description {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6740485786021359315L;
	private String id;
	private TYPE descriptionType;
	@Override
	public int compareTo(Description o) {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public RemoteEmulatorHelper getEmulatorHelper() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public TYPE getDescriptionType() {
		return descriptionType;
	}
	
	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return id;
	}
	@Override
	public String getHtml() {
		// TODO Auto-generated method stub
		return null;
	}
}
