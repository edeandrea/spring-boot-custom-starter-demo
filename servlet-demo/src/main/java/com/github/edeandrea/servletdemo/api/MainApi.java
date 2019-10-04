package com.github.edeandrea.servletdemo.api;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.github.edeandrea.servletdemo.api.entity.NotFoundException;
import com.github.edeandrea.servletdemo.api.entity.People;
import com.github.edeandrea.servletdemo.api.entity.Person;
import com.github.edeandrea.servletdemo.service.PeopleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/people")
@Api
@CrossOrigin(allowedHeaders = "SM_USER")
public class MainApi {
	private static final Logger LOGGER = LoggerFactory.getLogger(MainApi.class);

	private final PeopleService peopleService;

	public MainApi(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

	@ApiOperation(value = "Gets all people", notes = "Gets all people", nickname = "get-all-people")
	@ApiResponses({
									@ApiResponse(code = 200, message = "Success!"),
									@ApiResponse(code = 404, message = "No people found!")
	})
	@GetMapping(produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public People getPeople() {
		LOGGER.info("Inside {}.getPeople()", getClass().getName());
		Collection<Person> allPeople = this.peopleService.getAllPeople();

		if (!allPeople.isEmpty()) {
			return new People(allPeople);
		}
		else {
			throw new NotFoundException("No people found");
		}
	}

	@ApiOperation(value = "Get a person by personId", notes = "Get a person by personId", nickname = "get-person-by-personId")
	@ApiResponses({
									@ApiResponse(code = 200, message = "Success!"),
									@ApiResponse(code = 404, message = "Person not found")
	})
	@GetMapping(path = "/{personId}", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public Person getPerson(@ApiParam(value = "The person id", required = true) @PathVariable Long personId) {
		LOGGER.info("Inside {}.getPerson({})", getClass().getName(), personId);

		return this.peopleService.getPerson(personId)
			.orElseThrow(() -> new NotFoundException(String.format("Person %d was not found", personId)));
	}

	@ApiOperation(value = "Delete a person", notes = "Delete a person", nickname = "delete-person")
	@ApiResponses({
									@ApiResponse(code = 204, message = "Success!"),
									@ApiResponse(code = 404, message = "Person doesn't exist")
	})
	@DeleteMapping("/{personId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deletePerson(@ApiParam(value = "The person id", required = true) @PathVariable Long personId) {
		LOGGER.info("Inside {}.deletePerson({})", getClass().getName(), personId);

		if (!this.peopleService.getPerson(personId).isPresent()) {
			throw new NotFoundException(String.format("Person %d doesn't exist", personId));
		}

		this.peopleService.deletePerson(personId);
	}
}
