package com.att.training.spring.boot.demo.nplusone;

import com.att.training.spring.boot.demo.tc.MySqlSingletonContainer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.ttddyy.dsproxy.QueryCount;
import net.ttddyy.dsproxy.QueryCountHolder;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@Slf4j
@TestInstance(PER_CLASS)
@Transactional
class DbTest extends MySqlSingletonContainer {

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private TransactionTemplate transactionTemplate;

    @BeforeAll
    void init() {
        transactionTemplate.execute(status -> {
            for (int i = 0; i < 4; ++i) {
                Post post = new Post("post#" + i, PostStatus.APPROVED)
                        .addComment(new PostComment("Great post!"));
                entityManager.persist(post);
            }
            return null;
        });
    }

    @Nested
    class PostFetcher {

        @Autowired
        private PostRepository postRepository;

        @Test
        void findUsingEntityManager() {
            Post fetchedPost = entityManager.find(Post.class, 1L);
            logPost(fetchedPost);
        }

        @SuppressWarnings({"deprecation", "unchecked"})
        @Test
        void useProjection() {
            List<PostDto> fetchedPosts = entityManager.createNativeQuery("SELECT p.title, pc.review FROM post p INNER JOIN post_comment pc ON p.id = pc.post_id")
                    .unwrap(NativeQuery.class)
                    .setResultTransformer(Transformers.aliasToBean(PostDto.class))
                    .getResultList();
            fetchedPosts.forEach(this::logPost);
        }

        @Test
        void findUsingRepoDefault() {
            QueryCountHolder.clear();
            Post post = postRepository.findById(1L).orElseThrow();
            logPost(post);
            QueryCount queryCount = QueryCountHolder.getGrandTotal();
            long recordedSelectCount = queryCount.getSelect();
            assertThat(recordedSelectCount).isEqualTo(2);
        }

        @Test
        void findUsingRepoWithCustomQuery() {
            Post post = postRepository.findByIdWithExplicitJoinFetch(1L);
            logPost(post);
        }

        @Test
        void findUsingRepoWithGraph() {
            Post post = postRepository.getById(1L);
            logPost(post);
        }

        private void logPost(Post fetchedPost) {
            List<PostComment> comments = fetchedPost.getComments();
            log.info("Post: {} has {} comments", fetchedPost.getId(), comments.size());
            log.info("Post comments: {}", comments);
        }

        private void logPost(PostDto fetchedPost) {
            log.info("Post: {}, review: {}", fetchedPost.getTitle(), fetchedPost.getReview());
        }
    }

    @Nested
    class PostCommentFetcher {

        @Autowired
        private PostCommentRepository postCommentRepository;

        @Test
        void findUsingEntityManager() {
            PostComment fetchedPost = entityManager.find(PostComment.class, 1L);
            logPostComment(fetchedPost);
        }


        @Test
        void findAllUsingRepoDefault() {
            List<PostComment> postComments = postCommentRepository.findAll();
            for (var comment : postComments) {
                logPostComment(comment);
            }
        }

        @Test
        void findUsingRepoWithCustomQuery() {
            PostComment postComment = postCommentRepository.findByIdWithExplicitJoinFetch(1L);
            logPostComment(postComment);
        }

        @Test
        void findUsingRepoWithGraph() {
            PostComment postComment = postCommentRepository.getById(1L);
            logPostComment(postComment);
        }

        private void logPostComment(PostComment postComment) {
            log.info("PostComment: {}", postComment);
            Post post = postComment.getPost();
            log.info("Post: {} has status {}", post.getTitle(), post.getStatus());
        }
    }

    @Getter
    @Setter
    @ToString
    public static class PostDto {
        private String title;
        private String review;
    }
}

interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p JOIN FETCH p.comments WHERE p.id = :id")
    Post findByIdWithExplicitJoinFetch(Long id);

    @EntityGraph(attributePaths = "comments")
    Post getById(Long id);

}

interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    @Query("SELECT pc FROM PostComment pc JOIN FETCH pc.post WHERE pc.id = :id")
    PostComment findByIdWithExplicitJoinFetch(Long id);

    @EntityGraph(attributePaths = "post")
    PostComment getById(Long id);

}

@Entity
@Table
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
@Table
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

//@Entity
//@Table
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
class PostDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date createdOn;
    private String createdBy;
    @OneToOne
    @JoinColumn(name = "post_id", unique = true)
    private Post post;
}