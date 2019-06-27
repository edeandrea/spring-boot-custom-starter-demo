package com.mycompany.myframework.service.model;

import org.springframework.lang.Nullable;

public interface TraceableFault {
	@Nullable
	String getFaultId();

	void setFaultId(@Nullable String faultId);
}
