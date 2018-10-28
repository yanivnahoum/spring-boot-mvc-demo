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
* spring-actuator
  * [Info](http://localhost:8090/demo/actuator/info)
  * [Health](http://localhost:8090/demo/actuator/health)
  * [Metrics](http://localhost:8090/demo/actuator/info)
* Integration tests MockMvc using Junit Jupiter 

### Running the application:
```
mvn spring-boot:run
```
or just run the main in class [SpringMvcBootApplication](src/main/java/com/att/training/spring/boot/demo/SpringMvcBootApplication.java)

### The following API is exposed:
* [/demo/users/{id}](http://localhost:8090/demo/users/1) - Fetch user with id {id} (GET)
* [/demo/users/{id}](http://localhost:8090/demo/users/1) - Delete user with id {id} (DELETE)
* [/demo/users](http://localhost:8090/demo/users) - Fetch all users (GET)
* [/demo/users](http://localhost:8090/demo/users) - Update user (PUT). You can modify names or age (based on id of course)

### Setting up Metrics:
* Start up graphite docker:
```
docker run \
  -d \
  --name graphite \
  --restart=always \
  -p 80:80 \
  -p 2003-2004:2003-2004 \
  -p 2023-2024:2023-2024 \
  -p 8125:8125/udp \
  -p 8126:8126 \
  graphiteapp/graphite-statsd
```    
* Start up Grafana docker:
```
docker run \
  -d \
  -p 3000:3000 \
  --name=grafana \
  grafana/grafana
```
* Enable key metrics.export.graphite.enabled in the [application.yml](src/main/resources/application.yml)
* Run the spring-boot app
* Go to <http://localhost:3000>  
* Import the [spring-boot dashboard](grafana-dashboard.json) or create your own
 