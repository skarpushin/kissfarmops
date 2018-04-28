package org.kissfarmops.agent.node_identity.impl;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.kissfarmops.agent.node_identity.api.PublicIpResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This impl is actually will resolve first local ipv4 address, won't go
 * outside. Good for auto tests
 * 
 * @author Sergey Karpushin
 *
 */
public class PublicIpResolverLocalIpImpl implements PublicIpResolver {
	protected Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public String resolve() {
		try {
			List<InetAddress> ips = resolveLocalIps();
			return ips.stream().filter(x -> x.getAddress().length == 4 && x.isSiteLocalAddress()).findFirst()
					.orElseThrow(() -> new IllegalStateException("No IPv4 address found")).getHostAddress();
		} catch (Throwable t) {
			throw new RuntimeException("Failed to resolve public IP address", t);
		}
	}

	public List<InetAddress> resolveLocalIps() throws SocketException {
		List<InetAddress> addrList = new ArrayList<InetAddress>();
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

		while (interfaces.hasMoreElements()) {
			NetworkInterface ifc = interfaces.nextElement();
			if (!ifc.isUp() || ifc.isLoopback() || ifc.isVirtual()) {
				log.debug(
						"NetworkInterface skipped during IPs enumeration {}, isUp = {}, isLoopback = {}, isVirtual = {}",
						ifc.getName(), ifc.isUp(), ifc.isLoopback(), ifc.isVirtual());
				continue;
			}

			log.debug("Enumerating IPs from network interface: {}", ifc.getName());
			for (InterfaceAddress addr : ifc.getInterfaceAddresses()) {
				log.debug("Found IP: {}", addr);
				addrList.add(addr.getAddress());
			}
		}

		return addrList;
	}

}
