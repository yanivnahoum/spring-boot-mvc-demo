package com.att.training.spring.boot.demo.nplusone;

import com.att.training.spring.boot.demo.tc.MySqlSingletonContainer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import static net.ttddyy.dsproxy.asserts.assertj.DataSourceAssertAssertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@Slf4j
@TestInstance(PER_CLASS)
@Transactional
class NPlusOneTest extends MySqlSingletonContainer {

    private static final int POST_COUNT = 4;

    @BeforeAll
    void init() {
        transactionTemplate.executeWithoutResult(status -> {
            for (int i = 0; i < POST_COUNT; ++i) {
                Post post = new Post("post#" + (i + 1), PostStatus.APPROVED)
                        .addComment(new PostComment(String.format("[comment #1] Post #%s rocks!", i + 1)))
                        .addComment(new PostComment(String.format("[comment #2] Post #%s rocks!", i + 1)));
                entityManager.persist(post);
            }
        });
    }

    @Override
    protected List<String> tablesToDrop() {
        return List.of("post_comment", "post");
    }

    @Nested
    class PostCommentFetcher {

        @Autowired
        private PostCommentRepository postCommentRepository;

        @Disabled("Use the default FetchType.Eager of the PostComment.post's @ManyToOne to see the N + 1 in action")
        @Test
        void findAllUsingRepoDefaultWithEager() {
            postCommentRepository.findAll();
            assertThat(testDataSource).hasSelectCount(POST_COUNT + 1);
        }

        @Disabled("Use the default FetchType.Eager of the PostComment.post's @ManyToOne to see the N + 1 in action")
        @Test
        void findUsingRepoWithCustomQueryWithoutJoinFetch() {
            postCommentRepository.findByIdWithoutJoinFetch(1L);
            assertThat(testDataSource).hasSelectCount(2);
        }

        @Test
        void findUsingEntityManager() {
            PostComment fetchedPost = entityManager.find(PostComment.class, 1L);
            logPostComment(fetchedPost);
            assertThat(testDataSource).hasSelectCount(2);
        }

        @Test
        void findUsingRepoDefault() {
            PostComment fetchedPost = postCommentRepository.findById(1L).orElseThrow();
            logPostComment(fetchedPost);
            assertThat(testDataSource).hasSelectCount(2);
        }

        @Test
        void findUsingRepoWithCustomQuery() {
            PostComment postComment = postCommentRepository.findByIdWithExplicitJoinFetch(1L);
            logPostComment(postComment);
            assertThat(testDataSource).hasSelectCount(1);
        }

        @Test
        void findUsingRepoWithCustomNativeQuery() {
            PostComment postComment = postCommentRepository.findByIdWithNativeJoin(1L);
            logPostComment(postComment);
            assertThat(testDataSource).hasSelectCount(2);
        }

        @Test
        void findUsingRepoWithGraph() {
            PostComment postComment = postCommentRepository.readById(1L);
            logPostComment(postComment);
            assertThat(testDataSource).hasSelectCount(1);
        }

        private void logPostComment(PostComment postComment) {
            log.info("PostComment: {}", postComment);
            Post post = postComment.getPost();
            log.info("Post: {} has status {}", post.getTitle(), post.getStatus());
        }
    }

    @Nested
    class PostFetcher {

        @Autowired
        private PostRepository postRepository;

        @Test
        void findUsingEntityManager() {
            Post fetchedPost = entityManager.find(Post.class, 1L);
            logPost(fetchedPost);
            assertThat(testDataSource).hasSelectCount(2);
        }

        @Test
        void findUsingRepoDefault() {
            Post post = postRepository.findById(1L).orElseThrow();
            logPost(post);
            assertThat(testDataSource).hasSelectCount(2);
        }

        @Test
        void findUsingRepoWithCustomQuery() {
            Post post = postRepository.findByIdWithExplicitJoinFetch(1L);
            logPost(post);
            assertThat(testDataSource).hasSelectCount(1);
        }

        @Test
        void findUsingRepoWithGraph() {
            Post post = postRepository.readById(1L);
            logPost(post);
            assertThat(testDataSource).hasSelectCount(1);
        }

        @Test
        void findUsingRepoWithGraph2() {
            Post post = postRepository.queryById(1L);
            logPost(post);
            assertThat(testDataSource).hasSelectCount(1);
        }

        @Test
        void findUsingRepoWithGraph3() {
            Post post = postRepository.findWithCommentsById(1L);
            logPost(post);
            assertThat(testDataSource).hasSelectCount(1);
        }

        @Test
        void findAllUsingRepoWithGraph() {
            List<Post> posts = postRepository.readAllWithCommentsBy();
            posts.forEach(this::logPost);
            assertThat(testDataSource).hasSelectCount(1);
        }

        @Disabled("Change the fetch to FetchType.Eager of the Post.comments' @OneToMany to see the N + 1 in action")
        @Test
        void findAllUsingRepoDefaultWithEager() {
            List<Post> posts = postRepository.findAll();
            assertThat(testDataSource).hasSelectCount(POST_COUNT + 1);
        }

        @Test
        void findAllUsingRepoDefault() {
            List<Post> posts = postRepository.findAll();
            for (Post post : posts) {
                logPost(post);
            }
            assertThat(testDataSource).hasSelectCount(POST_COUNT + 1);
        }

        private void logPost(Post fetchedPost) {
            List<PostComment> comments = fetchedPost.getComments();
            log.info("Post: {} has {} comments", fetchedPost.getId(), comments.size());
            log.info("Post comments: {}", comments);
        }
    }
}

interface PostRepository extends JpaRepository<Post, Long> {

    @Query("select p from Post p left join fetch p.comments where p.id = :id")
    Post findByIdWithExplicitJoinFetch(Long id);

    // EntityGraph generates a LEFT JOIN
    @EntityGraph(attributePaths = "comments")
    Post findWithCommentsById(Long id);

    @EntityGraph(attributePaths = "comments")
    Post readById(Long id);

    @EntityGraph(attributePaths = "comments")
    Post queryById(Long id);

    @EntityGraph(attributePaths = "comments")
    Post searchById(Long id);


    @EntityGraph(attributePaths = "comments")
    List<Post> findAllWithCommentsBy();

    @EntityGraph(attributePaths = "comments")
    List<Post> readAllWithCommentsBy();

    @EntityGraph(attributePaths = "comments")
    List<Post> queryAllBy();

    @EntityGraph(attributePaths = "comments")
    List<Post> getAllWithXxxBy();

    @EntityGraph(attributePaths = "comments")
    List<Post> searchAllWithYyyBy();
}

interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    @Query("select pc from PostComment pc where pc.id = :id")
    PostComment findByIdWithoutJoinFetch(Long id);

    @Query("select pc from PostComment pc join fetch pc.post where pc.id = :id")
    PostComment findByIdWithExplicitJoinFetch(Long id);

    @Query(value = "SELECT * FROM post_comment pc INNER JOIN post p on pc.post_id = p.id WHERE pc.id = :id", nativeQuery = true)
    PostComment findByIdWithNativeJoin(Long id);

    @EntityGraph(attributePaths = "post")
    PostComment readById(Long id);
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

    @ToString.Exclude
    // Marks the column as NOT NULL if used to create the schema,
    // but also makes the em.find(id) generate inner join (as opposed to left join) when the association is eager
    @NotNull
    @ManyToOne(
            fetch = FetchType.LAZY
//            , optional = false // Has the same effect as @NotNull
    )
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
