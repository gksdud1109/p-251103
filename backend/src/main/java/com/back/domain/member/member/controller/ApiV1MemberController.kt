package com.back.domain.member.member.controller

import com.back.domain.member.member.dto.MemberDto
import com.back.domain.member.member.service.AuthTokenService
import com.back.domain.member.member.service.MemberService
import com.back.global.exception.ServiceException
import com.back.global.rq.Rq
import com.back.global.rsData.RsData
import com.back.standard.extentions.getOrThrow
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import lombok.RequiredArgsConstructor
import org.springframework.web.bind.annotation.*
import java.util.function.Supplier

@RestController
@RequestMapping("/api/v1/members")
class ApiV1MemberController(
    private val memberService: MemberService,
    private val rq: Rq
) {

    data class JoinReqBody(
        @field:NotBlank @field:Size(min = 2, max = 30) val username: String,
        @field:NotBlank @field:Size(min = 2, max = 30) val password: String,
        @field:NotBlank @field:Size(min = 2, max = 30) val nickname: String
    )

    data class JoinResBody(
        val memberDto: MemberDto
    )

    @PostMapping("/join")
    fun join(
        @RequestBody reqBody: @Valid JoinReqBody
    ): RsData<JoinResBody> {
        val member = memberService.join(
            reqBody.username,
            reqBody.password,
            reqBody.nickname
        )

        return RsData(
            "201-1",
            "회원가입이 완료되었습니다. ${reqBody.nickname}님 환영합니다.",
            JoinResBody(
                MemberDto(member)
            )
        )
    }


    data class LoginReqBody(
        @field:NotBlank @field:Size(min = 2, max = 30) val username: String,
        @field:NotBlank @field:Size(min = 2, max = 30) val password: String
    )

    data class LoginResBody(
        val memberDto: MemberDto,
        val apiKey: String,
        val accessToken: String
    )

    @PostMapping("/login")
    fun login(
        @RequestBody reqBody: @Valid LoginReqBody
    ): RsData<LoginResBody> {
        val member = memberService.findByUsername(reqBody.username)
            ?: throw ServiceException("401-1", "존재하지 않는 아이디입니다.")

        memberService.checkPassword(reqBody.password, member.password)
        val accessToken = memberService.genAccessToken(member)

        rq.setCookie("apiKey", member.apiKey)
        rq.setCookie("accessToken", accessToken)

        return RsData(
            "200-1",
            "${reqBody.username}님 환영합니다.",
            LoginResBody(
                MemberDto(member),
                member.apiKey,
                accessToken
            )
        )
    }

    @DeleteMapping("/logout")
    fun logout(): RsData<Void> {
        rq.apply {
            deleteCookie("apiKey")
            deleteCookie("accessToken")
        }

        return RsData(
            "200-1",
            "로그아웃 되었습니다."
        )
    }


    data class MeResBody(
        val memberDto: MemberDto
    )

    @GetMapping("/me")
    fun me(): RsData<MeResBody> {
        val author = memberService.findById(rq.actor.id).getOrThrow()

        return RsData(
            "200-1",
            "OK",
            MeResBody(
                MemberDto(author)
            )
        )
    }
}