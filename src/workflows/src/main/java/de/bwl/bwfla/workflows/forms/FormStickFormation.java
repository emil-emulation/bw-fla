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

package de.bwl.bwfla.workflows.forms;

public class FormStickFormation 
{	
	public String platform = null;
	private int subscriptions_select = 0;
	private int emulator_select = 0;


	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getPlatform() {
		return this.platform;
	}

	public void setSubscriptions_select(int subscriptions_select) {
		this.subscriptions_select = subscriptions_select;
	}
	
	public int getEmulator_select() {
		return emulator_select;
	}
	
	public void setEmulator_select(int emulator_select) {
		this.emulator_select = emulator_select;
	}

	public int getSubscriptions_select() {
		return subscriptions_select;
	}
}
