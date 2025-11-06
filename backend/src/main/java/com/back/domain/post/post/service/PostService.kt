package com.back.domain.post.post.service

import com.back.domain.member.member.entity.Member
import com.back.domain.post.comment.entity.Comment
import com.back.domain.post.post.entity.Post
import com.back.domain.post.post.repository.PostRepository
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service
import java.util.*

@Service
class PostService (
    private val postRepository: PostRepository
) {

    fun write(author: Member, title: String, content: String): Post {
        val post = Post(author, title, content)

        return postRepository.save(post)
    }

    fun count(): Long {
        return postRepository.count()
    }

    // TODO: Optional 제거
    fun findById(id: Long): Optional<Post> {
        return postRepository.findById(id)
    }

    fun findAll(): List<Post> {
        return postRepository.findAll()
    }

    fun modify(post: Post, title: String, content: String) {
        post.update(title, content)
    }

    fun writeComment(author: Member, post: Post, content: String): Comment {
        return post.addComment(author, content)
    }

    fun deleteComment(post: Post, commentId: Long) {
        post.deleteComment(commentId)
    }

    fun modifyComment(post: Post, commentId: Long, content: String) {
        post.updateComment(commentId, content)
    }

    fun delete(post: Post) {
        postRepository.delete(post)
    }

    fun flush() {
        postRepository.flush()
    }
}