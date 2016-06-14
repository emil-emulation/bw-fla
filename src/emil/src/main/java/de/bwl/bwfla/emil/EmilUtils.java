package de.bwl.bwfla.emil;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.bwl.bwfla.emil.datatypes.EmilEnvironment;

public class EmilUtils
{
	private static final Logger LOG = Logger.getLogger(EmilUtils.class.getName());
	
	private static final Gson GSON = new GsonBuilder().create();

	
	public static EmilEnvironment getEmilEnvironmentById(String basedir, String envid)
	{
		final Path envpath = Paths.get(basedir, envid);
		return EmilUtils.getEmilEnvironmentByPath(envpath);
	}
	
	public static EmilEnvironment getEmilEnvironmentByPath(Path envpath)
	{
		if (!Files.exists(envpath))
			return null;

		try (Reader reader = Files.newBufferedReader(envpath, StandardCharsets.UTF_8)) {
			return GSON.fromJson(reader, EmilEnvironment.class);
		}
		catch (Exception exception) {
			LOG.warning("Reading emil environment failed: " + envpath.toString());
			exception.printStackTrace();
			return null;
		}
	}
	
	public static boolean saveEmilEnvironment(EmilEnvironment env, String basedir)
	{
		final Path envpath = Paths.get(basedir, env.getEnvId());
		try {
			final String json = GSON.toJson(env);
			Files.write(envpath, json.getBytes());
			return true;
		}
		catch (Exception exception) {
			LOG.warning("Writing emil environment failed: " + envpath.toString());
			exception.printStackTrace();
			return false;
		}
	}

	public static boolean replaceEmilEnvironment(EmilEnvironment env, String basedir)
	{
		try {
			final Path envpath = Paths.get(basedir, env.getParentEnvId());
			Files.deleteIfExists(envpath);
		}
		catch (Exception exception) {
			exception.printStackTrace();
			return false;
		}
		
		return EmilUtils.saveEmilEnvironment(env, basedir);
	}
}
