package com.mycompany.myframework.autoconfigure.service.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Conditional;

import com.mycompany.myframework.autoconfigure.service.security.ConditionalOnNoJwtTokenParsing.OnJwtClassesMissingOrSitminderHeaderCondition;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Conditional(OnJwtClassesMissingOrSitminderHeaderCondition.class)
public @interface ConditionalOnNoJwtTokenParsing {
	class OnJwtClassesMissingOrSitminderHeaderCondition extends AnyNestedCondition {
		OnJwtClassesMissingOrSitminderHeaderCondition() {
			super(ConfigurationPhase.PARSE_CONFIGURATION);
		}

		@ConditionalOnMissingClass({ "com.nimbusds.jwt.JWT" })
		static class NimbusJwtOnClasspathClass {}

		@ConditionalOnMissingClass({ "org.springframework.security.oauth2.jwt.Jwt" })
		static class SpringSecurityOAuth2JoseOnClasspathClass {}

		@ConditionalOnMissingClass({ "org.springframework.security.oauth2.reactive.resource.authentication.JwtAuthenticationToken" })
		static class SpringSecurityOAuth2ResourceServerOnClasspathClass {}
	}
}
