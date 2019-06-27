package com.github.edeandrea.reactivedemo.service;

import com.github.edeandrea.reactivedemo.api.entity.Person;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PeopleService {
	Flux<Person> getAllPeople();
	Mono<Person> getPerson(Long personId);
	Mono<Void> deletePerson(Long personId);
}
