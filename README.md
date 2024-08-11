# A Spring Boot 2.x demo app 

This project demonstrates the following features:
* Spring DI
* [Singleton vs Prototype `@Scope`](src/test/java/com/att/training/spring/boot/demo/scopes/MySingletonTest.java)
* HTTP endpoints (Controllers)
* Exception handlers
* Bean Validation 2.0
* Binding POJOs to configuration
* [Binding POJOs to external files (*.yaml)](src/main/java/com/att/training/spring/boot/demo/user/ExternalUserConfiguration.java) 
* Spring Async - usage and configuration
  * [Configuring multiple thread-pools](src/main/java/com/att/training/spring/boot/demo/AsyncConfig.java) & [using `@Async`](src/main/java/com/att/training/spring/boot/demo/AsyncRunner.java)
  * HTTP [Async controller](src/main/java/com/att/training/spring/boot/demo/user/AsyncUserController.java)
  * [Testing Async controllers](src/test/java/com/att/training/spring/boot/demo/AsyncControllerTest.java)
* Serialization / deserialization with Jackson
  * Immutable POJOs (see lombok.config for handling POJOs with **single** constructor parameter)
  * [Deserialization POJOs so that collections are never null](src/test/java/com/att/training/spring/boot/demo/JacksonTest.java)
  * [Serialization / deserialization of jsr310 types (java.time) in @Controller](src/test/java/com/att/training/spring/boot/demo/datetime/DateTimeControllerTest.java)
* [CommandLineRunner](src/main/java/com/att/training/spring/boot/demo/AppConfig.java)
* Enabling the built-in [request logging filter](src/main/java/com/att/training/spring/boot/demo/AppConfig.java)
* [Lombok copyable annotations]() (see lombok.config)
* [spring-aop](src/main/java/com/att/training/spring/boot/demo/RandomDelayAspect.java)
* Swagger2 using [springfox](https://springfox.github.io/springfox/docs/current/#springfox-spring-mvc-and-spring-boot) ([swagger url](http://localhost:8090/demo/swagger-ui/))
* spring-actuator
  * [Info](http://localhost:8090/demo/actuator/info)
  * [Health](http://localhost:8090/demo/actuator/health)
  * [Metrics](http://localhost:8090/demo/actuator/metrics)
* Integration tests MockMvc using Junit Jupiter
* [Adding Servlet filters and testing them](src/test/java/com/att/training/spring/boot/demo/FilterTest.java)  
* [Using POJOs instead of simple method args in Controller methods](src/test/java/com/att/training/spring/boot/demo/ControllerMethodParametersTest.java)  

### Building the application:
```
./mvnw clean install -gs settings.xml
```

### Running the application:
```
./mvnw spring-boot:run
```
or just run the main in class [SpringMvcBootApplication](src/main/java/com/att/training/spring/boot/demo/SpringMvcBootApplication.java)

### The following API is exposed:
* [/demo/users/{id}](http://localhost:8090/demo/users/1) - Fetch user with id {id} (GET)
* [/demo/users/{id}](http://localhost:8090/demo/users/1) - Delete user with id {id} (DELETE)
* [/demo/users](http://localhost:8090/demo/users) - Fetch all users (GET)
* [/demo/users](http://localhost:8090/demo/users) - Update user (PUT). You can modify names or age (based on id of course)

### Setting up Metrics:
* Enable key management.metrics.export.graphite.enabled in the [application.yml](src/main/resources/application.yml)
* From your terminal, cd into the metrics directory.
* run `docker-compose up -d`
* Run the spring-boot app
* Go to <http://localhost:3000>  
  * Log in (admin/admin)
  * Change your password, as required
  * Click the Spring Boot Demo dashboard
  * Make some requests to [/demo/users/{id}](http://localhost:8090/demo/users/1) and [/demo/users](http://localhost:8090/demo/users) to see the response times.

 