package com.mycompany.myframework.service.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.cors.CorsConfiguration;

import com.mycompany.myframework.properties.config.MyFrameworkConfig.SecurityConfig.CorsConfig;

/**
 * Base class for custom {@link org.springframework.web.cors.CorsConfigurationSource} for servlet applications and
 * {@link org.springframework.web.cors.reactive.CorsConfigurationSource} fo reactive applications.
 *
 * @author Eric Deandrea
 */
public abstract class AllowedDomainsCorsConfigurationSourceBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(AllowedDomainsCorsConfigurationSourceBase.class);
	private static final String DOMAIN_MATCHER_PATTERN = ".*%s";
	private static final String ORIGIN_CANT_BE_NULL_MESSAGE = "origin can not be null";

	private final CorsConfig corsConfig;

	protected AllowedDomainsCorsConfigurationSourceBase(CorsConfig corsConfig) {
		Assert.notNull(corsConfig, "corsProperties can not be null");
		this.corsConfig = corsConfig;
	}

	/**
	 * Adds an origin domain if it comes from an origin that matches one of the allowed sub domains
	 * @param origin The origin
	 * @param corsConfiguration The {@link CorsConfiguration}
	 * @return A {@link CorsConfiguration} containing the CORS information for the current request
	 */
	@Nullable
	protected CorsConfiguration addOriginDomainIfApplicable(@Nullable String origin, @Nullable CorsConfiguration corsConfiguration) {
		CorsConfiguration newConfig = Optional.ofNullable(origin)
			.map(StringUtils::trimToNull)
			.filter(theOrigin -> isConfiguredWithAllowedDomains())
			.filter(this::originMatchesAllowedDomain)
			.map(theOrigin -> createCombinedConfiguration(theOrigin, corsConfiguration))
			.orElse(corsConfiguration);

		removeAllOriginsIfApplicable(newConfig);

		return newConfig;
	}

	private static CorsConfiguration createCombinedConfiguration(String origin, @Nullable CorsConfiguration originalConfiguration) {
		Assert.notNull(origin, ORIGIN_CANT_BE_NULL_MESSAGE);

		CorsConfiguration newConfig = Optional.ofNullable(originalConfiguration)
			.map(CorsConfiguration::new)
			.orElseGet(CorsConfiguration::new);

		newConfig.addAllowedOrigin(origin);

		return newConfig;
	}

	private void removeAllOriginsIfApplicable(@Nullable CorsConfiguration corsConfiguration) {
		LOGGER.info("Removing all origins if applicable from {}", corsConfiguration);

		Optional.ofNullable(corsConfiguration)
			.filter(config -> isConfiguredWithAllowedDomains())
			.ifPresent(config -> {
				List<String> allowedOrigins = new ArrayList<>(Optional.ofNullable(config.getAllowedOrigins()).orElseGet(ArrayList::new));

				if (allowedOrigins.removeIf(element -> StringUtils.equals(element, CorsConfiguration.ALL))) {
					LOGGER.debug("Removing the {} origin from allowedOrigins", CorsConfiguration.ALL);
					config.setAllowedOrigins(allowedOrigins);
				}
			});
	}

	private Set<String> getAllowedDomains() {
		return Optional.ofNullable(this.corsConfig.getAllowedDomains())
			.map(StringUtils::trimToNull)
			.map(org.springframework.util.StringUtils::commaDelimitedListToSet)
			.map(Set::stream)
			.orElseGet(Stream::empty)
			.map(StringUtils::trimToNull)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
	}

	private boolean isConfiguredWithAllowedDomains() {
		return !getAllowedDomains().isEmpty();
	}

	private boolean originMatchesAllowedDomain(String origin) {
		Assert.notNull(origin, ORIGIN_CANT_BE_NULL_MESSAGE);

		return getAllowedDomains().stream()
			.map(allowedDomain -> String.format(DOMAIN_MATCHER_PATTERN, allowedDomain))
			.map(Pattern::compile)
			.map(pattern -> pattern.matcher(origin))
			.anyMatch(Matcher::matches);
	}
}
