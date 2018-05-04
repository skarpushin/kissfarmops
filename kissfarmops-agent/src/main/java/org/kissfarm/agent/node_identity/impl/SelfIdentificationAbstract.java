package org.kissfarm.agent.node_identity.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.kissfarm.agent.node_identity.api.PublicIpResolver;
import org.kissfarm.agent.node_identity.api.SelfIdentification;
import org.kissfarm.shared.api.NodeIdentity;
import org.kissfarm.shared.tools.IdTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;

public abstract class SelfIdentificationAbstract implements SelfIdentification {
	private static final String DEFAULT_TAGS = "tags:unresolved";
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

	@Autowired(required = false)
	public void setPublicIpResolver(PublicIpResolver publicIpResolver) {
		this.publicIpResolver = publicIpResolver;
	}

	public Gson getGson() {
		return gson;
	}

	@Autowired
	public void setGson(Gson gson) {
		this.gson = gson;
	}

}