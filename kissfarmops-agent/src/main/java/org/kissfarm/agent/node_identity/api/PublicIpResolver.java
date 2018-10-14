package org.kissfarm.agent.node_identity.api;

/**
 * Interface to resolve machine public IP
 * 
 * TODOz4: Impl DigitalOcean's specific impl, i.e. like described here:
 * https://github.com/digitalocean/doctl/issues/89
 * 
 * @author Sergey Karpushin
 */
public interface PublicIpResolver {
	String resolve();
}
