package com.mycompany.myframework.autoconfigure.service.security.servlet;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Conditional;

import com.mycompany.myframework.autoconfigure.service.security.ConditionalOnSecurityEnabled;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Conditional(ConditionalOnServletSecurityEnabled.OnServletSecurityEnabledCondition.class)
public @interface ConditionalOnServletSecurityEnabled {
	class OnServletSecurityEnabledCondition extends AllNestedConditions {
		OnServletSecurityEnabledCondition() {
			super(ConfigurationPhase.PARSE_CONFIGURATION);
		}

		@ConditionalOnWebApplication(type = Type.SERVLET)
		static class ServletWebApplicationClass {}

		@ConditionalOnSecurityEnabled
		static class SecurityEnabled {}
	}
}
