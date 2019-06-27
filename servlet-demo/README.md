# Servlet Version Demo

## To turn off security
1. Open [application.yml](src/main/resources/application.yml) and comment-out the `mycompany.myframework.config.security` block
1. Open [build.gradle](build.gradle) and comment out the `implementation 'org.springframework.boot:spring-boot-starter-security'` in the `dependencies` section

## Swagger UI
You can use the embedded Swagger UI (http://localhost:8080/swagger-ui.html) instead of the below curl commands. The UI does prevent you from not entering a value for the `SM_USER` header though.

## Requests / Responses
### Actuator root (`mycompany.myframework.config.enable-actuators=true`)
```
$ curl "http://localhost:8080/actuator" {
  "_links": {
    "self": {
      "href": "http://localhost:8080/actuator",
      "templated": false
    },
    "auditevents": {
      "href": "http://localhost:8080/actuator/auditevents",
      "templated": false
    },
    "beans": {
      "href": "http://localhost:8080/actuator/beans",
      "templated": false
    },
    "caches-cache": {
      "href": "http://localhost:8080/actuator/caches/{cache}",
      "templated": true
    },
    "caches": {
      "href": "http://localhost:8080/actuator/caches",
      "templated": false
    },
    "health": {
      "href": "http://localhost:8080/actuator/health",
      "templated": false
    },
    "health-component": {
      "href": "http://localhost:8080/actuator/health/{component}",
      "templated": true
    },
    "health-component-instance": {
      "href": "http://localhost:8080/actuator/health/{component}/{instance}",
      "templated": true
    },
    "conditions": {
      "href": "http://localhost:8080/actuator/conditions",
      "templated": false
    },
    "configprops": {
      "href": "http://localhost:8080/actuator/configprops",
      "templated": false
    },
    "env": {
      "href": "http://localhost:8080/actuator/env",
      "templated": false
    },
    "env-toMatch": {
      "href": "http://localhost:8080/actuator/env/{toMatch}",
      "templated": true
    },
    "info": {
      "href": "http://localhost:8080/actuator/info",
      "templated": false
    },
    "loggers-name": {
      "href": "http://localhost:8080/actuator/loggers/{name}",
      "templated": true
    },
    "loggers": {
      "href": "http://localhost:8080/actuator/loggers",
      "templated": false
    },
    "heapdump": {
      "href": "http://localhost:8080/actuator/heapdump",
      "templated": false
    },
    "threaddump": {
      "href": "http://localhost:8080/actuator/threaddump",
      "templated": false
    },
    "metrics": {
      "href": "http://localhost:8080/actuator/metrics",
      "templated": false
    },
    "metrics-requiredMetricName": {
      "href": "http://localhost:8080/actuator/metrics/{requiredMetricName}",
      "templated": true
    },
    "scheduledtasks": {
      "href": "http://localhost:8080/actuator/scheduledtasks",
      "templated": false
    },
    "httptrace": {
      "href": "http://localhost:8080/actuator/httptrace",
      "templated": false
    },
    "mappings": {
      "href": "http://localhost:8080/actuator/mappings",
      "templated": false
    }
  }
}
```

### Actuator root (`mycompany.myframework.config.enable-actuators=false`)
```
$ curl "http://localhost:8080/actuator"
{
  "_links": {
    "self": {
      "href": "http://localhost:8080/actuator",
      "templated": false
    },
    "health-component-instance": {
      "href": "http://localhost:8080/actuator/health/{component}/{instance}",
      "templated": true
    },
    "health": {
      "href": "http://localhost:8080/actuator/health",
      "templated": false
    },
    "health-component": {
      "href": "http://localhost:8080/actuator/health/{component}",
      "templated": true
    },
    "info": {
      "href": "http://localhost:8080/actuator/info",
      "templated": false
    }
  }
}
```

### Health actuator (`mycompany.myframework.config.enable-actuators=true`)
```
$ curl "http://localhost:8080/actuator/health"
{
  "status": "UP",
  "details": {
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 499963174912,
        "free": 332039184384,
        "threshold": 10485760
      }
    }
  }
}
```

### Health actuator (`mycompany.myframework.config.enable-actuators=false`)
```
$ curl "http://localhost:8080/actuator/health"
{"status":"UP"}
```

### No authentication header - returns 401
```
$ curl "http://localhost:8080/people" -H "accept: application/json" -v

*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8080 (#0)
> GET /people HTTP/1.1
> Host: localhost:8080
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
$ curl "http://localhost:8080/people" -H "accept: application/json" -H "SM_USER: user2" -v
*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8080 (#0)
> GET /people HTTP/1.1
> Host: localhost:8080
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
$ curl "http://localhost:8080/people/bah" -H "accept: application/json" -H "SM_USER: user1" -v
*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8080 (#0)
> GET /people/bah HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.54.0
> accept: application/json
> SM_USER: user1
> 
< HTTP/1.1 400 
< Set-Cookie: XSRF-TOKEN=d95c59fa-6382-4200-aa4a-c731a439da2e; Path=/
< X-FAULT-ID: 876d3d6d-2dfa-4d5c-84cf-8def8d4eab70
< X-Content-Type-Options: nosniff
< X-XSS-Protection: 1; mode=block
< Cache-Control: no-cache, no-store, max-age=0, must-revalidate
< Pragma: no-cache
< Expires: 0
< X-Frame-Options: DENY
< Content-Type: application/json;charset=UTF-8
< Transfer-Encoding: chunked
< Date: Thu, 27 Jun 2019 20:42:59 GMT
< Connection: close
< 
* Closing connection 0
{
  "status": "ERROR",
  "errors": [{
    "field": "personId",
    "code": "typeMismatch",
    "message": "Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'; nested exception is java.lang.NumberFormatException: For input string: \"bah\""
  }],
  "developerMessage": "Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'; nested exception is java.lang.NumberFormatException: For input string: \"bah\""
}
```

### Authentication header for valid user (user1) - returns 200
```
$ curl "http://localhost:8080/people" -H "accept: application/json" -H "SM_USER: user1" -v

*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8080 (#0)
> GET /people HTTP/1.1
> Host: localhost:8080
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
