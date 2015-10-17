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

/* 
 * License:
 * This sourcecode (SerializationUtils.java) is license free. 
 * This means you are free to do whatever you want with it, without asking the Authors.
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Simple Class to serialize object to byte arrays
 * @author
 * Nick Russler
 * http://www.whitebyte.info
 */
public class SerializationUtils {

	
	/**
	 * @param obj - object to serialize to a byte array
	 * @return byte array containing the serialized obj
	 */
	public static byte[] serialize(Object obj) {
		byte[] result = null;
		ByteArrayOutputStream fos = null;

		try {
			fos = new ByteArrayOutputStream();
			ObjectOutputStream o = new ObjectOutputStream(fos);
			o.writeObject(obj);
			result = fos.toByteArray();
		} catch (IOException e) {
			System.err.println(e);
		} finally {
			try {
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return result;
	}

	
	/**
	 * @param arr - the byte array that holds the serialized object
	 * @return the deserialized object
	 */
	public static Object deserialize(byte[] arr) {
		InputStream fis = null;

		try {
			fis = new ByteArrayInputStream(arr);
			ObjectInputStream o = new ObjectInputStream(fis);
			return o.readObject();
		} catch (IOException e) {
			System.err.println(e);
		} catch (ClassNotFoundException e) {
			System.err.println(e);
		} finally {
			try {
				fis.close();
			} catch (Exception e) {
			}
		}

		return null;
	}
	
	/**
	 * @param obj - object to be cloned
	 * @return a clone of obj
	 */
	@SuppressWarnings("unchecked")
	public static <T> T cloneObject(T obj) {
		return (T) deserialize(serialize(obj));
	}
}