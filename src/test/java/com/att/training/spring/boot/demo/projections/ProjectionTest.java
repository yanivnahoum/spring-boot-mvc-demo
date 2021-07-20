package com.att.training.spring.boot.demo.projections;

import com.att.training.spring.boot.demo.tc.MySqlSingletonContainer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Tuple;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@Slf4j
@TestInstance(PER_CLASS)
@Transactional
@TestPropertySource(properties = {
        "spring.jpa.properties.hibernate.integrator_provider=com.att.training.spring.boot.demo.projections.ClassImportIntegratorIntegratorProvider"
})
class ProjectionTest extends MySqlSingletonContainer {

    @BeforeAll
    void init() {
        transactionTemplate.executeWithoutResult(status -> {
            for (int i = 0; i < 4; ++i) {
                Post post = new Post("post#" + i, PostStatus.APPROVED)
                        .addComment(new PostComment(String.format("[comment #%s] Great post!", i)));
                entityManager.persist(post);
            }
        });
    }

    @Test
    void useProjectionWithJpql() {
        List<PostCommentDto> fetchedPostComments = entityManager.createQuery(
                "select new com.att.training.spring.boot.demo.projections.PostCommentDto(p.title, pc.review) " +
                        "from PostComment pc join pc.post p", PostCommentDto.class)
                .getResultList();
        assertThat(fetchedPostComments).isNotNull();
        log.info("PostCommentDtos: {}", fetchedPostComments);
    }

    @Test
    void useProjectionWithJpqlReturningTuple() {
        List<Tuple> fetchedPostComments = entityManager.createQuery(
                "select pc.review as review, p.title as postTitle " +
                        "from PostComment pc join pc.post p", Tuple.class)
                .getResultList();
        assertThat(fetchedPostComments).isNotNull();
        log.info("PostCommentDtos: {}", tuplesToString(fetchedPostComments));
    }

    @SuppressWarnings({"deprecation", "unchecked"})
    @Test
    void useProjectionWithNativeQuery() {
        List<AnotherPostCommentDto> fetchedPostComments = entityManager.createNativeQuery(
                "SELECT p.title AS postTitle, pc.review " +
                        "FROM post_comment pc INNER JOIN post p ON pc.post_id = p.id")
                .unwrap(NativeQuery.class)
                .setResultTransformer(Transformers.aliasToBean(AnotherPostCommentDto.class))
                .getResultList();
        assertThat(fetchedPostComments).isNotNull();
        log.info("AnotherPostCommentDtos: {}", fetchedPostComments);
    }

    @SuppressWarnings("unchecked")
    @Test
    void useProjectionWithNativeQueryReturningTuple() {
        List<Tuple> fetchedPostComments = entityManager.createNativeQuery(
                "SELECT p.title AS postTitle, pc.review " +
                        "FROM post_comment pc INNER JOIN post p ON pc.post_id = p.id", Tuple.class)
                .getResultList();
        assertThat(fetchedPostComments).isNotNull();
        log.info("AnotherPostCommentDtos: {}", tuplesToString(fetchedPostComments));
    }

    @Test
    void useProjectionWithJpqlWithSimpleNameDto() {
        List<PostCommentDto> fetchedPostComments = entityManager.createQuery(
                "select new PostCommentDto(p.title, pc.review) " +
                        "from PostComment pc join pc.post p", PostCommentDto.class)
                .getResultList();
        assertThat(fetchedPostComments).isNotNull();
        log.info("PostCommentDtos: {}", fetchedPostComments);
    }

    private String tuplesToString(List<Tuple> fetchedPostComments) {
        var builder = new StringBuilder("[");
        for (var tuple : fetchedPostComments) {
            builder.append(tupleToString(tuple));
        }
        builder.append("]");
        return builder.toString();
    }

    private String tupleToString(Tuple tuple) {
        return String.format("{review=%s, postTitle=%s}", tuple.get("review"), tuple.get("postTitle"));
    }
}
//interface PostRepository extends JpaRepository<Post, Long> {}
//
//interface PostCommentRepository extends JpaRepository<PostComment, Long> {}

@Entity
@Getter
@Setter
@NoArgsConstructor
class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;

    @Enumerated(EnumType.ORDINAL)
    @Column(columnDefinition = "tinyint")
    private PostStatus status;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostComment> comments = new ArrayList<>();

    public Post(String title, PostStatus status) {
        this.title = title;
        this.status = status;
    }

    public Post addComment(PostComment comment) {
        comments.add(comment);
        comment.setPost(this);
        return this;
    }
}

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
class PostComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String review;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    public PostComment(String review) {
        this.review = review;
    }
}

enum PostStatus {
    PENDING,
    APPROVED,
    SPAM
}
