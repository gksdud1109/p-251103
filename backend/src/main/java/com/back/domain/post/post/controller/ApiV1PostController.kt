package com.back.domain.post.post.controller

import com.back.domain.member.member.service.MemberService
import com.back.domain.post.post.dto.PostDto
import com.back.domain.post.post.entity.Post
import com.back.domain.post.post.service.PostService
import com.back.global.rq.Rq
import com.back.global.rsData.RsData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import lombok.RequiredArgsConstructor
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/posts")
@Tag(name = "ApiV1PostController", description = "글 API")
@SecurityRequirement(name = "bearerAuth")
class ApiV1PostController(
    private val postService: PostService,
    private val rq: Rq
) {

    @Operation(summary = "글 다건 조회")
    @GetMapping
    @Transactional(readOnly = true)
    fun getItems(): List<PostDto> {
        return postService.findAll().reversed()
            .map { PostDto(it) }
    }


    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    @Operation(summary = "글 단건 조회")
    fun getItem(
        @PathVariable id: Long
    ): PostDto {
        val post = postService.findById(id).get()
        return PostDto(post)
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "글 삭제")
    fun deleteItem(
        @PathVariable id: Long
    ): RsData<Void> {
        val actor = rq.actor
        val post = postService.findById(id).get()

        post.checkActorDelete(actor)
        postService.delete(post)

        return RsData(
            "200-1",
            "${id}번 게시물이 삭제되었습니다."
        )
    }


    data class PostWriteReqBody(
        @field:NotBlank @field:Size(min = 2, max = 10) val title: String,
        @field:NotBlank @field:Size(min = 2, max = 100) val content: String
    )

    data class PostWriteResBody(
        val postDto: PostDto
    )

    @PostMapping
    @Transactional
    @Operation(summary = "글 작성")
    fun createItem(
        @RequestBody @Valid reqBody: PostWriteReqBody
    ): RsData<PostWriteResBody> {
        val actor = rq.actor
        val post = postService.write(actor, reqBody.title, reqBody.content)

        return RsData(
            "201-1",
            "${post.id}번 게시물이 생성되었습니다.",
            PostWriteResBody(
                PostDto(post)
            )
        )
    }


    data class PostModifyReqBody(
        @field:NotBlank @field:Size(min = 2, max = 10) val title: String,
        @field:NotBlank @field:Size(min = 2, max = 100) val content: String
    )

    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "글 수정")
    fun modifyItem(
        @PathVariable id: Long,
        @RequestBody @Valid reqBody: PostModifyReqBody
    ): RsData<Void> {
        val actor = rq.actor

        val post = postService.findById(id).get()
        post.checkActorModify(actor)
        postService.modify(post, reqBody.title, reqBody.content)

        return RsData(
            "200-1",
            "${id}번 게시물이 수정되었습니다."
        )
    }
}