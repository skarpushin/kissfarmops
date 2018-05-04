package org.kissfarm.agent.node_identity.api;

/**
 * Interface to resolve machine public IP
 * 
 * TODO: Impl DigitalOcean's specific impl, i.e. like described here:
 * https://github.com/digitalocean/doctl/issues/89
 * 
 * TODO: Add https://www.whatismyip.org/ as an alternative
 * 
 * TODO: distinguish between ipv4 and ipv6
 * 
 * @author Sergey Karpushin
 *
 */
public interface PublicIpResolver {
	String resolve();
}
