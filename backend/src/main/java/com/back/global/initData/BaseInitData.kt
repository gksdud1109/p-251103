package com.back.global.initData

import com.back.domain.member.member.service.MemberService
import com.back.domain.post.post.service.PostService
import lombok.RequiredArgsConstructor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.transaction.annotation.Transactional

@Configuration
class BaseInitData(
    @Autowired
    @Lazy
    private val self: BaseInitData,
    private val postService: PostService,
    private val memberService: MemberService
) {
    @Bean
    fun initDataRunner(): ApplicationRunner = ApplicationRunner {
        self.work1()
        self.work2()
    }

    @Transactional
    fun work1() {
        if (memberService.count() > 0) {
            return
        }

        listOf(
            Triple("system", "system", "시스템"),
            Triple("admin", "admin", "운영자"),
            Triple("user1", "1234", "유저1"),
            Triple("user2", "1234", "유저2"),
            Triple("user3", "1234", "유저3"),
        ).forEach { (username, password, nickname) ->
            memberService.join(username, password, nickname).apply {
                updateApiKey(username)
            }
        }
    }

    @Transactional
    fun work2() {
        if (postService.count() > 0) {
            return
        }

        val member1 = memberService.findByUsername("user1").get()
        val member2 = memberService.findByUsername("user2").get()
        val member3 = memberService.findByUsername("user3").get()

        val post1 = postService.write(member1, "제목1", "내용1")
        val post2 = postService.write(member1, "제목2", "내용2")
        val post3 = postService.write(member2, "제목3", "내용3")

        post1.addComment(member1, "댓글 1-1")
        post1.addComment(member1, "댓글 1-2")
        post1.addComment(member1, "댓글 1-3")
        post2.addComment(member2, "댓글 2-1")
        post2.addComment(member2, "댓글 2-2")
    }
}