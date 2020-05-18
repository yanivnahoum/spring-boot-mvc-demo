package com.att.training.spring.boot.demo.quote;

import com.att.training.spring.boot.demo.quote.api.QuoteResponse;
import com.att.training.spring.boot.demo.user.UserRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("This makes an actual http request")
@SpringBootTest
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
@MockBean(UserRepository.class)
class AnotherQuoteClientTest {

    @Autowired private QuoteClient quoteClient;

    @Test
    void test() {
        QuoteResponse quote = quoteClient.getQuoteOfTheDay("funny");
        assertThat(quote).isNotNull();
    }
}
