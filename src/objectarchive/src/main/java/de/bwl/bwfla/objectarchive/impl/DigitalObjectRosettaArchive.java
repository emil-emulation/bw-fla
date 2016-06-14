package de.bwl.bwfla.objectarchive.impl;

import java.io.IOException;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import de.bwl.bwfla.common.datatypes.FileCollection;
import de.bwl.bwfla.common.datatypes.FileCollectionEntry;
import de.bwl.bwfla.common.utils.SystemEnvironmentHelper;
import de.bwl.bwfla.objectarchive.DigitalObjectArchive;

public class DigitalObjectRosettaArchive implements Serializable, DigitalObjectArchive
{
	private String url;

	public DigitalObjectRosettaArchive(String url) {
		this.url = url;
	}

	@Override
	public List<String> getObjectList() {
		return null;
	}

	@Override
	public FileCollection getObjectReference(String objectId) {
		InputStream in = null;
		try {
			in = new URL( url + objectId ).openStream();
			
			String json = IOUtils.toString( in );
			System.out.println("got json: " + json );
			GsonBuilder gson = new GsonBuilder();
		
			BsbFileCollection bsbFiles = gson.create().fromJson(json , BsbFileCollection.class);
			if(bsbFiles == null)
			{
				System.out.println("failed json");
				return null;
			}
			FileCollection fc = new FileCollection(bsbFiles.id);
			for(BsbFileCollectionEntry e : bsbFiles.files)
			{
				FileCollectionEntry fce = new FileCollectionEntry();
				fce.setId(e.getFileId());
				fce.setType(e.getType());
				fce.setUrl(e.getUrl());
				fc.files.add(fce);
			}
			return fc;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally { IOUtils.closeQuietly(in);
		}
		return null;
	}
	

	@Override
	public String getName() {
		return "emil-rosetta";
	}


}
