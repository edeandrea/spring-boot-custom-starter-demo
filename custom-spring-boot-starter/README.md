This project represents a library packaged as a Spring Boot Starter. It contains the following functionality, implemented for **BOTH** the servlet & reactive stacks.

- Implement the [fault barrier](https://www.oracle.com/technetwork/articles/entarch/effective-exceptions2-097044.html) pattern
    - For all 4xx errors that could happen (request body marshalling, input validation, binding failures, etc), implement a standardized payload format with a model that looks like
       ```js
       {
         "developerMessage": "string",
         "status": "string",
         "response": "object",
         "errors": [{
           "code": "string",
           "field": "string",
           "message": "string"
         }]
       }
       ```
    - For all 4xx/5xx errors, expose a response header called `X-FAULT-ID` containing a GUID which can be correlated by upstream infrastructure
        - My implementation here doesn't completely implement this for 401/403 errors, but it certainly could if I spent a little more time on it. Just pretend for now :)
    - Give application developers a way to trigger a [fault](https://www.oracle.com/technetwork/articles/entarch/effective-exceptions2-097044.html)
- Expose a set of configuration properties (_values shown in snippet below are defaults if not specified by the application_)
    ```yaml
    mycompany:
      myframework:
        config:
          enable-actuators: false
          security:
            enabled: false
    ```
    - `mycompany.myframework.config.enable-actuators` property
        - If set to `true`, the following properties will then be set within the application
            - `management.endpoints.web.exposure.include=*`
            - `management.endpoint.health.show-details=always`
    - `mycompany.myframework.config.security.enabled` property
        - If set to `true` **AND** the `org.springframework.boot:spring-boot-starter-security` library is on the classpath then some default security setup will be done on behalf of the application
            - All actuator endpoints will bypass security (available without authentication)
            - The application will be set up in a way assuming that something like Siteminder is set up in front of the application, providing perimeter authentication (i.e. pre-authentication scenarios)
                - Authentication credentials are assumed to be passed to the application in the request header `SM_USER`
                - The application is configured such that there is only a single user allowed to access the application - **user1**
- For a servlet-based application, if the [SpringFox](http://springfox.github.io/springfox/docs/current/) library is in use **AND** security is turned on according to the above documentation on security, then configure CSRF within Swagger UI
    - There's an outstanding issue within SpringFox where if CSRF is enabled the Swagger UI does not carry the CSRF token provided when the UI is loaded into all the "Try It" examples shown
    - The framework solves this by intercepting the "Try It" requests and dynamically injecting the CSRF token into the request so that it is successful
- For a reactive-based application, there is an [oustanding issue in Spring Security](https://github.com/spring-projects/spring-security/issues/5766) where the CSRF token is not automatically subscribed to
    - The framework solves this by automatically subscribing to the CSRF token if one was generated as part of the request (see [ServerCsrfTokenSubscribingResponseModifier.java](src/main/java/com/mycompany/myframework/service/security/server/ServerCsrfTokenSubscribingResponseModifier.java))
