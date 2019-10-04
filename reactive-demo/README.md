# Reactive Version Demo

## To turn off security
1. Open [application.yml](src/main/resources/application.yml) and comment-out the `mycompany.myframework.config.security` block
1. Open [build.gradle](build.gradle) and comment out the `implementation 'org.springframework.boot:spring-boot-starter-security'` in the `dependencies` section

## Swagger UI
There is no embedded Swagger UI for the Reactive demo as the SpringFox library doesn't yet support the reactive stack.

## Requests / Responses
### Actuator root (`mycompany.myframework.config.enable-actuators=true`)
```
$ curl "http://localhost:8081/actuator"
{
  "_links": {
    "self": {
      "href": "http://localhost:8081/actuator",
      "templated": false
    },
    "auditevents": {
      "href": "http://localhost:8081/actuator/auditevents",
      "templated": false
    },
    "beans": {
      "href": "http://localhost:8081/actuator/beans",
      "templated": false
    },
    "caches-cache": {
      "href": "http://localhost:8081/actuator/caches/{cache}",
      "templated": true
    },
    "caches": {
      "href": "http://localhost:8081/actuator/caches",
      "templated": false
    },
    "health": {
      "href": "http://localhost:8081/actuator/health",
      "templated": false
    },
    "health-component": {
      "href": "http://localhost:8081/actuator/health/{component}",
      "templated": true
    },
    "health-component-instance": {
      "href": "http://localhost:8081/actuator/health/{component}/{instance}",
      "templated": true
    },
    "conditions": {
      "href": "http://localhost:8081/actuator/conditions",
      "templated": false
    },
    "configprops": {
      "href": "http://localhost:8081/actuator/configprops",
      "templated": false
    },
    "env": {
      "href": "http://localhost:8081/actuator/env",
      "templated": false
    },
    "env-toMatch": {
      "href": "http://localhost:8081/actuator/env/{toMatch}",
      "templated": true
    },
    "info": {
      "href": "http://localhost:8081/actuator/info",
      "templated": false
    },
    "loggers-name": {
      "href": "http://localhost:8081/actuator/loggers/{name}",
      "templated": true
    },
    "loggers": {
      "href": "http://localhost:8081/actuator/loggers",
      "templated": false
    },
    "heapdump": {
      "href": "http://localhost:8081/actuator/heapdump",
      "templated": false
    },
    "threaddump": {
      "href": "http://localhost:8081/actuator/threaddump",
      "templated": false
    },
    "metrics-requiredMetricName": {
      "href": "http://localhost:8081/actuator/metrics/{requiredMetricName}",
      "templated": true
    },
    "metrics": {
      "href": "http://localhost:8081/actuator/metrics",
      "templated": false
    },
    "scheduledtasks": {
      "href": "http://localhost:8081/actuator/scheduledtasks",
      "templated": false
    },
    "httptrace": {
      "href": "http://localhost:8081/actuator/httptrace",
      "templated": false
    },
    "mappings": {
      "href": "http://localhost:8081/actuator/mappings",
      "templated": false
    }
  }
}
```

### Actuator root (`mycompany.myframework.config.enable-actuators=false`)
```
$ curl "http://localhost:8081/actuator"
{
  "_links": {
    "self": {
      "href": "http://localhost:8081/actuator",
      "templated": false
    },
    "health": {
      "href": "http://localhost:8081/actuator/health",
      "templated": false
    },
    "health-component": {
      "href": "http://localhost:8081/actuator/health/{component}",
      "templated": true
    },
    "health-component-instance": {
      "href": "http://localhost:8081/actuator/health/{component}/{instance}",
      "templated": true
    },
    "info": {
      "href": "http://localhost:8081/actuator/info",
      "templated": false
    }
  }
}
```

### Health Actuator (`mycompany.myframework.config.enable-actuators=true`)
```
$ curl "http://localhost:8081/actuator/health"
{
  "status": "UP",
  "details": {
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 499963174912,
        "free": 332040753152,
        "threshold": 10485760
      }
    }
  }
}
```

### Health actuator (`mycompany.myframework.config.enable-actuators=false`)
```
$ curl "http://localhost:8081/actuator/health"
{"status":"UP"}
```

### No authentication header - returns 401
```
$ curl "http://localhost:8081/people" -H "accept: application/json" -v

*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8081 (#0)
> GET /people HTTP/1.1
> Host: localhost:8081
> User-Agent: curl/7.54.0
> accept: application/json
> 
< HTTP/1.1 401 
< Set-Cookie: XSRF-TOKEN=c18180ee-ed53-4ad2-9c71-038751d30208; Path=/
< X-Content-Type-Options: nosniff
< X-XSS-Protection: 1; mode=block
< Cache-Control: no-cache, no-store, max-age=0, must-revalidate
< Pragma: no-cache
< Expires: 0
< X-Frame-Options: DENY
< Content-Length: 0
< Date: Thu, 27 Jun 2019 13:45:15 GMT
< 
* Connection #0 to host localhost left intact
```

### Authentication header for invalid user (user2) - returns 401
```
$ curl "http://localhost:8081/people" -H "accept: application/json" -H "SM_USER: user2" -v
*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8081 (#0)
> GET /people HTTP/1.1
> Host: localhost:8081
> User-Agent: curl/7.54.0
> accept: application/json
> SM_USER: user2
> 
< HTTP/1.1 401 
< Set-Cookie: XSRF-TOKEN=d36083c2-06b1-4936-b1f3-701e1dc6e4f9; Path=/
< X-Content-Type-Options: nosniff
< X-XSS-Protection: 1; mode=block
< Cache-Control: no-cache, no-store, max-age=0, must-revalidate
< Pragma: no-cache
< Expires: 0
< X-Frame-Options: DENY
< Content-Length: 0
< Date: Thu, 27 Jun 2019 13:47:01 GMT
< 
* Connection #0 to host localhost left intact
```

### Authentication header for valid user (user1) but binding error - returns 400
```
$ curl "http://localhost:8081/people/bah" -H "accept: application/json" -H "SM_USER: user1" -v
*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8081 (#0)
> GET /people/bah HTTP/1.1
> Host: localhost:8081
> User-Agent: curl/7.54.0
> accept: application/json
> SM_USER: user1
> 
< HTTP/1.1 400 Bad Request
< Content-Type: application/json
< X-FAULT-ID: 33131c0d-f07a-4120-b6cd-c3bc57855ff4
< Content-Length: 342
< Cache-Control: no-cache, no-store, max-age=0, must-revalidate
< Pragma: no-cache
< Expires: 0
< X-Content-Type-Options: nosniff
< X-Frame-Options: DENY
< X-XSS-Protection: 1 ; mode=block
< Referrer-Policy: no-referrer
< 
* Connection #0 to host localhost left intact
{
  "status": "VALIDATION",
  "response": null,
  "errors": [],
  "developerMessage": "400 BAD_REQUEST \"Type mismatch.\"; nested exception is org.springframework.beans.TypeMismatchException: Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'; nested exception is java.lang.NumberFormatException: For input string: \"bah\""
}
```

### Authentication header for valid user (user1) - returns 200
```
$ curl "http://localhost:8081/people" -H "accept: application/json" -H "SM_USER: user1" -v

*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8081 (#0)
> GET /people HTTP/1.1
> Host: localhost:8081
> User-Agent: curl/7.54.0
> accept: application/json
> SM_USER: user1
> 
< HTTP/1.1 200 
< Set-Cookie: XSRF-TOKEN=5c7e8297-ab33-489d-ba7d-a32125b0dfc5; Path=/
< X-Content-Type-Options: nosniff
< X-XSS-Protection: 1; mode=block
< Cache-Control: no-cache, no-store, max-age=0, must-revalidate
< Pragma: no-cache
< Expires: 0
< X-Frame-Options: DENY
< Content-Type: application/json;charset=UTF-8
< Transfer-Encoding: chunked
< Date: Thu, 27 Jun 2019 13:43:48 GMT
< 
* Connection #0 to host localhost left intact
{
  "people": [{
    "personId": 0,
    "emailAddress": "someone@somewhere.com",
    "name": "John Smith"
  }, {
    "personId": 1,
    "emailAddress": "someoneelse@somewhereelse.com",
    "name": "Joan Smith"
  }, {
    "personId": 2,
    "emailAddress": "anotherperson@theworld.com",
    "name": "James Smith"
  }],
  "numPeople": 3
}
```

### CORS Pre-flight request that passes
```
$ curl -X OPTIONS "http://localhost:8081/people" -H "Accept: application/json" -H "SM_USER: user1" -H "Origin: https://www.subdomain1.subdomain.redhat.com" -H "Access-Control-Request-Method: GET" -H "Access-Control-Request-Headers: SM_USER" -v

*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8081 (#0)
> OPTIONS /people HTTP/1.1
> Host: localhost:8081
> User-Agent: curl/7.54.0
> Accept: application/json
> SM_USER: user1
> Origin: https://www.subdomain1.subdomain.redhat.com
> Access-Control-Request-Method: GET
> Access-Control-Request-Headers: SM_USER
> 
< HTTP/1.1 200 OK
< Vary: Origin
< Vary: Access-Control-Request-Method
< Vary: Access-Control-Request-Headers
< Access-Control-Allow-Origin: https://www.subdomain1.subdomain.redhat.com
< Access-Control-Allow-Methods: GET
< Access-Control-Allow-Headers: SM_USER
< Access-Control-Max-Age: 1800
< Cache-Control: no-cache, no-store, max-age=0, must-revalidate
< Pragma: no-cache
< Expires: 0
< X-Content-Type-Options: nosniff
< X-Frame-Options: DENY
< X-XSS-Protection: 1 ; mode=block
< Referrer-Policy: no-referrer
< content-length: 0
< 
* Connection #0 to host localhost left intact
```

### CORS Pre-flight request that fails
```
$ curl -X OPTIONS "http://localhost:8081/people" -H "Accept: application/json" -H "SM_USER: user1" -H "Origin: https://www.redhat.com" -H "Access-Control-Request-Method: GET" -H "Access-Control-Request-Headers: SM_USER" -v

*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8081 (#0)
> OPTIONS /people HTTP/1.1
> Host: localhost:8081
> User-Agent: curl/7.54.0
> Accept: application/json
> SM_USER: user1
> Origin: https://www.redhat.com
> Access-Control-Request-Method: GET
> Access-Control-Request-Headers: SM_USER
> 
< HTTP/1.1 403 Forbidden
< Vary: Origin
< Vary: Access-Control-Request-Method
< Vary: Access-Control-Request-Headers
< Cache-Control: no-cache, no-store, max-age=0, must-revalidate
< Pragma: no-cache
< Expires: 0
< X-Content-Type-Options: nosniff
< X-Frame-Options: DENY
< X-XSS-Protection: 1 ; mode=block
< Referrer-Policy: no-referrer
< content-length: 0
< 
* Connection #0 to host localhost left intact
```
