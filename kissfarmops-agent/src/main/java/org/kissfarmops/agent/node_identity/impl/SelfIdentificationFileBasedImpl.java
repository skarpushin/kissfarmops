package org.kissfarmops.agent.node_identity.impl;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.kissfarmops.agent.utils.StringUtils;
import org.kissfarmops.shared.nodeid.api.NodeIdentity;

import com.google.common.base.Preconditions;

/**
 * This impl will load tags from file embedded in the image and will generate id
 * and resolve host name programmatically.
 * 
 * @author Sergey Karpushin
 *
 */
public class SelfIdentificationFileBasedImpl extends SelfIdentificationAbstract {
	private String nodeIdentityFilename;

	public SelfIdentificationFileBasedImpl(String nodeIdentityFile) {
		this.nodeIdentityFilename = nodeIdentityFile;
		Preconditions.checkArgument(StringUtils.hasText(nodeIdentityFile),
				"Node identity file path and name must be provided");
	}

	@Override
	public NodeIdentity resolve() {
		try {
			NodeIdentity ret;
			File nodeIdentityFile = new File(nodeIdentityFilename);
			if (nodeIdentityFile.exists()) {
				ret = gson.fromJson(FileUtils.readFileToString(new File(nodeIdentityFilename), "UTF-8"),
						NodeIdentity.class);
			} else {
				log.warn("Node identity file {} is missing, empty (no tags) NodeIdentity will be created",
						nodeIdentityFilename);
				ret = new NodeIdentity();
			}

			fillEmptyFieldsIfAny(ret);
			if (log.isDebugEnabled()) {
				log.debug("returning NodeIdentity: " + gson.toJson(ret));
			}

			return ret;
		} catch (Throwable t) {
			throw new RuntimeException("Failed to resolve node identity. File provided: " + nodeIdentityFilename, t);
		}
	}
}
