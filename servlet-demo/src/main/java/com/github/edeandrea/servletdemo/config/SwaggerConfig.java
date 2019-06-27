package com.github.edeandrea.servletdemo.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.classmate.TypeResolver;
import com.github.edeandrea.servletdemo.ServletCustomStarterDemoApplication;
import com.mycompany.myframework.properties.config.MyFrameworkConfig;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
	private final MyFrameworkConfig myFrameworkConfig;

	public SwaggerConfig(MyFrameworkConfig myFrameworkConfig) {
		this.myFrameworkConfig = myFrameworkConfig;
	}

	@Bean
	public Docket apiDocket() {
		Docket docket = new Docket(DocumentationType.SWAGGER_2)
			.directModelSubstitute(LocalDate.class, java.sql.Date.class)
			.directModelSubstitute(LocalDateTime.class, java.util.Date.class)
			.useDefaultResponseMessages(false)
			.apiInfo(apiInfo())
			.select()
				.apis(RequestHandlerSelectors.basePackage(ServletCustomStarterDemoApplication.class.getPackage().getName()))
				.paths(PathSelectors.any())
			.build();

		if (this.myFrameworkConfig.getSecurity().isEnabled()) {
			docket
				.globalOperationParameters(
					Arrays.asList(
						new ParameterBuilder()
							.name("SM_USER")
							.description("Siteminder user header")
							.required(true)
							.allowMultiple(false)
							.modelRef(new ModelRef("String"))
							.type(new TypeResolver().resolve(String.class))
							.parameterType("header")
							.hidden(false)
							.build()
					)
				);

			// This adds a 401 (Unauthorized) to all of the operations in the application
			// Uncomment this if you have authorization turned on for your application
			Arrays.stream(RequestMethod.values())
				.forEach(requestMethod -> docket.globalResponseMessage(
					requestMethod,
					Arrays.asList(
						new ResponseMessageBuilder()
							.code(HttpStatus.UNAUTHORIZED.value())
							.message("User isn't authorized to use application")
							.build()
					)
				)
			);
		}

		return docket;
	}

	private ApiInfo apiInfo() {
		return new ApiInfoBuilder()
			.title("Spring Boot Custom Starter Demo")
			.description("Spring Boot Custom Starter Demo")
			.contact(new Contact("Eric Deandrea", "", "edeandrea@redhat.com"))
			.version("1.0")
			.build();
	}
}
