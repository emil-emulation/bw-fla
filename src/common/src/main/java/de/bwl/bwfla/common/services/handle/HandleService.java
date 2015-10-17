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

package de.bwl.bwfla.common.services.handle;

import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.security.PrivateKey;
import java.util.logging.Logger;
import net.handle.hdllib.AbstractMessage;
import net.handle.hdllib.AbstractResponse;
import net.handle.hdllib.AdminRecord;
import net.handle.hdllib.Common;
import net.handle.hdllib.CreateHandleRequest;
import net.handle.hdllib.Encoder;
import net.handle.hdllib.HandleResolver;
import net.handle.hdllib.HandleValue;
import net.handle.hdllib.PublicKeyAuthenticationInfo;
import net.handle.hdllib.Util;
import de.bwl.bwfla.conf.CommonSingleton;

public class HandleService {

	protected final static Logger log = Logger.getLogger(HandleService.class.getName());
	private static PublicKeyAuthenticationInfo loadKey(String keyfile, int authIndex, String authHandle) throws UnsupportedEncodingException
	{
		byte[] key = null;
		PrivateKey privkey = null;
		try {
			File f = new File(keyfile);
			if(!f.exists())
			{
				log.severe("keyfile " + keyfile + " not found");
				return null;

			}
			FileInputStream fs = new FileInputStream(f);
			key = new byte[(int)f.length()];
			int n=0;
			while(n<key.length) key[n++] = (byte)fs.read();
			fs.read(key);
			fs.close();

			if(Util.requiresSecretKey(key)){ 
				log.severe("encrypted keys are not supported");
				return null;
			}
			key = Util.decrypt(key, null);
			privkey = Util.getPrivateKeyFromBytes(key, 0);
		}
		catch (Throwable t){
			log.severe(t.getMessage());
			return null;
		}

		return new PublicKeyAuthenticationInfo(authHandle.getBytes("UTF8"), authIndex, privkey);
	}

	public static boolean createUrlHandle(String handle, String url)
	{
		String authHandle;
		int authIndex;
		String keyfile;
	
		keyfile = CommonSingleton.CONF.hdlConf.keyfile;
		authHandle = CommonSingleton.CONF.hdlConf.authHandle;
		String index = CommonSingleton.CONF.hdlConf.authIndex;
		
		if(keyfile == null || authHandle == null || index == null)
		{
			log.severe("hdl.properties not configured properly...");
			return false;
		}
		authIndex = Integer.parseInt(index);
		
		int timestamp = (int)(System.currentTimeMillis()/1000);

		try {
			PublicKeyAuthenticationInfo auth = loadKey(keyfile, authIndex, authHandle);
			if(auth == null)
			{
				log.severe("failed to obtain key");
				return false;
			}
			AdminRecord admin = new AdminRecord(authHandle.getBytes("UTF8"), authIndex,
					true, true , true, true, true, true,
					true, true, true, true, true, true);
			
			HandleValue[] val = { new HandleValue(100, "HS_ADMIN".getBytes("UTF8"),
					Encoder.encodeAdminRecord(admin),
					HandleValue.TTL_TYPE_RELATIVE, 86400,
					timestamp, null, true, true, true, false),

					new HandleValue(1, Common.STD_TYPE_URL, Util.encodeString(url),
							HandleValue.TTL_TYPE_RELATIVE, 86400,
							timestamp, null, true, true, true, false),	
			};

			CreateHandleRequest req =
					new CreateHandleRequest(handle.getBytes("UTF8"), val, auth);

			HandleResolver resolver = new HandleResolver();
			resolver.traceMessages = true;
			AbstractResponse response = resolver.processRequest(req);
			if (response.responseCode != AbstractMessage.RC_SUCCESS)
			{
				log.severe(response.toString());
				return false;
			}


		} catch (Throwable t) {
			t.printStackTrace();
			return false;
		}

		return true;

	}
	
    public static String lookupUrl(String handle)
    {
        try
        {
            HandleResolver resolver = new HandleResolver();
            String[] types = { "URL" };
            HandleValue[] values = resolver.resolveHandle(handle, types, null);

            if (values.length > 0)
            {
                return values[0].getDataAsString();
            }
        } catch (Throwable t)
        {
            t.printStackTrace();
        }

        return null;
    }
}
