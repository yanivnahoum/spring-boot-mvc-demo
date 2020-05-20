# A Spring Boot 2 component testing demo app 

This project demonstrates the following features:
* Testing HTTP endpoints:
  * @SpringBootTest & MockMvc
  * @WebMvcTest & MockMvc
* Testing databases using TestContainers
  * mySQL component test
    * MySQL container launched [via JDBC URL scheme](src/test/java/com/att/training/spring/boot/demo/tc/SpringBootMySqlTest.java) (container per test class)
    * MySQL container launched [via JUnit Jupiter extension](src/test/java/com/att/training/spring/boot/demo/tc/MySqlIntegrationTest.java) (container per test class)
    * MySQL container launched [in singleton mode](src/test/java/com/att/training/spring/boot/demo/tc/MySqlSingletonContainer.java) (one container per entire test suite)
  * [UserRepositoryTest](src/test/java/com/att/training/spring/boot/demo/tc/UserRepositoryTest.java) - demonstrates how @Transactional works in tests
* Testing web clients
  * [Using Spring's `MockRestServiceServer`](src/test/java/com/att/training/spring/boot/demo/quote/QuoteClientTest.java)
  * Using WireMock
    * [Explicit start/stop](src/test/java/com/att/training/spring/boot/demo/quote/BasicWireMockQuoteClientTest.java)
    * [With spring-cloud-contract's `@AutoConfigureWireMock`](src/test/java/com/att/training/spring/boot/demo/quote/WireMockQuoteClientTest.java)
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

### This app exposes the following API:
* [/demo/users](http://localhost:8090/demo/users) - Fetch all users (GET)
* [/demo/users/{id}](http://localhost:8090/demo/users/1) - Fetch user with id {id} (GET)
* [/demo/users/{id}](http://localhost:8090/demo/users/1) - Delete user with id {id} (DELETE)
* [/demo/users](http://localhost:8090/demo/users) - Update user (PUT). You can modify names or age (based on id of course)