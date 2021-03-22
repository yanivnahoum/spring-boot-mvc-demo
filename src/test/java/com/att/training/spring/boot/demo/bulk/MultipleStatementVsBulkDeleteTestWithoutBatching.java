package com.att.training.spring.boot.demo.bulk;

import com.att.training.spring.boot.demo.tc.MySqlSingletonContainer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.ttddyy.dsproxy.asserts.ProxyTestDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.List;

import static net.ttddyy.dsproxy.asserts.assertj.DataSourceAssertAssertions.assertThat;

class MultipleStatementVsBulkDeleteTestWithoutBatching extends MySqlSingletonContainer {

    @Autowired private UserRepository userRepository;
    @Autowired private ProxyTestDataSource testDataSource;
    private List<User> users;

    @BeforeEach
    void beforeEach() {
        users = userRepository.saveAll(List.of(
                new User("Alice", "Cooper"),
                new User("Bob", "DeNiro"),
                new User("Carl", "Zeiss")
        ));
        testDataSource.reset();
    }

    @Test
    void findAllById() {
        userRepository.findAllById(List.of(1L, 2L));
        assertThat(testDataSource).hasSelectCount(1);
    }

    @Test
    void findAllByFirstName() {
        userRepository.findAllByFirstNameIn(List.of("Alice", "Bob"));
        assertThat(testDataSource).hasSelectCount(1);
    }

    @Test
    void deleteAllSpecifiedEntities() {
        userRepository.deleteAll(users);
        assertThat(testDataSource).hasSelectCount(3)
                .hasDeleteCount(3);
    }

    @Test
    void deleteAll() {
        userRepository.deleteAll();
        assertThat(testDataSource).hasSelectCount(1)
                .hasDeleteCount(3);
    }

    @Test
    void deleteInBatch() {
        userRepository.deleteInBatch(users);
        assertThat(testDataSource).hasSelectCount(0)
                .hasDeleteCount(1);
    }

    @Test
    void deleteAllInBatch() {
        userRepository.deleteAllInBatch();
        assertThat(testDataSource).hasSelectCount(0)
                .hasDeleteCount(1);
    }

    @Test
    void deleteByLastNameIsContaining() {
        userRepository.deleteByLastNameIsContaining("e");
        assertThat(testDataSource).hasSelectCount(1)
                .hasDeleteCount(3);
    }

    @Test
    void deleteInBulkByLastNameContaining() {
        userRepository.deleteInBulkByLastNameContaining("e");
        assertThat(testDataSource).hasSelectCount(0)
                .hasDeleteCount(1);
    }
}

class MultipleStatementVsBulkDeleteTestWithBatching extends MySqlSingletonContainer {

    @Autowired private UserRepository userRepository;
    @Autowired private ProxyTestDataSource testDataSource;

    @DynamicPropertySource
    static void hibernateProps(DynamicPropertyRegistry registry) {
        registry.add("spring.jpa.properties.hibernate.jdbc.batch_size", () -> 25);
    }

    @BeforeEach
    void beforeEach() {
        userRepository.saveAll(List.of(
                new User("Alice", "Cooper"),
                new User("Bob", "DeNiro"),
                new User("Carl", "Zeiss")
        ));
        testDataSource.reset();
    }

    @Test
    void deleteAll() {
        userRepository.deleteAll();
        assertThat(testDataSource).hasSelectCount(1)
                .hasBatchPreparedCount(1)
                .hasDeleteCount(1);
    }
}

@Transactional
interface UserRepository extends JpaRepository<User, Long> {
    void deleteByLastNameIsContaining(String token);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from User u where u.lastName like %:token%")
    void deleteInBulkByLastNameContaining(String token);

    @Transactional(readOnly = true)
    List<User> findAllByFirstNameIn(List<String> names);
}

@Entity

@Table
@Getter
@NoArgsConstructor
class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String firstName;
    private String lastName;

    public User(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
