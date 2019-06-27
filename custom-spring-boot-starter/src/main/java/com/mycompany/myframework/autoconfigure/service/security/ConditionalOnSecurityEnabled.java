package com.mycompany.myframework.autoconfigure.service.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import com.mycompany.myframework.properties.config.MyFrameworkConfig;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Conditional(ConditionalOnSecurityEnabled.OnSecurityEnabledCondition.class)
public @interface ConditionalOnSecurityEnabled {
	class OnSecurityEnabledCondition extends AllNestedConditions {
		OnSecurityEnabledCondition() {
			super(ConfigurationPhase.PARSE_CONFIGURATION);
		}

		@ConditionalOnClass({ AuthenticationManager.class, GlobalAuthenticationConfigurerAdapter.class, EnableWebSecurity.class, WebSecurityConfigurerAdapter.class })
		static class SpringSecurityClassesPresentClass {}

		@ConditionalOnProperty(prefix = MyFrameworkConfig.PREFIX + ".security", name = "enabled", havingValue = "true")
		static class SecurityEnabledClass {}
	}
}
