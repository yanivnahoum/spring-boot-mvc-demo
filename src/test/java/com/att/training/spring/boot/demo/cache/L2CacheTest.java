package com.att.training.spring.boot.demo.cache;

import com.att.training.spring.boot.demo.tc.MySqlSingletonContainer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.ttddyy.dsproxy.asserts.ProxyTestDataSource;
import org.hibernate.annotations.Cache;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.lang.NonNull;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.QueryHint;
import javax.transaction.Transactional;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static net.ttddyy.dsproxy.asserts.assertj.DataSourceAssertAssertions.assertThat;
import static org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE;
import static org.hibernate.jpa.QueryHints.HINT_CACHEABLE;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@Slf4j
@TestInstance(PER_CLASS)
@Transactional
class L2CacheTest extends MySqlSingletonContainer {

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ProxyTestDataSource testDataSource;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private PostRepository postRepository;

    @DynamicPropertySource
    static void hibernateProps(DynamicPropertyRegistry registry) {
        registry.add("spring.jpa.properties.hibernate.generate_statistics", () -> true);
        registry.add("spring.jpa.properties.hibernate.cache.use_second_level_cache", () -> true);
        registry.add("spring.jpa.properties.hibernate.cache.use_query_cache", () -> true);
        registry.add("spring.jpa.properties.hibernate.cache.region.factory_class",
                () -> "com.hazelcast.hibernate.HazelcastCacheRegionFactory");
    }

    @BeforeAll
    void init() {
        transactionTemplate.execute(status -> {
            for (int i = 0; i < 4; ++i) {
                Post post = new Post("post#" + i, PostStatus.APPROVED)
                        .addComment(new PostComment(String.format("[comment #1] Post #%s rocks!", i)))
                        .addComment(new PostComment(String.format("[comment #2] Post #%s rocks!", i)));
                entityManager.persist(post);
            }
            return null;
        });
    }

    @BeforeEach
    void beforeEach() {
        testDataSource.reset();
//        entityManager.getEntityManagerFactory().getCache().evictAll();
    }

    @Test
    void findUsingEntityManager() {
        Post fetchedPost = entityManager.find(Post.class, 1L);
        logPost(fetchedPost);
        assertThat(testDataSource).hasSelectCount(0);
    }

    @Test
    void findUsingRepoDefault() {
        Post post = postRepository.findById(1L).orElseThrow();
        logPost(post);
        assertThat(testDataSource).hasSelectCount(0);
    }

    @RepeatedTest(3)
    void findAllUsingRepoDefault(RepetitionInfo repetitionInfo) {
        List<Post> posts = postRepository.findAll();
        // This will trigger additional N queries for the comments for the first repetition
//        posts.forEach(this::logPost);
        assertThat(testDataSource).hasSelectCount(repetitionInfo.getCurrentRepetition() > 1 ? 0 : 1);
    }

    @RepeatedTest(3)
    void findUsingRepoWithCustomQuery(RepetitionInfo repetitionInfo) {
        Post post = postRepository.findByIdWithExplicitJoinFetch(1L);
        logPost(post);
        assertThat(testDataSource).hasSelectCount(repetitionInfo.getCurrentRepetition() > 1 ? 0 : 1);
    }

    @RepeatedTest(3)
    void findUsingRepoWithGraph(RepetitionInfo repetitionInfo) {
        Post post = postRepository.getById(1L);
        logPost(post);
        assertThat(testDataSource).hasSelectCount(repetitionInfo.getCurrentRepetition() > 1 ? 0 : 1);
    }

    @RepeatedTest(3)
    void findAllOrderByTitleDesc(RepetitionInfo repetitionInfo) {
        List<Post> posts = postRepository.findAllByOrderByTitleDesc();
        posts.forEach(post -> log.info("Post #{}, title='{}'", post.getId(), post.getTitle()));
        assertThat(testDataSource).hasSelectCount(repetitionInfo.getCurrentRepetition() > 1 ? 0 : 1);
    }

    private void logPost(Post fetchedPost) {
        List<PostComment> comments = fetchedPost.getComments();
        log.info("Post: {} has {} comments", fetchedPost.getId(), comments.size());
        log.info("Post comments: {}", comments);
    }
}

interface PostRepository extends JpaRepository<Post, Long> {

    @QueryCache
    @Override
    @NonNull
    List<Post> findAll();

    @QueryCache
    List<Post> findAllByOrderByTitleDesc();

    @QueryCache
    @Query("select p from Post p join fetch p.comments where p.id = :id")
    Post findByIdWithExplicitJoinFetch(Long id);

    @QueryCache
    @EntityGraph(attributePaths = "comments")
    Post getById(Long id);
}

@Target(METHOD)
@Retention(RUNTIME)
@Documented
@QueryHints(@QueryHint(name = HINT_CACHEABLE, value = "true"))
@interface QueryCache {}

@Entity
@Cache(usage = READ_WRITE)
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

    @Cache(usage = READ_WRITE)
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
@Cache(usage = READ_WRITE)
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
