package com.att.training.spring.boot.demo.bulk;

import com.att.training.spring.boot.demo.tc.MySqlSingletonContainer;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static net.ttddyy.dsproxy.asserts.assertj.DataSourceAssertAssertions.assertThat;

class MultipleStatementVsBulkDeleteWithoutBatchingTest extends MySqlSingletonContainer {

    @Autowired private UserRepository userRepository;
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
    void deleteAllInBatchSpecifiedEntities() {
        userRepository.deleteAllInBatch(users);
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
    void deleteByLastNameContaining() {
        userRepository.deleteByLastNameContaining("e");
        assertThat(testDataSource).hasSelectCount(1)
                .hasDeleteCount(3);
    }

    @Test
    void deleteInBulkByLastNameContaining() {
        userRepository.deleteInBulkByLastNameContaining("e");
        assertThat(testDataSource).hasSelectCount(0)
                .hasDeleteCount(1);
    }

    @Test
    void deleteSingleEntity() {
        userRepository.delete(users.get(0));
        assertThat(testDataSource).hasSelectCount(1)
                .hasDeleteCount(1);
    }

    @Test
    void deleteSingleEntityById() {
        var aliceId = users.get(0).getId();
        userRepository.deleteById(aliceId);
        assertThat(testDataSource).hasSelectCount(1)
                .hasDeleteCount(1);
    }
}

@TestPropertySource(properties = "spring.jpa.properties.hibernate.jdbc.batch_size=25")
class MultipleStatementVsBulkDeleteWithBatchingTest extends MySqlSingletonContainer {

    @Autowired private UserRepository userRepository;

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
    void deleteByLastNameContaining(String token);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from User u where u.lastName like %:token%")
    void deleteInBulkByLastNameContaining(String token);
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
