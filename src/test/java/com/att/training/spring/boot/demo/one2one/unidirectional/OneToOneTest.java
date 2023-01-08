package com.att.training.spring.boot.demo.one2one.unidirectional;

import com.att.training.spring.boot.demo.tc.MySqlSingletonContainer;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static net.ttddyy.dsproxy.asserts.assertj.DataSourceAssertAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class OneToOneTest extends MySqlSingletonContainer {
    private static final int POST_COUNT = 4;

    @BeforeAll
    void init() {
        transactionTemplate.executeWithoutResult(status -> {
            for (int i = 0; i < POST_COUNT; ++i) {
                int index = i + 1;
                var post = new Post("post#" + index);
                entityManager.persist(post);
                var postDetails = new PostDetails("creator #" + index)
                        .setPost(post);
                entityManager.persist(postDetails);
            }
        });
    }

    @Override
    protected List<String> tablesToDrop() {
        return List.of("post_details", "post");
    }

    @Nested
    class PostFetcher {

        @Autowired
        private PostDetailsRepository postDetailsRepository;

        @Test
        void findPostUsingEntityManager() {
            Post fetchedPost = entityManager.find(Post.class, 1L);
            assertThat(fetchedPost).isNotNull();
            assertThat(testDataSource).hasSelectCount(1);
        }

        @Test
        void findPostDetailsUsingEntityManager() {
            PostDetails postDetails = entityManager.find(PostDetails.class, 1L);
            assertThat(postDetails).isNotNull();
            assertThat(testDataSource).hasSelectCount(1);
        }


        @Test
        void findPostDetailsWithPostUsingJpql() {
            PostDetails postDetails = postDetailsRepository.findByIdUsingJpql(1L);
            logPostDetails(postDetails);
            assertThat(testDataSource).hasSelectCount(1);
        }

        private void logPostDetails(PostDetails postDetails) {
            log.info("PostComment: id={}, createdBy={}, post title={}",
                    postDetails.getId(), postDetails.getCreatedBy(), postDetails.getPost().getTitle());
        }
    }
}

interface PostDetailsRepository extends JpaRepository<PostDetails, Long> {

    @Transactional(readOnly = true)
    @Query("select pd from PostDetails pd join fetch pd.post where pd.id = :id")
    PostDetails findByIdUsingJpql(long id);
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

    public Post(String title) {
        this.title = title;
    }
}

@Entity
@Getter
@Setter
@NoArgsConstructor
class PostDetails {

    @Id // not @Generated!
    private Long id;
    private LocalDate createdOn;
    private String createdBy;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "id") // Without this the column name will be 'post_id'
    private Post post;

    public PostDetails(String createdBy) {
        this.createdBy = createdBy;
        this.createdOn = LocalDate.now();
    }

    public PostDetails setPost(Post post) {
        this.post = post;
        return this;
    }
}
