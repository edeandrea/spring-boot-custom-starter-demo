This project represents a library packaged as a Spring Boot Starter. It contains the following functionality, implemented for **BOTH** the servlet & reactive stacks.

- [Fault Barrier Pattern](#fault-barrier-pattern)
- [Configuration Properties](#configuration-properties)
- [Default Jackson Configuration](#default-jackson-configuration)
- [SpringFox Configuration](#springfox-configuration)
- [Reactive CSRF Token Subscription](#reactive-csrf-token-subscription)
- [Customized CORS Configuration](#customized-cors-configuration)

## Fault Barrier Pattern
Implement the [fault barrier](https://www.oracle.com/technetwork/articles/entarch/effective-exceptions2-097044.html) pattern

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

## Configuration Properties
Expose a set of configuration properties (_values shown in snippet below are defaults if not specified by the application_)
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

## Default Jackson Configuration
Automatically set a few Jackson-related configuration properties to control how Jackson serialization/deserialization works
   ```yaml
   spring:
     jackson:
       default-property-inclusion: non_empty
       deserialization:
         accept-empty-string-as-null-object: true
         accept-empty-array-as-null-object: true
         fail-on-null-for-primitives: true
   ```
   
## SpringFox Configuration
For a servlet-based application, if the [SpringFox](http://springfox.github.io/springfox/docs/current/) library is in use **AND** security is turned on according to the above documentation on security, then configure CSRF within Swagger UI
- There's an outstanding issue within SpringFox where if CSRF is enabled the Swagger UI does not carry the CSRF token provided when the UI is loaded into all the "Try It" examples shown
- The framework solves this by intercepting the "Try It" requests and dynamically injecting the CSRF token into the request so that it is successful

## Reactive CSRF Token Subscription
For a reactive-based application, there is an [oustanding issue in Spring Security](https://github.com/spring-projects/spring-security/issues/5766) where the CSRF token is not automatically subscribed to
- The framework solves this by automatically subscribing to the CSRF token if one was generated as part of the request (see [ServerCsrfTokenSubscribingResponseModifier.java](src/main/java/com/mycompany/myframework/service/security/server/ServerCsrfTokenSubscribingResponseModifier.java))

## Customized CORS Configuration
The framework allows for CORS configuration based on domains/subdomains. The [CORS specification](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS) was written back in the web 1.0 days. In this day where one team may be building SPA apps and other independent teams building REST endpoints/applications, its near impossible to know who the consumers of every RESTful application are at deploy time. CORS configuration requires you to know the fully-qualified URL of your javascript app at deployment time, which isn't entirely possible today. The CORS spec doesn't allow wildcarding (like `*.subdomain.domain.com`), other than the simple `*`, which says to allow everything. This allows the app team to build an idiom where they can configure a set of domains/subdomains to allow. This works for both the servlet & reactive samples.

It also allows the application team the ability to narrow it even further within their applications as they normally would (see the `MainApi` classes in each demo and look at the `@CrossOrigin` annotation). The applications are configured like

```yaml
mycompany:
  myframework:
    config:
      security:
        enabled: true
        cors:
          allowed-domains: subdomain.redhat.com
```

This will allow a CORS pre-flight request from any javascript application residing anywhere under the `subdomain.redhat.com` domain. See the [Servlet Demo](../servlet-demo) & [Reactive Demo](../reactive-demo) pages for specific request details.

The main implementation of this functionality can be found from the following classes:
- Servlet
    - The `corsConfigurationSource` method in the [ServiceServletSecurityAutoConfig](src/main/java/com/mycompany/myframework/autoconfigure/service/security/servlet/ServiceServletSecurityAutoConfig.java) class
    - The [AllowedDomainsCorsConfigurationSource](src/main/java/com/mycompany/myframework/service/security/servlet/AllowedDomainsCorsConfigurationSource.java) class
- Reactive
    - The `AllowedDomainsCorsConfiguration` inner class inside the [ServiceReactiveSecurityAutoConfig](src/main/java/com/mycompany/myframework/autoconfigure/service/security/reactive/ServiceReactiveSecurityAutoConfig.java) class
    - The [ServerAllowedDomainsCorsProcessor](src/main/java/com/mycompany/myframework/service/security/server/ServerAllowedDomainsCorsProcessor.java) class
    - The [ServerAllowedDomainsCorsConfigurationSource](src/main/java/com/mycompany/myframework/service/security/server/ServerAllowedDomainsCorsConfigurationSource.java) class
