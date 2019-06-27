package com.github.edeandrea.servletdemo.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import com.github.edeandrea.servletdemo.api.entity.Person;

@Service
public class InMemoryPeopleService implements PeopleService {
	private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryPeopleService.class);

	// Yes I know this isn't the best solution for concurrency here, but its simple enough for demonstration purposes
	private final ConcurrentMap<Long, Person> people = new ConcurrentHashMap<>(10);

	@Override
	public Collection<Person> getAllPeople() {
		LOGGER.info("Inside {}.getAllPeople()", getClass().getName());
		return Collections.unmodifiableCollection(this.people.values());
	}

	@Override
	public Optional<Person> getPerson(Long personId) {
		LOGGER.info("Inside {}.getPerson({})", getClass().getName(), personId);
		return Optional.ofNullable(this.people.get(personId));
	}

	@Override
	public void deletePerson(Long personId) {
		LOGGER.info("Inside {}.deletePerson({})", getClass().getName(), personId);
		this.people.remove(personId);
	}

	@PostConstruct
	private void initPeople() {
		this.people.putAll(
			Stream.of(
				new Person()
					.withPersonId(0L)
					.withEmailAddress("someone@somewhere.com")
					.withName("John Smith"),
				new Person()
					.withPersonId(1L)
					.withEmailAddress("someoneelse@somewhereelse.com")
					.withName("Joan Smith"),
				new Person()
					.withPersonId(2L)
					.withEmailAddress("anotherperson@theworld.com")
					.withName("James Smith")
			)
			.collect(Collectors.toMap(Person::getPersonId, Function.identity()))
		);
	}
}
