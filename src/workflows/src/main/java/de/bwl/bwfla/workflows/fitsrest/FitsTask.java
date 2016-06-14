package de.bwl.bwfla.workflows.fitsrest;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.Callable;
import org.apache.commons.io.IOUtils;
import de.bwl.bwfla.common.exceptions.BWFLAException;



class FitsTask implements Callable<String>
{
	private static final File	fitsDir	= new File("/tmp/fits");
	private final String		xmlId;

	public FitsTask(String xmlId)
	{
		this.xmlId = xmlId;
	}

	@Override
	public String call() throws Exception
	{
		File fitsFile = new File(fitsDir.getAbsolutePath() + File.separator + xmlId + ".xml");
		if(!fitsFile.isFile())
			throw new BWFLAException("could not locate FITS characterization xml-file with this id: " + xmlId);

		return IOUtils.toString(new FileInputStream(fitsFile), "UTF-8");
	}
}