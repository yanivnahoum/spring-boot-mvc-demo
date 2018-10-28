# A Spring Boot 2 demo app 

This project demonstrates the following features:
* Spring DI
* HTTP endpoints (Controllers)
* Exception handlers
* Bean Validation 2.0
* Binding POJOs to configuration 
* Serialization / deserialization of immutable POJOs
* [CommandLineRunner](src/main/java/com/att/training/spring/boot/demo/AppConfig.java)
* Enabling the built-in [request logging filter](src/main/java/com/att/training/spring/boot/demo/AppConfig.java)
* spring-aop (see @RandomDelay)
* Integration tests MockMvc using Junit Jupiter 
* Customizing Jackson

### Running the application:
```
mvn spring-boot:run
```

### The following API is exposed:
* [/demo/users/{id}](http://localhost:8090/demo/users/1) - Fetch user with id {id} (GET)
* [/demo/users/{id}](http://localhost:8090/demo/users/1) - Delete user with id {id} (DELETE)
* [/demo/users](http://localhost:8090/demo/users) - Fetch all users (GET)
* [/demo/users](http://localhost:8090/demo/users) - Update user (PUT). You can modify names or age (based on id of course)