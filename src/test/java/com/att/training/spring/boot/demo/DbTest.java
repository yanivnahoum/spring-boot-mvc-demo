package com.att.training.spring.boot.demo;

import com.att.training.spring.boot.demo.tc.MySqlSingletonContainer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Commit;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.transaction.Transactional;

class DbTest extends MySqlSingletonContainer {

    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    @Commit
    void test() {
        entityManager.persist(new Post(1L, "stuff", PostStatus.APPROVED));
        entityManager.find(Post.class, 1L);
    }
}

@Entity
@Table
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
class Post {

    @Id
    private Long id;

    private String title;

    @Enumerated(EnumType.ORDINAL)
    @Column(columnDefinition = "tinyint")
    private PostStatus status;
}

enum PostStatus {
    PENDING,
    APPROVED,
    SPAM
}