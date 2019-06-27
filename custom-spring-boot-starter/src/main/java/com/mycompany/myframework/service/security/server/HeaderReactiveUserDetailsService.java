package com.mycompany.myframework.service.security.server;

import org.apache.commons.lang3.StringUtils;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import reactor.core.publisher.Mono;

public class HeaderReactiveUserDetailsService implements ReactiveUserDetailsService {
	@Override
	public Mono<UserDetails> findByUsername(String username) {
		return Mono.justOrEmpty(username)
			.map(StringUtils::trimToNull)
			.filter(user -> StringUtils.equals(user, "user1"))
			.map(this::createUserDetails)
			.switchIfEmpty(Mono.defer(() -> Mono.error(new UsernameNotFoundException(String.format("User %s is not a valid user", username)))));
	}

	private UserDetails createUserDetails(String username) {
		return User.withUsername(username)
			.accountExpired(false)
			.accountLocked(false)
			.credentialsExpired(false)
			.disabled(false)
			.password("N/A")
			.authorities("ROLE_USER")
			.build();
	}
}
