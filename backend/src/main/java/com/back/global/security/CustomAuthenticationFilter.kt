package com.back.global.security

import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.service.MemberService
import com.back.global.exception.ServiceException
import com.back.global.rq.Rq
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import lombok.RequiredArgsConstructor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import java.util.function.Supplier

@Component
class CustomAuthenticationFilter(
    private val memberService: MemberService,
    private val rq: Rq
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ){
        logger.debug("CustomAuthenticationFilter called")

        runCatching {
            authenticate(request, response, filterChain)
        }.onFailure { e ->
            when(e){
                is ServiceException -> {
                    val rs = e.rsData
                    response.contentType = "application/json"
                    response.status = rs.statusCode
                    response.writer.write(
                        """
                        {
                            "resultCode": "${rs.resultCode}",
                            "msg": "${rs.msg}"
                        }
                        """.trimIndent()
                    )
                }
                else -> throw e
            }
        }
    }

    private fun authenticate(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        val uri = request.requestURI

        // 인증이 필요 없는 URI는 필터 체인 그대로 진행
        if (!uri.startsWith("/api/") ||
            uri in listOf("/api/v1/members/join", "/api/v1/members/login")
        ) {
            chain.doFilter(request, response)
            return
        }

        // Authorization 헤더 또는 쿠키에서 토큰 추출
        val headerAuth = rq.getHeader("Authorization", "")
        val (apiKey, accessToken) = parseTokens(headerAuth)
            ?: Pair(rq.getCookieValue("apiKey", "").orEmpty(),
                rq.getCookieValue("accessToken", "").orEmpty())

        if (apiKey.isBlank() && accessToken.isBlank()) {
            chain.doFilter(request, response)
            return
        }

        val member = resolveMember(apiKey, accessToken)

        // 인증 정보(SecurityUser) 세팅
        val userDetails = SecurityUser(
            id = member.id,
            username = member.username,
            password = "",
            nickname = member.nickname,
            authorities = member.authorities
        )

        val authentication = UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.authorities
        )

        SecurityContextHolder.getContext().authentication = authentication
        chain.doFilter(request, response)
    }

    private fun parseTokens(header: String): Pair<String, String>? {
        if(header.isBlank()) return null
        if (!header.startsWith("Bearer ")) {
            throw ServiceException("401-2", "Authorization 헤더가 Bearer 형식이 아닙니다.")
        }

        val bits = header.split(" ", limit = 3)
        val apiKey = bits.getOrNull(1).orEmpty()
        val accessToken = bits.getOrNull(2).orEmpty()
        return apiKey to accessToken
    }

    private fun resolveMember(apiKey: String, accessToken: String): Member {
        var member: Member? = null
        var accessTokenValid = false

        if(accessToken.isNotBlank()){
            val payload = memberService.payload(accessToken)
            if(payload != null){
                val id = payload["id"] as Long
                val username =payload["username"] as String
                val nickname = payload["nickname"] as String
                member = Member(id, username, nickname)
                accessTokenValid = true
            }
        }

        if(member == null){
            member = memberService.findByApiKey(apiKey)
                ?: throw ServiceException("401-3", "API 키가 유효하지 않습니다.")
        }

        if (accessToken.isNotBlank() && !accessTokenValid) {
            val newToken = memberService.genAccessToken(member!!)
            rq.setCookie("accessToken", newToken)
            rq.setHeader("accessToken", newToken)
        }

        return requireNotNull(member) {
            "resolveMember()에서 member가 null입니다. API key나 accessToken을 확인하세요."
        }
    }
}
