package com.att.training.spring.boot.demo.utils;

import com.google.common.collect.Streams;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StreamWithIndexTest {

    @Test
    void mapWithIndex() {
        record Indexed<T>(T value, long index) {}
        var input = List.of("a", "b", "c", "d", "e");
        var indexed = Streams.mapWithIndex(input.stream(), Indexed::new)
                .toList();
        assertThat(indexed).containsExactly(
                new Indexed<>("a", 0),
                new Indexed<>("b", 1),
                new Indexed<>("c", 2),
                new Indexed<>("d", 3),
                new Indexed<>("e", 4)
        );
    }
}
