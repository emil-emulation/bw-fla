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

package de.bwl.bwfla.imagearchive;

import java.io.File;

public class ImageArchiveConfig {

	
	
	protected File imagePath = null;
	protected File metaDataPath = null;
	protected File exportPath = null;
	protected File incomingPath = null;
	protected File recordingsPath = null;
	protected File templatesPath = null;
	
	protected String nbdPrefix = null;
	protected String httpPrefix = null;	
	
	protected final static String IA_URI_SCHEME = "imagearchive";
	
	public enum ImageType {base, user, derivate, system, netenv};
	
	public ImageArchiveConfig(String base, String nbdExport, String httpExport)
	{	
		imagePath = new File(base + "/images");
		metaDataPath = new File(base + "/meta-data");
		templatesPath = new File(metaDataPath + "/template");
		exportPath = new File(base + "/nbd-export");
		incomingPath = new File(base + "/incoming");
		recordingsPath = new File(base + "/recordings");
		
		this.nbdPrefix = nbdExport;
		this.httpPrefix = httpExport;
	}
}
