# A Spring Boot 2 demo app 

This project demonstrates the following features:
* Integration using JUnit Jupiter
  * @SpringBootTest & MockMvc
  * @WebMvcTest & MockMvc
* Integration tests using TestContainers
  *  mySQL integration test
  
### Building the application:
```
./mvnw clean install [-P withITs|onlyITs]
./gradlew build [-PwithITs]
```
Use the withITs profile to run all tests (including the slower integration tests)
Use the onlyITs profile to run only the slower integration tests
### Running the application:
```
mvn spring-boot:run
```
or just run the main in class [SpringMvcBootApplication](src/main/java/com/att/training/spring/boot/demo/SpringMvcBootApplication.java)

### The following API is exposed:
* [/demo/users](http://localhost:8090/demo/users) - Fetch all users (GET)
* [/demo/users/{id}](http://localhost:8090/demo/users/1) - Fetch user with id {id} (GET)
* [/demo/users/{id}](http://localhost:8090/demo/users/1) - Delete user with id {id} (DELETE)
* [/demo/users](http://localhost:8090/demo/users) - Update user (PUT). You can modify names or age (based on id of course)