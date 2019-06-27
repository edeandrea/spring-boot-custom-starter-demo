The point of this project is to demonstrate some of the things that a framework library, developed as a custom Spring Boot Starter, could do. This is certainly not exhaustive of things that could be done by a starter library, but its a small "real-world" example of things that are done today within organizations that are looking to put their own organizational opinions on their applications.

These are the things the [custom library](custom-spring-boot-starter) does (see it's own page for specifics):
- Expose a custom set of configurations that applications can use to configure their apps without having to write/maintain any code
- From those configurations
    - Perform all necessary Spring Security configuration on behalf of the application, so the application developer doesn't have to
    - Do some further customization of some of the behavior of the Spring Boot Actuators by setting standard Spring Boot properties

Furthermore, the [custom library](custom-spring-boot-starter) provides all of the above on both the traditional servlet stack as well as the reactive stack. There are 2 sample applications, 1 servlet & 1 reactive, which illustrate this.

## Project Structure
To keep things simple, this repository is set up as a multi-module project. Note that this is really just for ease of use - there aren't any inter-dependencies between the projects. The projects are
- [Servlet Demo](servlet-demo)
    - The servlet version of the Spring Boot application
- [Reactive Demo](reactive-demo)
    - The reactive version of the Spring Boot application
- [Custom Spring Boot Starter](custom-spring-boot-starter)
    - The custom Spring Boot Starter library
    
Both the [Servlet Demo](servlet-demo) & the [Reactive Demo](reactive-demo) have the exact same behavior. They also have a binary dependency on the [custom Spring Boot Starter](custom-spring-boot-starter). There is no source-level dependency. This is achieved through Gradle's dependency substitution capabilities:
```groovy
configurations.all {
	resolutionStrategy {
		dependencySubstitution {
			substitute module('com.mycompany.myframework:custom-spring-boot-starter:1.0') with project(':custom-spring-boot-starter')
		}
	}
}
```

The fact that the [custom library](custom-spring-boot-starter) is a child project in the same source tree is irrelevant - its merely for ease of use/distribution so the library didn't have to be published out to some Maven repo in order for it to be consumed.

## Running the applications
Both applications can be run at the same time. They are configured to run on different ports.

- [Servlet Demo](servlet-demo)
    - `./gradlew :servlet-demo:bootRun`
    - This will run the application at http://localhost:8080
        - **Actuators**: http://localhost/actuator
- [Reactive Demo](reactive-demo)
    - `./gradlew :reactive-demo:bootRun`
    - This will run the application at http://localhost:8081
        - **Actuators**: http://localhost:8081/actuator
