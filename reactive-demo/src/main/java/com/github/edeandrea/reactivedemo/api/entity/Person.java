package com.github.edeandrea.reactivedemo.api.entity;

import java.util.Objects;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiModelProperty.AccessMode;

@ApiModel(description = "A Person")
@JacksonXmlRootElement(localName = "Person")
public class Person {
	@ApiModelProperty(required = true, accessMode = AccessMode.READ_ONLY, value = "The person id")
	private Long personId;

	@ApiModelProperty("The email address")
	private String emailAddress;

	@ApiModelProperty("The person's name")
	private String name;

	public Long getPersonId() {
		return this.personId;
	}

	public void setPersonId(Long personId) {
		this.personId = personId;
	}

	public Person withPersonId(Long personId) {
		setPersonId(personId);
		return this;
	}

	public String getEmailAddress() {
		return this.emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public Person withEmailAddress(String emailAddress) {
		setEmailAddress(emailAddress);
		return this;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Person withName(String name) {
		setName(name);
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if ((o == null) || (getClass() != o.getClass())) {
			return false;
		}

		Person person = (Person) o;
		return new EqualsBuilder().append(this.personId, person.personId).isEquals();
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.personId);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
