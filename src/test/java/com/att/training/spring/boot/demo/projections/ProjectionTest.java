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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static net.ttddyy.dsproxy.asserts.assertj.DataSourceAssertAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@Slf4j
@TestInstance(PER_CLASS)
@Transactional
@TestPropertySource(properties = {
        "spring.jpa.properties.hibernate.integrator_provider=com.att.training.spring.boot.demo.projections.ClassImportIntegratorIntegratorProvider"
})
class ProjectionTest extends MySqlSingletonContainer {

    private static final int POST_COUNT = 4;
    private static final int POST_COMMENT_COUNT = POST_COUNT * 2;
    @Autowired private PostRepository postRepository;
    @Autowired private PostCommentRepository postCommentRepository;

    @BeforeAll
    void init() {
        transactionTemplate.executeWithoutResult(status -> {
            for (int i = 1; i <= POST_COUNT; ++i) {
                Post post = new Post("post#" + i, PostStatus.APPROVED)
                        .addComment(new PostComment(String.format("[comment #1] Post #%s is great!", i)))
                        .addComment(new PostComment(String.format("[comment #2] Post #%s is fantastic!", i)));
                entityManager.persist(post);
            }
        });
    }

    @Nested
    class JpqlQueries {
        private final ProjectionTest top = ProjectionTest.this;

        @Test
        void useProjectionWithJpqlReturningTuple() {
            List<Tuple> postComments = entityManager.createQuery(
                    "select pc.review as review, p.title as postTitle " +
                            "from PostComment pc join pc.post p", Tuple.class)
                    .getResultList();
            assertThat(postComments).hasSize(POST_COMMENT_COUNT);
            log.info("PostCommentDtos: {}", top.toString(postComments, top::toString));
        }

        @Test
        void useProjectionWithJpql() {
            List<PostCommentDto> postComments = entityManager.createQuery(
                    "select new com.att.training.spring.boot.demo.projections.PostCommentDto(p.title, pc.review) " +
                            "from PostComment pc join pc.post p", PostCommentDto.class)
                    .getResultList();
            assertThat(postComments).hasSize(POST_COMMENT_COUNT);
            log.info("PostCommentDtos: {}", postComments);
        }

        @Test
        void useProjectionWithJpqlWithSimpleNameDto() {
            List<PostCommentDto> fetchedPostComments = entityManager.createQuery(
                    "select new PostCommentDto(p.title, pc.review) " +
                            "from PostComment pc join pc.post p", PostCommentDto.class)
                    .getResultList();
            assertThat(fetchedPostComments).hasSize(POST_COMMENT_COUNT);
            log.info("PostCommentDtos: {}", fetchedPostComments);
        }
    }

    @Nested
    class NativeQueries {
        private final ProjectionTest top = ProjectionTest.this;

        @SuppressWarnings({"deprecation", "unchecked"})
        @Test
        void useProjectionWithNativeQuery() {
            List<AnotherPostCommentDto> postComments = entityManager.createNativeQuery(
                    "SELECT p.title AS postTitle, pc.review " +
                            "FROM post_comment pc INNER JOIN post p ON pc.post_id = p.id")
                    .unwrap(NativeQuery.class)
                    .setResultTransformer(Transformers.aliasToBean(AnotherPostCommentDto.class))
                    .getResultList();
            assertThat(postComments).hasSize(POST_COMMENT_COUNT);
            log.info("AnotherPostCommentDtos: {}", postComments);
        }

        @SuppressWarnings("unchecked")
        @Test
        void useProjectionWithNativeQueryReturningTuple() {
            List<Tuple> postComments = entityManager.createNativeQuery(
                    "SELECT p.title AS postTitle, pc.review " +
                            "FROM post_comment pc INNER JOIN post p ON pc.post_id = p.id", Tuple.class)
                    .getResultList();
            assertThat(postComments).hasSize(POST_COMMENT_COUNT);
            log.info("AnotherPostCommentDtos: {}", top.toString(postComments, top::toString));
        }
    }

    @Nested
    class SpringDataJpa {
        private final ProjectionTest top = ProjectionTest.this;

        @Test
        void useDerivedQuery_returningClassProjection() {
            List<PostCommentReviewDto> postComments = postCommentRepository.findAllFetchingReviewOnlyBy();
            assertThat(postComments).hasSize(POST_COMMENT_COUNT);
            log.info("PostCommentDtos: {}", postComments);
            assertThat(testDataSource).hasSelectCount(1);
        }

        @Test
        void useDerivedQuery_returningInterfaceProjection() {
            List<PostCommentShortSummary> postCommentSummaries = postCommentRepository.findAllFetchingShortSummaryBy();
            assertThat(postCommentSummaries).hasSize(POST_COMMENT_COUNT);
            log.info("PostCommentSummaries: {}", top.toString(postCommentSummaries, top::toString));
            assertThat(testDataSource).hasSelectCount(1);
        }

        @Test
        void useDerivedQuery_returningInterfaceProjectionWithNestedAssociation() {
            List<PostCommentSummary> postCommentSummaries = postCommentRepository.findAllFetchingSummaryBy();
            assertThat(postCommentSummaries).hasSize(POST_COMMENT_COUNT);
            log.info("PostCommentSummaries: {}", top.toString(postCommentSummaries, top::toString));
            assertThat(testDataSource).hasSelectCount(1);
        }

        @Test
        void useDerivedQuery_returningInterfaceProjection_withDefaultMethods() {
            List<PostCommentSummaryWithDefaultMethods> postCommentSummaries = postCommentRepository.findAllFetchingSummaryWithDefaultMethodsBy();
            assertThat(postCommentSummaries).hasSize(POST_COMMENT_COUNT);
            log.info("PostCommentSummaries: {}", top.toString(postCommentSummaries, top::toString));
            assertThat(testDataSource).hasSelectCount(1);
        }

        @Test
        void useDerivedQuery_returningInterfaceProjection_withSpEL() {
            List<PostCommentSummaryWithSpEL> postCommentSummaries = postCommentRepository.findAllFetchingSummaryWithSpELNPlusOneBy();
            assertThat(postCommentSummaries).hasSize(POST_COMMENT_COUNT);
            // This triggers the N+1 issue!
            log.info("PostCommentSummaries: {}", top.toString(postCommentSummaries, top::toString));
            assertThat(testDataSource).hasSelectCount(POST_COUNT + 1);
        }

        @Test
        void useDeclaredJpqlQuery_returningClassProjection() {
            List<PostCommentDto> postComments = postCommentRepository.findAllWithJpql();
            assertThat(postComments).hasSize(POST_COMMENT_COUNT);
            log.info("PostCommentDtos: {}", postComments);
            assertThat(testDataSource).hasSelectCount(1);
        }

        @Test
        void useDeclaredNativeQuery_returningInterfaceProjection() {
            List<PostCommentSimpleSummary> postComments = postCommentRepository.findAllFetchingReviewOnlyWithNativeQueryBy();
            assertThat(postComments).hasSize(POST_COMMENT_COUNT);
            log.info("PostCommentSummaries: {}", top.toString(postComments, top::toString));
            assertThat(testDataSource).hasSelectCount(1);
        }
    }

    @Nested
    class SpringDataPost {
        private final ProjectionTest top = ProjectionTest.this;

        @Test
        void useDerivedQuery_returningInterfaceProjection() {
            List<PostSummary> posts = postRepository.findAllFetchingSummaryBy();
            assertThat(posts).hasSize(POST_COUNT);
            log.info("PostSummaries: {}", top.toString(posts, top::toString));
            assertThat(testDataSource).hasSelectCount(1);
        }

        @Test
        void useDerivedQuery_returningInterfaceProjectionWithNestedAssociation() {
            List<PostExtendedSummary> posts = postRepository.findAllFetchingExtendedSummaryNPlusOneBy();
            assertThat(posts).hasSize(POST_COUNT);
            // This triggers the N+1 problem!
            log.info("PostExtendedSummaries: {}", top.toString(posts, top::toString));
            assertThat(testDataSource).hasSelectCount(POST_COUNT + 1);
        }

        @Test
        void useEntityGraph_returningInterfaceProjectionWithNestedAssociation() {
            List<PostExtendedSummary> posts = postRepository.findAllFetchingExtendedSummaryBy();
            assertThat(posts).hasSize(POST_COUNT);
            log.info("PostExtendedSummaries: {}", top.toString(posts, top::toString));
            assertThat(testDataSource).hasSelectCount(1);
        }
    }

    private <T> String toString(List<T> elements, Function<T, String> elementToString) {
        var builder = new StringBuilder("[");
        for (var element : elements) {
            builder.append(elementToString.apply(element));
        }
        builder.append("]");
        return builder.toString();
    }

    private String toString(Tuple tuple) {
        return String.format("{review=%s, postTitle=%s}", tuple.get("review"), tuple.get("postTitle"));
    }

    private String toString(PostCommentShortSummary commentSummary) {
        return String.format("{review=%s}", commentSummary.getReview());
    }

    private String toString(PostCommentSummary commentSummary) {
        return String.format("{review=%s, postTitle=%s}", commentSummary.getReview(), commentSummary.getPost().getTitle());
    }

    private String toString(PostCommentSummaryWithDefaultMethods commentSummary) {
        return String.format("{review=%s, postTitle=%s}", commentSummary.getReview(), commentSummary.getPostTitle());
    }

    private String toString(PostCommentSummaryWithSpEL commentSummary) {
        return String.format("{review=%s, postTitle=%s}", commentSummary.getReview(), commentSummary.getPostTitle());
    }

    private String toString(PostCommentSimpleSummary commentSummary) {
        return String.format("{review=%s, postTitle=%s}", commentSummary.getReview(), commentSummary.getPostTitle());
    }

    private String toString(PostSummary post) {
        return String.format("{title=%s}", post.getTitle());
    }

    private String toString(PostExtendedSummary post) {
        return String.format("{title=%s, comment count=%s}", post.getTitle(), post.getComments().size());
    }
}

interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    /**
     * When using a derived query, our class projection properties must match the column names
     *
     * @return class DTO projection, single c'tor
     */
    List<PostCommentReviewDto> findAllFetchingReviewOnlyBy();

    /**
     * The interface projection returned here includes only properties of the main entity with no nested
     * associations. This works perfectly - generating a query that includes only the properties included in
     * the interface.
     * @return an interface projections
     */
    List<PostCommentShortSummary> findAllFetchingShortSummaryBy();

    /**
     * The interface projection returned here includes a nested association as an interface projection.
     * This causes Spring to fetch all of the properties of the nested Entity, regardless of what is actually included
     * in the interface projection.
     * nested association
     * @return an interface projections with a nested association
     */
    List<PostCommentSummary> findAllFetchingSummaryBy();

    List<PostCommentSummaryWithDefaultMethods> findAllFetchingSummaryWithDefaultMethodsBy();

    /**
     * Here we return a type that uses SpEL - this causes the N+1 issue when the property is referenced.
     * It also causes Spring to fetch all properties of the Entity, regardless of what is actually included
     * in the interface projection.
     * @return interface projection with SpEL property
     */
    List<PostCommentSummaryWithSpEL> findAllFetchingSummaryWithSpELNPlusOneBy();

    /**
     * This is one of the best ways to return a projection: works exactly like with the EntityManager::createQuery
     *
     * @return class DTO projection
     */
    @Query("select new PostCommentDto(p.title, pc.review) " +
            "from PostComment pc join pc.post p")
    List<PostCommentDto> findAllWithJpql();

    /**
     * Declared native queries cannot return class projections. We must use interface projections instead.
     *
     * @return interface projection
     */
    @Query(value = "SELECT p.title AS postTitle, pc.review " +
            "FROM post_comment pc INNER JOIN post p ON pc.post_id = p.id", nativeQuery = true)
    List<PostCommentSimpleSummary> findAllFetchingReviewOnlyWithNativeQueryBy();

}

interface PostRepository extends JpaRepository<Post, Long> {

    List<PostSummary> findAllFetchingSummaryBy();

    List<PostExtendedSummary> findAllFetchingExtendedSummaryNPlusOneBy();

    @EntityGraph(attributePaths = "comments")
    List<PostExtendedSummary> findAllFetchingExtendedSummaryBy();

}

@lombok.Value
class PostCommentReviewDto {
    String review;
}

interface PostCommentShortSummary {
    String getReview();
}

interface PostCommentSummary {

    String getReview();

    PostSummary getPost();

    interface PostSummary {
        String getTitle();
    }
}

interface PostCommentSummaryWithDefaultMethods {

    String getReview();

    PostSummary getPost();

    default String getPostTitle() {
        return getPost().getTitle();
    }

    interface PostSummary {
        String getTitle();
    }
}

interface PostCommentSummaryWithSpEL {

    String getReview();

    @Value("#{target.post.title }")
    String getPostTitle();
}

interface PostCommentSimpleSummary {

    String getReview();

    String getPostTitle();
}

interface PostSummary {
    String getTitle();
}

interface PostExtendedSummary {
    String getTitle();

    List<PostCommentSummary> getComments();

    interface PostCommentSummary {
        String getReview();
    }
}

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

    @NotNull
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
