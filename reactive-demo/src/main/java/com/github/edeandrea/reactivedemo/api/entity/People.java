package com.github.edeandrea.reactivedemo.api.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiModelProperty.AccessMode;

@ApiModel(description = "A collection of person")
@JacksonXmlRootElement(localName = "People")
public class People {
	@ApiModelProperty(required = true, value = "The collection of people")
	@JacksonXmlElementWrapper(localName = "People")
	@JacksonXmlProperty(localName = "Person")
	private final List<Person> people = new ArrayList<>();

	public People() {
		super();
	}

	public People(Collection<Person> people) {
		this();
		addPeople(people);
	}

	public People(Person... people) {
		this();
		addPeople(people);
	}

	@ApiModelProperty(accessMode = AccessMode.READ_ONLY, value = "The number of people in the collection")
	public int getNumPeople() {
		return this.people.size();
	}

	public List<Person> getPeople() {
		return Collections.unmodifiableList(this.people);
	}

	public void addPeople(Collection<Person> people) {
		this.people.addAll(Optional.ofNullable(people).orElseGet(Collections::emptyList));
	}

	public void addPeople(Person... people) {
		this.people.addAll(Optional.ofNullable(people).map(Arrays::asList).orElseGet(Collections::emptyList));
	}

	public void clearPeople() {
		this.people.clear();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
