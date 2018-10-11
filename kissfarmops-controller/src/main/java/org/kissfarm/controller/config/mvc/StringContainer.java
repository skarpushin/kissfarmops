package org.kissfarm.controller.config.mvc;

import org.summerb.approaches.jdbccrud.common.DtoBase;

public class StringContainer implements DtoBase {
	private static final long serialVersionUID = -6402242333723501615L;

	private String str;

	public StringContainer() {
	}

	public StringContainer(String str) {
		super();
		this.str = str;
	}

	public String getStr() {
		return str;
	}

	public void setStr(String str) {
		this.str = str;
	}
}
