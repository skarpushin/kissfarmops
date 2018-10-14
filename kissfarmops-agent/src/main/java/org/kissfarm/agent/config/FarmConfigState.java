package org.kissfarm.agent.config;

import org.summerb.approaches.jdbccrud.common.DtoBase;

public class FarmConfigState implements DtoBase {
	private static final long serialVersionUID = 6350340228628906587L;

	private String activeVersion;

	public String getActiveVersion() {
		return activeVersion;
	}

	public void setActiveVersion(String activeVersion) {
		this.activeVersion = activeVersion;
	}
}
