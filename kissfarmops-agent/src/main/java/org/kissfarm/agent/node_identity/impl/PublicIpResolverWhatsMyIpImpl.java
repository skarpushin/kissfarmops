package org.kissfarm.agent.node_identity.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import org.kissfarm.agent.node_identity.api.PublicIpResolver;

/**
 * This impl will use http://bot.whatismyipaddress.com.
 * 
 * NOTE: It's hard to rely on it, it might be better to create our own service
 * 
 * @author Sergey Karpushin
 *
 */
public class PublicIpResolverWhatsMyIpImpl implements PublicIpResolver {
	@Override
	public String resolve() {
		try {
			URL url_name = new URL("http://bot.whatismyipaddress.com");
			try (BufferedReader sc = new BufferedReader(new InputStreamReader(url_name.openStream()))) {
				return sc.readLine().trim();
			}
		} catch (Throwable t) {
			throw new RuntimeException("Failed to resolve public IP address", t);
		}
	}
}
