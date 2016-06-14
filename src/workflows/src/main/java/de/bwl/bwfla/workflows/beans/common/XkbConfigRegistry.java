package de.bwl.bwfla.workflows.beans.common;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

@XmlRootElement
public class XkbConfigRegistry {
	public static class Model {
		public static class ModelConfigItem {
			@XmlElement
			String name;

			@XmlElement
			String description;

			@XmlElement
			String vendor;
		}

		@XmlElement
		ModelConfigItem configItem;
	}

	public static class Layout {
		public static class LayoutConfigItem {
			@XmlElement
			String name;

			@XmlElement
			String description;
		}

		@XmlElement
		LayoutConfigItem configItem;
	}

	@XmlElementWrapper(name="modelList")
	@XmlElement(name="model")
	List<Model> modelList = new ArrayList<Model>();

	@XmlElementWrapper(name="layoutList")
	@XmlElement(name="layout")
	List<Layout> layoutList = new ArrayList<Layout>();

	private static XkbConfigRegistry instance = null;

	public static XkbConfigRegistry getXkbConfigRegistry() {
		if (instance == null) {
			try {
				JAXBContext jc = JAXBContext.newInstance(XkbConfigRegistry.class);

				SAXParserFactory spf = SAXParserFactory.newInstance();
				spf.setFeature("http://apache.org/xml/features/validation/schema", false);
				spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
				XMLReader xmlReader = spf.newSAXParser().getXMLReader();
				

				
				//File xml = new File("/usr/share/X11/xkb/rules/base.xml");
				ClassLoader classLoader = XkbConfigRegistry.class.getClassLoader();
				InputStream xml = classLoader.getResourceAsStream("base.xml");
				if(xml == null )
				{
					Utils.log(XkbConfigRegistry.class.getName(), "failed loading base.xml: " + xml);
				}
				InputSource inputSource = new InputSource(xml);
				Unmarshaller unmarshaller = jc.createUnmarshaller();
				SAXSource source = new SAXSource(xmlReader, inputSource);
				
				instance = (XkbConfigRegistry) unmarshaller.unmarshal(source);
			} catch (Exception e) {
				Utils.log(XkbConfigRegistry.class.getName(), "Failed to parse 'base.xml': " + e.getMessage());
				e.printStackTrace();
			}
		}

		return instance;
	}
}
