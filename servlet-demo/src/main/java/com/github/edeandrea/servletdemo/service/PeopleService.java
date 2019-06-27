package com.github.edeandrea.servletdemo.service;

import java.util.Collection;
import java.util.Optional;

import com.github.edeandrea.servletdemo.api.entity.Person;

public interface PeopleService {
	Collection<Person> getAllPeople();
	Optional<Person> getPerson(Long personId);
	void deletePerson(Long personId);
}
