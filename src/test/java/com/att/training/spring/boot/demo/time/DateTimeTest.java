package com.att.training.spring.boot.demo.time;

import com.att.training.spring.boot.demo.tc.MySqlSingletonContainer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static javax.persistence.GenerationType.IDENTITY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@Entity
@Table(name = "date_time_test")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@ToString
class DateTimeEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    private LocalDate localDate = LocalDate.of(2020, 12, 31);
    private LocalDate localDate2 = LocalDate.now();
    @CreationTimestamp private Instant instant;
    @CreatedDate private LocalDateTime localDateTime;


//    private LocalDateTime localDateTime = LocalDateTime.now();
//    private LocalDateTime localDateTimeInUTC = LocalDateTime.parse(instant.toString(), DateTimeFormatter.ISO_DATE_TIME);
//    private ZonedDateTime zonedDateTime = ZonedDateTime.now();
//    private ZonedDateTime zonedDateTimeInUTC = ZonedDateTime.parse(instant.toString());
}

interface DateTimeRepository extends JpaRepository<DateTimeEntity, Integer> {
}

@SpringBootTest
class DateTimeTest extends MySqlSingletonContainer {

    @Autowired private DateTimeRepository repo;

    @BeforeAll
    static void beforeAll() {
//        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
//        TimeZone.setDefault(TimeZone.getTimeZone("US/Hawaii"));
//        TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Auckland"));
    }

    @Test
    void persistAndFetch() {
        var entity = new DateTimeEntity();
        var savedEntity = repo.save(entity);
        assertThat(savedEntity.getId()).isNotNull();

        var fetched = repo.findById(savedEntity.getId());
        System.out.println(fetched.orElseThrow());
    }
}

class Service {

    int foo(int x, Class<?> clazz) {
        return x + clazz.hashCode();
    }
}

@ExtendWith(MockitoExtension.class)
class Tester {

    @Test
    void test(@Mock Service service) {
        when(service.foo(anyInt(), eq(String.class))).thenReturn(1);
        assertThat(service.foo(100, String.class)).isOne();
    }
}
