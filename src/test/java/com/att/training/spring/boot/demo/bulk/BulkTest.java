package com.att.training.spring.boot.demo.bulk;

import com.att.training.spring.boot.demo.tc.DatasourceProxyBeanPostProcessor;
import com.att.training.spring.boot.demo.tc.MySqlSingletonContainer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.ttddyy.dsproxy.asserts.ProxyTestDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.sql.DataSource;
import java.util.List;

import static net.ttddyy.dsproxy.asserts.assertj.DataSourceAssertAssertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
@Transactional
class BulkTest extends MySqlSingletonContainer {

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DataSource dataSource;
    private ProxyTestDataSource testDataSource;
    private List<User> users;

    @BeforeAll
    void init() {
        initTestDataSource();
        users = userRepository.saveAll(List.of(
                new User("Alice", "Cooper"),
                new User("Bob", "DeNiro"),
                new User("Carl", "Zeiss")
        ));
    }

    @BeforeEach
    void beforeEach() {
        testDataSource.reset();
    }

    @Test
    void deleteAll() {
        userRepository.deleteAll(users);
        entityManager.flush();
        assertThat(testDataSource).hasSelectCount(3)
                .hasBatchPreparedCount(1)
                .hasDeleteCount(1);
    }

    @Test
    void deleteAllInBatch() {
        userRepository.deleteInBatch(users);
        entityManager.flush();
        assertThat(testDataSource).hasDeleteCount(1);
    }

    private void initTestDataSource() {
        var advised = (Advised) dataSource;
        var advisors = advised.getAdvisors();
        var advice = (DatasourceProxyBeanPostProcessor.ProxyDataSourceInterceptor) advisors[0].getAdvice();
        testDataSource = (ProxyTestDataSource) advice.getDataSource();
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
