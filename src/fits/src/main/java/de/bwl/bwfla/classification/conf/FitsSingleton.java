package de.bwl.bwfla.classification.conf;

import java.io.File;
import java.util.logging.Logger;
import de.bwl.bwfla.common.utils.config.ConfigurationManager;


public class FitsSingleton
{	
	protected static final Logger	LOG			= Logger.getLogger(FitsSingleton.class.getName());
	public static volatile boolean	confValid	= false;
	public static volatile FitsConf	CONF;
	
	static
	{
		loadConf();
	}

	public static boolean validate(FitsConf conf)
	{
		return conf != null && conf.fitsHome != null && (new File(conf.fitsHome)).isDirectory();  
	}

	synchronized public static void loadConf()
	{ 
		CONF = ConfigurationManager.load(FitsConf.class);
		
		if(CONF != null)
			LOG.info(CONF.toString());
		
		confValid = validate(CONF);
	}
}
