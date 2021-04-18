package com.att.training.spring.boot.demo.projections;

import lombok.Value;

@Value
public class PostCommentDto {
    String postTitle;
    String review;
}
