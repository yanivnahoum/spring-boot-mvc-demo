---
description: "Guidelines for testing spring-boot-mvc-demo (Spring Boot MVC demo application)"
applyTo: "src/main/java/com/att/training/spring/boot/demo/**/*.java,src/test/java/com/att/training/spring/boot/demo/**/*.java"
version: 1.0.5
---

# Overview

Single-stack repository: Java 21 + Spring Boot 3.5.x. Testing leverages JUnit Platform (engine) with AssertJ (EXCLUSIVE
assertion API), Spring Test slices, Mockito (for mocking only, NOT using @InjectMocks), and RestAssured for HTTP-level
integration. Use `org.springframework.test.web.servlet.assertj.MockMvcTester` instead of legacy `MockMvc` for controller
slice tests. Build scripts for both Maven (`pom.xml`) and Gradle (`build.gradle.kts`) exist; choose one primary in CI to
avoid divergence.

Stacks detected: java-spring (JUnit 5 platform runner `mvn test` / `gradlew test`). Monorepo: false.

Key directories:

- Source: `src/main/java/com/att/training/spring/boot/demo/`
- Tests:  `src/test/java/com/att/training/spring/boot/demo/`

# Running Tests

Primary commands (choose one build system):

- Maven full test suite (non-interactive): `mvn -q test`
- Maven with clean build: `mvn -q clean verify`
- Gradle full test suite: `./gradlew test`
- Gradle clean & test with report: `./gradlew clean test`

Focused / single test class execution:

- Maven: `mvn -Dtest=UserControllerTest test`
- Gradle: `./gradlew test --tests "*UserControllerTest"`

(If RestAssured integration tests become slower, you may segment with Maven Surefire includes/excludes or Gradle test
filtering patterns.)

# Naming & Structure

- Test classes end with `Test` (observed): e.g., `UserControllerTest`, `RandomDelayAspectTest`.
- All tests placed under mirrored package path relative to source for discoverability.
- Utility/support classes (e.g., `JsonUtils`) live in test tree; keep them in a `utils` or `support` subpackage.
- Keep pure unit tests fast and side-effect free; avoid starting full Spring context unless verifying integration (
  controllers, configuration binding, AOP, filters, serialization specifics).

# Test Style & Conventions

Adopt AAA (Arrange, Act, Assert) or Given/When/Then comments for clarity.

- Assertions: Use AssertJ only (`assertThat`, `assertThatThrownBy`, `assertThatCode`). Do NOT use JUnit Jupiter
  assertion methods.
- Exception testing: `assertThatThrownBy(() -> call()) .isInstanceOf(...) .hasMessageContaining("...");`
- Determinism: Avoid `Thread.sleep`; for async/timing assertions, inject a clock or abstract delay via an interface (
  e.g., `Sleeper`) and mock it.
- One behavioral focus per test method; multiple related AssertJ assertions permitted.
- Avoid Mockito `@InjectMocks`; explicitly instantiate the class under test in `@BeforeEach` passing mocks and test
  doubles. This improves constructor clarity and prevents hidden injection paths.

JSON representation:

- Always use Java text blocks (""" ... """) for request and expected JSON to avoid escaping quotes and to improve
  readability.
- Prefer alignment of keys and trailing commas omitted for clarity; keep stable ordering for strict comparisons.
- Use `.isLenientlyEqualTo` for order-insensitive / extra-field-tolerant checks; use `.isStrictlyEqualTo` when exact
  structural match (ordering + fields) is required.

Allowed assertion forms:

- Direct AssertJ `assertThat(...)` / `assertThatThrownBy(...)` for plain units.

Example instantiation pattern:

```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat; // AssertJ only

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository repo;
    @Mock
    private Clock clock;
    private UserService service;

    @BeforeEach
    void setUp() {
        // Explicit manual construction; no @InjectMocks
        service = new UserService(repo, clock);
    }

    // Example simple behavior test
    // @Test
    // void findsUserDelegatesToRepository() { ... }
}
```

# Strategy (Test Levels)

Unit Tests:

- Scope: Plain logic, DTO validation edge cases, stream utilities.
- Do NOT load Spring context; rely on Mockito for collaborators. Instantiate subject manually (no `@InjectMocks`).
- Mock boundaries: repositories, network/external calls, time/delay providers.

Slice / Component Tests:

- `@WebMvcTest` for controller request/response shape, validation errors, and exception translation using
  `MockMvcTester` (AssertJ-centric fluent API) instead of raw `MockMvc`.
- `@JsonTest` (can be added) for focused Jackson ser/deser.

Integration Tests:

- `@SpringBootTest(webEnvironment = RANDOM_PORT)` with RestAssured or TestRestTemplate for end-to-end HTTP (status
  codes, JSON contracts, actuator endpoints, OpenAPI spec presence).
- AOP verification (RandomDelayAspect) if timing/proxy behavior required; stub or mock delay provider where feasible for
  determinism.

Cross-Cutting:

- Configuration binding: Validate `@ConfigurationProperties` via Spring context property overrides or Binder-based unit
  approach.
- Prototype vs singleton scope: Validate instance identity semantics.
- Serialization: Round-trip, null handling, polymorphism, BigDecimal precision + negative malformed input cases.

Performance / Non-functional (Optional Enhancements):

- Lightweight startup smoke test (avoid fragile strict timing thresholds).
- Mutation testing (PIT) to strengthen assertion quality.

# Example Patterns

## Unit Test (Happy Path) – AssertJ Only

```java
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MathUtilsTest {
    @Test
    void sum_twoPositiveIntegers_returnsTotal() {
        int result = 3 + 5;
        assertThat(result).isEqualTo(8);
    }
}
```

## Unit Test (Edge / Exception) – AssertJ Exception Style

```java
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DivisionUtilsTest {
    int divide(int n, int d) {
        if (d == 0) throw new IllegalArgumentException("d must not be zero");
        return n / d;
    }

    @Test
    void divide_byZero_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> divide(4, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be zero");
    }
}
```

## Controller Slice Test Using MockMvcTester – POST with JSON Text Blocks

```java
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(UserController.class)
class UserRegistrationTests {
    @Autowired
    private MockMvcTester mockMvcTester;
    @MockitoBean
    private UserService userService;

    @Test
    void userRegistrationSuccessful() {
        String requestBody = """
                {
                  "firstName": "John",
                  "lastName": "Doe",
                  "email": "joh@gmail.com",
                  "role": "ROLE_USER"
                }
                """;

        // (Optional) stub service if controller delegates creation
        // when(userService.register(...)).thenReturn(...);

        MvcTestResult testResult = mockMvcTester
                .post()
                .uri("/api/users")
                .param("p1", "v1")
                .param("p2", "v2") // example query params
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .exchange();

        assertThat(testResult)
                .hasStatus(HttpStatus.CREATED)
                .bodyJson()
                .isLenientlyEqualTo("""
                        {
                          "firstName": "John",
                          "lastName": "Doe",
                          "email": "joh@gmail.com",
                          "role": "ROLE_USER"
                        }
                        """);

        // JSON Fixture Comparison (ClassPathResource)
        // Assume user-registration-response.json exists under src/test/resources/fixtures/
        var expected = new ClassPathResource("fixtures/user-registration-response.json", UserRegistrationTests.class);
        assertThat(testResult)
                .hasStatusOk()
                .bodyJson()
                .isLenientlyEqualTo(expected);

        // Single JSON path expression:
        assertThat(testResult)
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.firstName").isEqualTo("John");

        // Multiple paths with custom assertions:
        assertThat(testResult)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.firstName", path -> assertThat(path).isEqualTo("John"))
                .hasPathSatisfying("$.lastName", path -> assertThat(path).isEqualTo("Doe"));

        // Mapping JSON to a Record for Rich Assertions
        assertThat(testResult)
                .hasStatusOk()
                .bodyJson()
                .convertTo(RegistrationResponse.class)
                .satisfies(response -> {
                    assertThat(response.firstName()).isEqualTo("John");
                    assertThat(response.lastName()).isEqualTo("Doe");
                    assertThat(response.email()).isEqualTo("john@gmail.com");
                    assertThat(response.role()).isEqualTo("ROLE_USER");
                });
    }
}
```

### MockMvcTester JSON Assertion Guide

- `bodyJson().isStrictlyEqualTo(jsonTextBlock)` – Exact structural & ordering match.
- `bodyJson().isLenientlyEqualTo(jsonTextBlock)` – Ignores ordering, allows extra fields (consumer-driven flexibility).
- `bodyJson().isStrictlyEqualTo(resource)` / `isLenientlyEqualTo(resource)` – Compare against classpath fixture.
- `bodyJson().extractingPath("$.field")` – Extract a simple scalar for direct assertion.
- `bodyJson().hasPathSatisfying("$.field", valueAssert)` – For custom assertions on extracted value.
- `bodyJson().convertTo(Type.class)` – Deserialize to the given type (record or POJO) and continue assertion chain.

### Best Practices for JSON in Tests

- Keep canonical fixtures under `src/test/resources/fixtures` or root test resources; name them after endpoint &
  status (e.g., `user-registration-201.json`).
- Use lenient comparison in evolving APIs; tighten to strict when stabilizing contracts.
- For large payloads, consider snapshot grouping (but avoid entire-snapshot brittleness—split into focused fixtures
  where possible).

# Mocking & Test Utilities

- Use Mockito strictly for mocks; manual constructor instantiation of class under test in `@BeforeEach` (no
  `@InjectMocks`).
- Adopt `MockMvcTester` for HTTP slice tests; keep `RestAssured` for full-stack (port-bound) integration.
- Introduce a `TestClock` or pass `Clock` into services for temporal determinism; stub with fixed instant in unit tests.
- Provide builder/factory for domain objects (e.g., `TestUsers.user()` returning canonical `User`).

# Stack Details: Java Spring (junit-jupiter)

- Language: Java 21.
- Runner: JUnit Platform (engine only; assertions via AssertJ exclusively).
- Assertions: AssertJ (exclusive).
- Mocking: Mockito (`@Mock` + manual instantiation) and Spring `@MockBean` for slices.
- HTTP Slice: `MockMvcTester`.
- Full Integration: RestAssured / TestRestTemplate (optional).
- AOP Testing: Aspect proxied beans with delay abstraction.

# Recommendations (Immediate Next Steps)

1. Migrate any remaining JUnit assertions to AssertJ (`assertThat`, `assertThatThrownBy`).
2. Replace legacy `MockMvc` usages with injected `MockMvcTester` in slice tests.
3. Remove `@InjectMocks` usages (none currently shown; ensure future tests follow manual instantiation pattern).
4. Add Jacoco + PIT configuration with baseline thresholds.
5. Introduce `@JsonTest` for serializer/deserializer focus.
6. Add actuator (`/actuator/health`) and OpenAPI (`/v3/api-docs`) smoke tests.

# Changelog

- 1.0.5: Added MockMvcTester JSON guidelines: enforced JSON text blocks, POST example with lenient equality, fixture
  comparison, path extraction, record mapping, expanded usage notes.
- 1.0.4: Updated MockMvcTester example and guidance to follow Spring Boot documentation (expectStatus/expectBody chain)
  and clarified allowed fluent expectations.
- 1.0.3: Corrected MockMvcTester example to use direct `mvc.get("/path", vars).exchange()` form; added detailed usage
  notes per Spring documentation.
- 1.0.2: Replaced openMocks initialization with @ExtendWith(MockitoExtension.class); corrected MockMvcTester example to
  match AssertJ response assertion style; updated version.
- 1.0.1: Enforced AssertJ-only assertions, adopted MockMvcTester example, added guidance eliminating @InjectMocks usage.
- 1.0.0: Initial generation (single Java Spring stack, baseline guidance, gaps identified).
