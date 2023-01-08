package com.att.training.spring.boot.demo.cache;

import com.att.training.spring.boot.demo.tc.MySqlSingletonContainer;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.QueryHint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Cache;
import org.hibernate.query.NativeQuery;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.lang.NonNull;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static net.ttddyy.dsproxy.asserts.assertj.DataSourceAssertAssertions.assertThat;
import static org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE;
import static org.hibernate.jpa.AvailableHints.HINT_CACHEABLE;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

@Slf4j
@TestInstance(PER_CLASS)
@Transactional
@TestPropertySource(properties = {
        "spring.jpa.properties.hibernate.generate_statistics=true",
        "spring.jpa.properties.hibernate.cache.use_second_level_cache=true",
        "spring.jpa.properties.hibernate.cache.use_query_cache=true",
        "spring.jpa.properties.hibernate.cache.region.factory_class=com.hazelcast.hibernate.HazelcastCacheRegionFactory"
})
class L2CacheTest extends MySqlSingletonContainer {

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private PostCommentRepository postCommentRepository;

    @BeforeAll
    void init() {
        transactionTemplate.executeWithoutResult(status -> {
            for (int i = 0; i < 4; ++i) {
                Post post = new Post("post#" + i, PostStatus.APPROVED)
                        .addComment(new PostComment(String.format("[comment #1] Post #%s rocks!", i)))
                        .addComment(new PostComment(String.format("[comment #2] Post #%s rocks!", i)));
                entityManager.persist(post);
            }
        });
    }

    @Override
    protected List<String> tablesToDrop() {
        return List.of("post_comment", "post");
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
        Post post = postRepository.readById(1L);
        logPost(post);
        assertThat(testDataSource).hasSelectCount(repetitionInfo.getCurrentRepetition() > 1 ? 0 : 1);
    }

    @Nested
    class QueryCacheTest {

        @RepeatedTest(3)
        void findAllOrderByTitleDesc(RepetitionInfo repetitionInfo) {
            if (repetitionInfo.getCurrentRepetition() == 1) invalidateQueryCache();
            List<Post> posts = postRepository.findAllByOrderByTitleDesc();
            posts.forEach(post -> log.info("Post #{}, title='{}'", post.getId(), post.getTitle()));
            assertThat(testDataSource).hasSelectCount(repetitionInfo.getCurrentRepetition() > 1 ? 0 : 1);
        }

        @Transactional(propagation = NOT_SUPPORTED)
        @Test
        void whenRelatedJpqlQueryIsExecuted_thenQueryCacheIsInvalidated() {
            // Invalidate cache & run query for the first time, caching it.
            invalidateQueryCache();
            postRepository.findAllByOrderByTitleDesc();

            // Load and update a post - this entity is part of the former query
            Post post = postRepository.findById(1L).orElseThrow();
            post.setTitle("post#5");
            log.info("Updating post(id=1) title");
            postRepository.save(post);

            // Execute query again and make sure it hits the db
            testDataSource.reset();
            List<Post> posts = postRepository.findAllByOrderByTitleDesc();
            posts.forEach(p -> log.info("Post #{}, title='{}'", p.getId(), p.getTitle()));
            assertThat(testDataSource).hasSelectCount(1);
        }

        @Transactional(propagation = NOT_SUPPORTED)
        @Test
        void whenUnrelatedJpqlQueryIsExecuted_thenQueryCacheIsNotInvalidated() {
            // Invalidate cache & run query for the first time, caching it.
            invalidateQueryCache();
            postRepository.findAllByOrderByTitleDesc();

            // Load and update a comment - not part of the former query
            PostComment comment = postCommentRepository.findById(1L).orElseThrow();
            comment.setReview("changed!");
            log.info("Updating comment(id=1) review");
            postCommentRepository.save(comment);

            // Execute query again and make sure it doesn't hit the db, but taken from the query cache instead
            testDataSource.reset();
            List<Post> posts = postRepository.findAllByOrderByTitleDesc();
            posts.forEach(post -> log.info("Post #{}, title='{}'", post.getId(), post.getTitle()));
            assertThat(testDataSource).hasSelectCount(0);
        }

        @Transactional(propagation = NOT_SUPPORTED)
        @Test
        void whenNativeQueryIsExecuted_thenQueryCacheIsInvalidated() {
            // Invalidate cache & run query for the first time, caching it.
            invalidateQueryCache();
            postRepository.findAllByOrderByTitleDesc();

            // Update a comment - this entity is NOT part of the former query,
            // but since we're using a native query, Hibernate is in the dark
            // and is forced to invalidate all the cache
            log.info("Updating comment(id=1) review with native query");
            postCommentRepository.nativeUpdate(1L, "changed from native!");

            // Execute query again and make sure it hits the db
            testDataSource.reset();
            List<Post> posts = postRepository.findAllByOrderByTitleDesc();
            posts.forEach(p -> log.info("Post #{}, title='{}'", p.getId(), p.getTitle()));
            assertThat(testDataSource).hasSelectCount(1);
        }

        @Transactional(propagation = NOT_SUPPORTED)
        @Test
        void whenNativeQueryWithEntitySynchronizationIsExecuted_thenQueryCacheIsNotInvalidated() {
            // Invalidate cache & run query for the first time, caching it.
            invalidateQueryCache();
            postRepository.findAllByOrderByTitleDesc();

            // Update a comment - this entity is NOT part of the former query,
            // but since we're using a native query, Hibernate is in the dark
            // We manually provide the required information using addSynchronizedEntityClass()
            log.info("Updating comment(id=1) review with native query & synced entity");
            transactionTemplate.executeWithoutResult(status ->
                    entityManager.createNativeQuery("UPDATE post_comment SET review = 'changed from native& synced!' WHERE id = 1")
                            .unwrap(NativeQuery.class)
                            .addSynchronizedEntityClass(PostComment.class)
                            .executeUpdate());

            // Execute query again and make sure it doesn't hit the db, but taken from the query cache instead
            testDataSource.reset();
            List<Post> posts = postRepository.findAllByOrderByTitleDesc();
            posts.forEach(p -> log.info("Post #{}, title='{}'", p.getId(), p.getTitle()));
            assertThat(testDataSource).hasSelectCount(0);
        }

        private void invalidateQueryCache() {
            entityManager.getEntityManagerFactory()
                    .getCache()
                    .unwrap(org.hibernate.Cache.class)
                    .evictDefaultQueryRegion();
        }
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
    Post readById(Long id);
}

interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    @Transactional
    @Modifying
    @Query(value = "UPDATE post_comment SET review = :review WHERE id = :id", nativeQuery = true)
    void nativeUpdate(long id, String review);
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
