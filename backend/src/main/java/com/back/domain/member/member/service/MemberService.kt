package com.back.domain.member.member.service

import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.repository.MemberRepository
import com.back.global.exception.ServiceException
import com.back.standard.extentions.getOrThrow
import lombok.RequiredArgsConstructor
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*
import java.util.function.Consumer

@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val authTokenService: AuthTokenService,
    private val passwordEncoder: PasswordEncoder
) {

    fun count(): Long {
        return memberRepository.count()
    }

    fun join(username: String, password: String, nickname: String): Member {
        return join(username, password, nickname, null)
    }

    fun join(username: String, password: String, nickname: String, profileImgUrl: String?): Member {
        memberRepository.findByUsername(username)?.let {
            throw ServiceException("409-1", "이미 사용중인 아이디입니다.")
        }

        val member = Member(username, passwordEncoder.encode(password), nickname, profileImgUrl)
        return memberRepository.save(member)
    }


    fun modifyOrJoin(username: String, password: String, nickname: String, profileImgUrl: String?): Member {
        return memberRepository.findByUsername(username)
            ?.apply { update(nickname, profileImgUrl) }
            ?: join(username, password, nickname, profileImgUrl)
    }

    fun findByUsername(username: String): Member? {
        return memberRepository.findByUsername(username)
    }

    fun findByApiKey(apiKey: String): Member?{
        return memberRepository.findByApiKey(apiKey)
    }

    fun genAccessToken(member: Member): String {
        return authTokenService.genAccessToken(member)
    }

    fun payload(accessToken: String): Map<String, Any>? {
        return authTokenService.payloadOrNull(accessToken)
    }

    fun findById(id: Long): Member? {
        return memberRepository.findByIdOrNull(id)
    }

    fun findAll(): List<Member> {
        return memberRepository.findAll()
    }

    fun checkPassword(inputPassword: String, rawPassword: String) {
        if (!passwordEncoder.matches(inputPassword, rawPassword)) {
            throw ServiceException("401-2", "비밀번호가 일치하지 않습니다.")
        }
    }
}