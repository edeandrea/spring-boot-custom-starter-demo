package com.mycompany.myframework.autoconfigure.service.security.reactive;

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
import com.mycompany.myframework.autoconfigure.service.security.reactive.ConditionalOnReactiveSecurityEnabled.OnReactiveSecurityEnabledCondition;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Conditional(OnReactiveSecurityEnabledCondition.class)
public @interface ConditionalOnReactiveSecurityEnabled {
	class OnReactiveSecurityEnabledCondition extends AllNestedConditions {
		OnReactiveSecurityEnabledCondition() {
			super(ConfigurationPhase.PARSE_CONFIGURATION);
		}

		@ConditionalOnWebApplication(type = Type.REACTIVE)
		static class ReactiveWebApplicationClass {}

		@ConditionalOnSecurityEnabled
		static class SecurityEnabled {}
	}
}
