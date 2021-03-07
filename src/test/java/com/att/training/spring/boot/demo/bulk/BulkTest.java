package com.att.training.spring.boot.demo.bulk;

import com.att.training.spring.boot.demo.tc.MySqlSingletonContainer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.ttddyy.dsproxy.asserts.ProxyTestDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.context.TestPropertySource;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.List;

import static net.ttddyy.dsproxy.asserts.assertj.DataSourceAssertAssertions.assertThat;

//@Transactional
class BulkTest extends MySqlSingletonContainer {

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProxyTestDataSource testDataSource;
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
    void deleteAllEntities() {
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
    void deleteAllInBatch() {
        userRepository.deleteAllInBatch();
        assertThat(testDataSource).hasDeleteCount(1);
    }

    @Test
    void deleteInBatch() {
        userRepository.deleteInBatch(users);
        assertThat(testDataSource).hasDeleteCount(1);
    }

    @Nested
    @TestPropertySource(properties = "spring.jpa.properties.hibernate.jdbc.batch=25")
    class WithBatching {

        @Test
        void deleteAll() {
            userRepository.deleteAll();
            assertThat(testDataSource).hasSelectCount(1)
                    .hasBatchPreparedCount(1)
                    .hasDeleteCount(3);
        }
    }

}

interface UserRepository extends JpaRepository<User, Long> {}

@Entity
@Table
@Getter
@Setter
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
