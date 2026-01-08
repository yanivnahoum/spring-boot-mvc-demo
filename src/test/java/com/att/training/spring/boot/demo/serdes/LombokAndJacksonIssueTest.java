package com.att.training.spring.boot.demo.serdes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Demonstrates lombok/jackson <a href="https://github.com/projectlombok/lombok/issues/3978">issue</a>
 */
@JsonTest
class LombokAndJacksonIssueTest {
    @Autowired
    private ObjectMapper mapper;

    @Test
    void write() throws JsonProcessingException {
        String json = mapper.writeValueAsString(new Pojo(true, true));
        assertThat(json).isEqualTo("""
                {"isActive":true,"isEnabled":true,"enabled":true}\
                """);
    }

    @Test
    void read() throws JsonProcessingException {
        String json = """
                {"isActive":true,"isEnabled":true}\
                """;
        Pojo pojo = mapper.readValue(json, Pojo.class);
        assertThat(pojo).isEqualTo(new Pojo(true, true));
    }

    // This was de-lomboked from a class with @Data
    static class Pojo {
        @JsonProperty("isActive")
        private boolean active;
        @JsonProperty("isEnabled")
        private boolean isEnabled;

        public Pojo(boolean active, boolean isEnabled) {
            this.active = active;
            this.isEnabled = isEnabled;
        }

        public boolean isActive() {return this.active;}

        public boolean isEnabled() {return this.isEnabled;}

        //        @JsonProperty("isActive")
        public void setActive(boolean active) {this.active = active;}

        // Since lombok this is no longer copied by default from the field,
        // unless `lombok.copyJacksonAnnotationsToAccessors = true` is specified in lombok.config
        //        @JsonProperty("isEnabled")
        public void setEnabled(boolean isEnabled) {this.isEnabled = isEnabled;}

        public boolean equals(@org.springframework.lang.Nullable final Object o) {
            if (o == this) return true;
            if (!(o instanceof Pojo)) return false;
            final Pojo other = (Pojo) o;
            if (!other.canEqual((Object) this)) return false;
            if (this.isActive() != other.isActive()) return false;
            if (this.isEnabled() != other.isEnabled()) return false;
            return true;
        }

        protected boolean canEqual(@org.springframework.lang.Nullable final Object other) {return other instanceof Pojo;}

        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            result = result * PRIME + (this.isActive() ? 79 : 97);
            result = result * PRIME + (this.isEnabled() ? 79 : 97);
            return result;
        }

        @org.springframework.lang.NonNull
        public String toString() {return "AnotherLombokAndJacksonTest.Pojo(active=" + this.isActive() + ", isEnabled=" + this.isEnabled() + ")";}
    }
}
