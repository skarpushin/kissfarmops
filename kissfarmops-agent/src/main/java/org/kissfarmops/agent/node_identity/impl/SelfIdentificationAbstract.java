package org.kissfarmops.agent.node_identity.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import org.kissfarmops.agent.node_identity.api.PublicIpResolver;
import org.kissfarmops.agent.node_identity.api.SelfIdentification;
import org.kissfarmops.agent.utils.StringUtils;
import org.kissfarmops.shared.api.IdTools;
import org.kissfarmops.shared.nodeid.api.NodeIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;

public abstract class SelfIdentificationAbstract implements SelfIdentification {
	private static final List<String> DEFAULT_TAGS = Arrays.asList("tags:unresolved");
	protected Logger log = LoggerFactory.getLogger(getClass());

	protected PublicIpResolver publicIpResolver;
	protected Gson gson = new Gson();

	public SelfIdentificationAbstract() {
		super();
	}

	protected void fillEmptyFieldsIfAny(NodeIdentity ret) throws UnknownHostException {
		if (ret.getTags() == null) {
			ret.setTags(DEFAULT_TAGS);
		}

		if (!StringUtils.hasText(ret.getId())) {
			ret.setId(IdTools.randomId());
		}

		if (!StringUtils.hasText(ret.getHostName())) {
			InetAddress myHost = InetAddress.getLocalHost();
			ret.setHostName(myHost.getHostName());
		}

		if (!StringUtils.hasText(ret.getPublicIp())) {
			Preconditions.checkArgument(publicIpResolver != null, "publicIpResolver required");
			ret.setPublicIp(publicIpResolver.resolve());
		}
	}

	public PublicIpResolver getPublicIpResolver() {
		return publicIpResolver;
	}

	public void setPublicIpResolver(PublicIpResolver publicIpResolver) {
		this.publicIpResolver = publicIpResolver;
	}

	public Gson getGson() {
		return gson;
	}

	public void setGson(Gson gson) {
		this.gson = gson;
	}

}