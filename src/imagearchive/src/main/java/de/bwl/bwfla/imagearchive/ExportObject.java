package de.bwl.bwfla.imagearchive;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import de.bwl.bwfla.common.services.net.HttpExport;
import de.bwl.bwfla.imagearchive.ImageArchiveConfig.ImageType;
import de.bwl.bwfla.imagearchive.conf.ImageArchiveSingleton;

public class ExportObject extends HttpExport 
{
	@Override
	public File resolveRequest(String reqStr) 
	{		
		ImageArchiveSingleton.loadConf();
		String filename = null;
		if(!reqStr.startsWith("/"))
		{
			System.out.println("wrong req str: " + reqStr);
			return null;
		}
		filename = reqStr;
		
		try {
			filename = URLDecoder.decode(filename, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
		
		for(ImageType type : ImageType.values())
		{
			File f = new File(ImageArchiveSingleton.iaConfig.imagePath + "/" + type.name() + filename);
			if(f.exists())
				return f;
		}
		
		File f = new File(ImageArchiveSingleton.iaConfig.incomingPath + filename);
		if(f.exists())
			return f;
		
		// temp fix: we link the file from incoming to nbdExport dir with a new id, so look there to
		f = new File(ImageArchiveSingleton.iaConfig.exportPath, filename);
		if(f.exists())
			return f;
		
		return null;
	}

}
