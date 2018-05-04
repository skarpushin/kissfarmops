package org.kissfarm.agent.node_identity.impl;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.kissfarm.shared.api.NodeIdentity;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.StringUtils;

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

	public SelfIdentificationFileBasedImpl() {
	}

	@Override
	public NodeIdentity resolve() {
		try {
			Preconditions.checkArgument(StringUtils.hasText(nodeIdentityFilename),
					"Node identity file path and name must be provided");

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

			if (!nodeIdentityFile.exists()) {
				try {
					log.debug("Writing down NodeIdentity to: " + nodeIdentityFile);
					File parentDir = nodeIdentityFile.getParentFile();
					if (parentDir != null) {
						Preconditions.checkArgument(parentDir.exists() || parentDir.mkdirs(),
								"Failed to ensure parent dirs for NodeIdentity");
					}
					FileUtils.writeStringToFile(nodeIdentityFile, gson.toJson(ret), "UTF-8");
				} catch (Throwable t) {
					log.warn("Failed to write down NodeIdentity", t);
				}
			}

			return ret;
		} catch (Throwable t) {
			throw new RuntimeException("Failed to resolve node identity. File provided: " + nodeIdentityFilename, t);
		}
	}

	public String getNodeIdentityFilename() {
		return nodeIdentityFilename;
	}

	@Required
	public void setNodeIdentityFilename(String nodeIdentityFilename) {
		this.nodeIdentityFilename = nodeIdentityFilename;
	}
}
