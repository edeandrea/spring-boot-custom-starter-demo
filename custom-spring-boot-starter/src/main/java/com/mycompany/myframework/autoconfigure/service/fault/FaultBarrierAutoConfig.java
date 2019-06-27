package com.mycompany.myframework.autoconfigure.service.fault;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.mycompany.myframework.service.fault.server.ServerFaultBarrier;
import com.mycompany.myframework.service.fault.servlet.ServletFaultBarrier;

@Configuration
public class FaultBarrierAutoConfig {
	@Configuration
	@ConditionalOnWebApplication(type = Type.SERVLET)
	@ComponentScan(basePackageClasses = { ServletFaultBarrier.class })
	static class ServletFaultBarrierScanner {}

	@Configuration
	@ConditionalOnWebApplication(type = Type.REACTIVE)
	@ComponentScan(basePackageClasses = { ServerFaultBarrier.class })
	static class ServerFaultBarrierScanner {}
}
