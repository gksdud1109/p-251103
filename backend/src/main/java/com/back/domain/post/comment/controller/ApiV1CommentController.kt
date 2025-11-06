package com.back.domain.post.comment.controller

import com.back.domain.member.member.service.MemberService
import com.back.domain.post.comment.dto.CommentDto
import com.back.domain.post.comment.entity.Comment
import com.back.domain.post.post.service.PostService
import com.back.global.rq.Rq
import com.back.global.rsData.RsData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import lombok.RequiredArgsConstructor
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
@Tag(name = "ApiV1CommentController", description = "댓글 API")
class ApiV1CommentController(
    private val postService: PostService,
    private val rq: Rq
) {

    @GetMapping
    @Operation(summary = "다건 조회")
    fun getItems(
        @PathVariable postId: Long
    ): List<CommentDto> {
        val post = postService.findById(postId).get()
        return post.getComments().reversed()
            .map { CommentDto(it) }
    }

    @GetMapping("/{commentId}")
    @Transactional(readOnly = true)
    @Operation(summary = "단건 조회")
    fun getItem(
        @PathVariable postId: Long,
        @PathVariable commentId: Long
    ): CommentDto {
        val post = postService.findById(postId).get()
        val comment = post.findCommentById(commentId).get()
        return CommentDto(comment)
    }

    @DeleteMapping("/{commentId}")
    @Transactional
    @Operation(summary = "댓글 삭제")
    fun deleteItem(
        @PathVariable postId: Long,
        @PathVariable commentId: Long
    ): RsData<Void> {
        val actor = rq.actor
        val post = postService.findById(postId).get()
        val comment = post.findCommentById(commentId).get()
        comment.checkActorDelete(actor)
        postService.deleteComment(post, commentId)

        return RsData(
            "200-1",
            "${commentId}번 댓글이 삭제되었습니다."
        )
    }


    data class CommentWriteReqBody(
        @field:NotBlank @field:Size(min = 2, max = 100) val content: String
    )

    data class CommentWriteResBody(
        val commentDto: CommentDto
    )

    @PostMapping
    @Transactional
    @Operation(summary = "댓글 작성")
    fun createItem(
        @PathVariable postId: Long,
        @RequestBody reqBody: @Valid CommentWriteReqBody
    ): RsData<CommentWriteResBody> {
        val actor = rq.actor
        val post = postService.findById(postId).get()
        val comment = postService.writeComment(
            actor, post,
            reqBody.content
        )

        postService.flush()

        return RsData(
            "201-1",
            "${comment.id}번 댓글이 생성되었습니다.",
            CommentWriteResBody(
                CommentDto(comment)
            )
        )
    }

    internal data class CommentModifyReqBody(
        @field:NotBlank @field:Size(min = 2, max = 100) val content: String
    )

    @PutMapping("/{commentId}")
    @Transactional
    @Operation(summary = "댓글 수정")
    fun modifyItem(
        @PathVariable postId: Long,
        @PathVariable commentId: Long,
        @RequestBody reqBody: @Valid CommentWriteReqBody
    ): RsData<Void> {
        val actor = rq.actor
        val post = postService.findById(postId).get()
        val comment = post.findCommentById(commentId).get()
        comment.checkActorModify(actor)
        postService.modifyComment(post, commentId, reqBody.content)

        return RsData(
            "200-1",
            ("${commentId}번 댓글이 수정되었습니다.")
        )
    }
}