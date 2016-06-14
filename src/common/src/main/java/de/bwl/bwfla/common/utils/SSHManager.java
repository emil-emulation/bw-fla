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

package de.bwl.bwfla.common.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;


public class SSHManager
{
	public static String execViaSsh(String cmd, String host, String user, String pwd)
	{
		Properties props = new Properties(); 
		props.put("StrictHostKeyChecking", "no");
		int port = 22;

		String out = "";
		try
		{
			JSch jsch=new JSch();  
			Session session= jsch.getSession(user, host, port);
			session.setConfig(props);
			session.setPassword(pwd);
			session.connect();
			
			ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
			InputStream in = channelExec.getInputStream();
			channelExec.setCommand(cmd);
		    channelExec.connect();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		    String line = "";
		    while ((line = reader.readLine()) != null)
		    	out += line;
			
		    channelExec.disconnect();
		    session.disconnect();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		    
		return out;
	}
}
