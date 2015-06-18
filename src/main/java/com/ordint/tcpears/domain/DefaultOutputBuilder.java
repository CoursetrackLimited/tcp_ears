package com.ordint.tcpears.domain;

public class DefaultOutputBuilder implements OutputBuilder {

	@Override
	public String build(Position2 p) {
		StringBuilder out = new StringBuilder();
		out.append("").append(",").append(p.getLat()).append(",").append(p.getLon());
		return out.toString();
	}

}
