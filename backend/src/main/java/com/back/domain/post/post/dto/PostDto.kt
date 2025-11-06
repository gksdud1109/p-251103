package com.back.domain.post.post.dto

import com.back.domain.post.post.entity.Post
import org.springframework.http.RequestEntity.post
import java.time.LocalDateTime

data class PostDto private constructor(
    val id: Long?,
    val createDate: LocalDateTime?,
    val modifyDate: LocalDateTime?,
    val title: String?,
    val content: String?,
    val authorId: Long?,
    val authorName: String?
) {
    constructor(post: Post) : this(
        post.id,
        post.createDate,
        post.modifyDate,
        post.title,
        post.content,
        post.author.id,
        post.author.name
    )
}
