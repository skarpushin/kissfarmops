package org.kissfarm.agent.client.api;

import org.kissfarm.agent.client.impl.StompSessionEvt;

public interface StompSessionHolder {
	StompSessionEvt getSession();
}
