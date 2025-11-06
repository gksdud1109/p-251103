package com.back.domain.post.comment.entity

import com.back.domain.member.member.entity.Member
import com.back.domain.post.post.entity.Post
import com.back.global.exception.ServiceException
import com.back.global.jpa.entity.BaseEntity
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne
import lombok.AllArgsConstructor
import lombok.NoArgsConstructor

@Entity
class Comment(
    @field:ManyToOne(fetch = FetchType.LAZY)
    var author: Member,

    @JvmField
    var content: String,

    @field:JsonIgnore
    @field:ManyToOne(fetch = FetchType.LAZY)
    var post: Post
) : BaseEntity() {
    fun update(content: String) {
        this.content = content
    }

    fun checkActorModify(actor: Member?) {
        if (author != actor) throw ServiceException("403-1", "댓글 수정 권한이 없습니다.")
    }

    fun checkActorDelete(actor: Member?) {
        if (author != actor) throw ServiceException("403-2", "댓글 삭제 권한이 없습니다.")
    }
}
