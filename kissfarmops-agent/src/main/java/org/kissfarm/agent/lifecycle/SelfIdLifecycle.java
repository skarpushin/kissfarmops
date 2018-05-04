package org.kissfarm.agent.lifecycle;

import org.kissfarm.agent.node_identity.api.NodeIdentityHolder;
import org.kissfarm.agent.node_identity.api.SelfIdentification;
import org.kissfarmops.shared.nodeid.api.NodeIdentity;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Goal of this lifecycle is to obtain {@link NodeIdentity}. It will be obtain
 * via injected {@link SelfIdentification}.
 * 
 * @author Sergey Karpushin
 *
 */
public class SelfIdLifecycle extends LifecycleSyncBase implements NodeIdentityHolder {
	private SelfIdentification selfIdentification;
	private RegistrationLifecycle registrationLifecycle;

	private NodeIdentity result = null;

	@Override
	protected Lifecycle doStep() throws Exception {
		result = selfIdentification.resolve();
		log.info("NodeId: " + result);
		return registrationLifecycle;
	}

	@Autowired
	public void setSelfIdentification(SelfIdentification selfIdentification) {
		this.selfIdentification = selfIdentification;
	}

	@Autowired
	public void setRegistrationLifecycle(RegistrationLifecycle registrationLifecycle) {
		this.registrationLifecycle = registrationLifecycle;
	}

	@Override
	public NodeIdentity getNodeIdentity() {
		return result;
	}
}
