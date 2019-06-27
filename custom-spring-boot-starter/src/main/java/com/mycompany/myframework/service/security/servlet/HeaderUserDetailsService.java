package com.mycompany.myframework.service.security.servlet;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import org.springframework.lang.Nullable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class HeaderUserDetailsService implements UserDetailsService {
	@Override
	public UserDetails loadUserByUsername(@Nullable String username) throws UsernameNotFoundException {
		return Optional.ofNullable(username)
			.map(StringUtils::trimToNull)
			.filter(user -> StringUtils.equals(user, "user1"))
			.map(this::createUserDetails)
			.orElseThrow(() -> new UsernameNotFoundException(String.format("User %s is not a valid user", username)));
	}

	private UserDetails createUserDetails(String username) {
		return User.withUsername(username)
			.accountExpired(false)
			.accountLocked(false)
			.credentialsExpired(false)
			.disabled(false)
			.password("n/a")
			.authorities("ROLE_USER")
			.build();
	}
}
