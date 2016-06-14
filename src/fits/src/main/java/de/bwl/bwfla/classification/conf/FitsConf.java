package de.bwl.bwfla.classification.conf;

import javax.xml.bind.annotation.XmlRootElement;
import de.bwl.bwfla.common.utils.config.Configuration;


@XmlRootElement
public class FitsConf extends Configuration
{
	private static final long	serialVersionUID	= -6995479461901431382L;

	public String fitsHome;
}