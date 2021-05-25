package com.att.training.spring.boot.demo.one2one.bidirectional;

import com.att.training.spring.boot.demo.tc.MySqlSingletonContainer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import java.time.LocalDate;
import java.util.List;

import static net.ttddyy.dsproxy.asserts.assertj.DataSourceAssertAssertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
class OneToOneTest extends MySqlSingletonContainer {
    private static final int POST_COUNT = 4;

    @BeforeAll
    void init() {
        transactionTemplate.executeWithoutResult(status -> {
            for (int i = 0; i < POST_COUNT; ++i) {
                int index = i + 1;
                Post post = new Post("post#" + index)
                        .addDetails(new PostDetails("creator #" + index));
                entityManager.persist(post);
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
        private PostRepository postRepository;

        @Test
        void findUsingEntityManager() {
            Post fetchedPost = entityManager.find(Post.class, 1L);
            assertThat(testDataSource).hasSelectCount(2);
        }
    }
}

interface PostRepository extends JpaRepository<Post, Long> {}

@Entity
@Getter
@Setter
@NoArgsConstructor
class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;

    @OneToOne(
            mappedBy = "post", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true,
            optional = false
    )
    private PostDetails details;

    public Post(String title) {
        this.title = title;
    }

    public Post addDetails(PostDetails details) {
        details.setPost(this);
        this.details = details;
        return this;
    }

    public void removeDetails() {
        if (details != null) {
            details.setPost(null);
            details = null;
        }
    }
}

@Entity
@Getter
@Setter
@NoArgsConstructor
class PostDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate createdOn;
    private String createdBy;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn
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
