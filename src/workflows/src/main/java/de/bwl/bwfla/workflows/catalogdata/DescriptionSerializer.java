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

package de.bwl.bwfla.workflows.catalogdata;

import java.lang.reflect.Type;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import de.bwl.bwfla.common.utils.SystemEnvironmentHelper;
import de.bwl.bwfla.workflows.catalogdata.DescriptionTypes.TYPE;

public abstract class DescriptionSerializer implements Description
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2865143760109684573L;
	private DescriptionTypes.TYPE descriptionType;
	
	public static Description copy(Description other)
	{
		String s = other.toString();
		switch(other.getDescriptionType())
		{
		case EVALUATION:
			return DescriptionSerializer.fromString(s, ObjectEvaluationDescription.class);
		case OBJECT:
			return DescriptionSerializer.fromString(s, ObjectEnvironmentDescription.class);
		case SYSTEM:
			return DescriptionSerializer.fromString(s, SystemEnvironmentDescription.class);
		case UNDEF:
		default:
			return null;
		}
	}
	
	protected static Description fromString(String jsonString, Class<? extends Description> classOfT)
	{
		GsonBuilder gson = new GsonBuilder();
		gson.registerTypeAdapter(SystemEnvironmentHelper.class, new SystemEnvironmentHelperJsonDes());
		
		// form resulting Description object
		try {
			Description result = gson.create().fromJson(jsonString, classOfT); 
			return result;
		}
		catch (JsonSyntaxException e)
		{
			System.out.println(jsonString);
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String toString()
	{
		GsonBuilder gson = new GsonBuilder();
		gson.registerTypeAdapter(SystemEnvironmentHelper.class, new SystemEnvironmentHelperJsonSer());
		
		// converting this object to JSON
		return gson.create().toJson(this);

	}

	static private class SystemEnvironmentHelperJsonSer implements JsonSerializer<SystemEnvironmentHelper> 
	{
		public JsonElement serialize(SystemEnvironmentHelper src, Type typeOfSrc, JsonSerializationContext context) 
		{
			System.out.println("serializing env helper");
			return new JsonPrimitive(src.toString());
		}
	}

	static private class SystemEnvironmentHelperJsonDes implements JsonDeserializer<SystemEnvironmentHelper> 
	{
		public SystemEnvironmentHelper deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			return new SystemEnvironmentHelper(json.getAsJsonPrimitive().getAsString());
		}
	}
	
	protected void setDescriptionType(DescriptionTypes.TYPE t)
	{
		descriptionType = t;
	}
	
	@Override
	public TYPE getDescriptionType() {
		return descriptionType;
	}
	
	abstract public String getId();
}
