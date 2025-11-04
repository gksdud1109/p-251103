package com.back.global.security

import com.back.domain.member.member.service.MemberService
import com.back.global.rq.Rq
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import lombok.RequiredArgsConstructor
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*

@Component
class CustomOAuth2LoginSuccessHandler(
    private val memberService: MemberService,
    private val rq: Rq
) : AuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse?,
        authentication: Authentication?
    ) {
        val member = rq.actor ?: throw IllegalArgumentException("로그인된 회원이 없습니다.")
        val accessToken = memberService.genAccessToken(member)
        val apiKey = member.apiKey

        rq.setCookie("accessToken", accessToken)
        rq.setCookie("apiKey", apiKey)

        val redirectUrl = extractRedirectUrl(request.getParameter("state")) ?: "/"
        rq.sendRedirect(redirectUrl)
    }

    private fun extractRedirectUrl(state: String?): String? {
        if (state.isNullOrBlank()) return null
        val decoded = runCatching {
            String(Base64.getUrlDecoder().decode(state), Charsets.UTF_8)
        }.getOrNull() ?: return null

        // "#" 기준으로 안전하게 분리
        val parts = decoded.split("#")
        return parts.getOrNull(1)
    }
}
