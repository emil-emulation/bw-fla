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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class NetworkUtils {
	/**
	 * Returns a network address the host is available on. If JBoss is bound to
	 * a single IP address, this address is used. Else (e.g. JBoss listens on
	 * 0.0.0.0), the list of all available network addresses for the host
	 * machine is used to return the first address that is not 127.0.0.1.
	 * 
	 * @return An InetAddress that this host is available on or null if
	 *         none can be found.
	 */
	public static InetAddress getHostAddress() {
		try {
			InetAddress address = InetAddress.getByName(System
					.getProperty("jboss.bind.address"));

			// note that we allow multicast addresses here in case we want
			// really strange scenarios
			if (!address.isAnyLocalAddress()) {
				return address;
			}
		} catch (Throwable t) {
			// this can be ignored as it is thrown if jboss' binding address
			// is invalid for some reason
			// proceed with network address iteration
		}

		// JBoss' address binding is not suitable, find available network
		// addresses from the host system

		try {
			for (Enumeration<NetworkInterface> ifaces = NetworkInterface
					.getNetworkInterfaces(); ifaces.hasMoreElements();) {
				NetworkInterface iface = ifaces.nextElement();

				for (Enumeration<InetAddress> addresses = iface
						.getInetAddresses(); addresses.hasMoreElements();) {
					InetAddress address = addresses.nextElement();
					if (!address.isLoopbackAddress()) {
						return address;
					}
				}
			}
		} catch (Throwable t) {
			// the only function that throws is the above block is
			// NetworkInterface.getNetworkInterfaces()
			// if this happens, we can't get a list of network interfaces,
			// so we'll just return null
			return null;
		}
		return null;
	}
}
