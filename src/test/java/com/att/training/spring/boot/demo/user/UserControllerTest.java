package com.att.training.spring.boot.demo.user;

import com.att.training.spring.boot.demo.Slow;
import com.att.training.spring.boot.demo.errors.ExceptionHandlers;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.NestedTestConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.NestedTestConfiguration.EnclosingConfiguration.INHERIT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slow
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@Transactional
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private static final String[] options = {
            "--character-set-server=latin1",
            "--collation-server=latin1_general_ci",
            "--log-bin-trust-function-creators=true"
    };

    @Container
    private static final MySQLContainer<?> mySqlContainer = createAndStart();

    private static MySQLContainer<?> createAndStart() {
        var container = new MySQLContainer<>("mysql:8.0.22")
                .withDatabaseName("demo")
                .withCreateContainerCmdModifier(cmd -> cmd.withCmd(options));
        // This is only needed because of spring-cloud-contract-wiremock's WireMockTestExecutionListener
        // It causes the application context to load (and activates #mySqlProperties()) before the container is ready
        // It's also needed in case you're using @TestContainers/@Container with junit-jupiter's @TestInstance(PER_CLASS)
        container.start();
        return container;
    }

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper mapper;

    @DynamicPropertySource
    static void mySqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mySqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mySqlContainer::getUsername);
        registry.add("spring.datasource.password", mySqlContainer::getPassword);
    }

    @Test
    void whenGetUsers_shouldReturn200OK_withAllUsers() throws Exception {
        mockMvc.perform(get("/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize((int) userRepository.count())));
    }

    @Test
    void whenDeleteUser_givenId1_shouldReturn200Ok() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    // Nested classes do NOT inherit the full spring context, they are NOT transactional.
    // They should probably be avoided in this context.
    @Nested
    @NestedTestConfiguration(INHERIT)
    @DisplayName("When calling GET /users/{id}")
    class GetSingleUser {

        @Test
        void givenId1_shouldReturn200OK_withMichaelAsJson() throws Exception {
            mockMvc.perform(get("/users/{id}", 1))
                   .andDo(print())
                   .andExpect(status().isOk())
                   .andExpect(content().json("{'id':1,'firstName':'Michael','lastName':'Jordan','age':50}"))
                   .andExpect(jsonPath("$.firstName", is("Michael")));
        }

        @Test
        void givenMissingId_shouldReturn404NotFound() throws Exception {
            int id = 4;
            String expectedJson = String.format("{ code: 5001, message = 'UserDto not found: %d' }", id);
            mockMvc.perform(get("/users/{id}", id))
                   .andDo(print())
                   .andExpect(status().isNotFound())
                   .andExpect(content().json(expectedJson));
        }
        @Test
        void whenUserServiceThrowsGenericException_shouldReturn500InternalServerError(@Mock UserService userService) throws Exception {
            mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService, mapper))
                                     .setControllerAdvice(new ExceptionHandlers())
                                     .build();

            String errorMessage = "Thrown intentionally by mock!";
            int id = 2;
            when(userService.fetch(id)).thenThrow(new IllegalArgumentException(errorMessage));
            String expectedJson = String.format("{ code: 9999, message = '%s' }", errorMessage);

            mockMvc.perform(get("/users/{id}", id))
                   .andDo(print())
                   .andExpect(status().isInternalServerError())
                   .andExpect(content().json(expectedJson));
        }

    }
}