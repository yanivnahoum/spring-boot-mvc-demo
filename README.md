# A Spring Boot 2 demo app 

This project demonstrates the following features:
* Spring DI
* HTTP endpoints (Controllers)
* Exception handlers
* Binding POJOs to configuration 
* Bean Validation 2.0
* Customizing a Jackson ObjectMapper to be used by Jersey
* Serialization / deserialization of immutable POJOs
* CommandLineRunner
* Enabling the built-in request logging filter
* Integration tests MockMvc
* Spring @Transactional implementation (see CustomTransactionManager)
* spring-aop (see @UnitOfWork)
* JAX-RS filter name binding (see @ValidateUser)

### Running the application:
```
mvn spring-boot:run
```

### The following APIs are exposed:
* [/demo/users](http://localhost:8080/demo/users) - Fetch all users (GET)
* [/demo/users/{id}](http://localhost:8080/demo/users/1) - Fetch user with id {id} (GET)
* [/demo/users](http://localhost:8080/demo/users) - Update user (PUT). You can modify names or age (based on id of course)