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

import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;



public class BWFLAExceptionHandlerFactory extends ExceptionHandlerFactory
{
	private ExceptionHandlerFactory	exceptionHandlerFactory;

	public BWFLAExceptionHandlerFactory(ExceptionHandlerFactory exceptionHandlerFactory)
	{
		this.exceptionHandlerFactory = exceptionHandlerFactory;
	}

	@Override
	public ExceptionHandler getExceptionHandler()
	{
		ExceptionHandler result = exceptionHandlerFactory.getExceptionHandler();
		result = new BWFLAExceptionHandler(result);

		return result;
	}
}