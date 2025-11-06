package com.back.domain.member.member.service

import com.back.domain.member.member.entity.Member
import com.back.standard.ut.Ut
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class AuthTokenService {
    @Value("\${custom.jwt.secretPattern}")
    private lateinit var secretPattern: String

    @Value("\${custom.jwt.expireSeconds}")
    private val expireSeconds: Long = 0

    fun genAccessToken(member: Member): String {
        return Ut.jwt.toString(
            secretPattern,
            expireSeconds,
            mapOf(
                "id" to member.id,
                "username" to member.username,
                "nickname" to member.nickname
            )
        )
    }

    fun payloadOrNull(jwt: String): Map<String, Any>? {
        val payload = Ut.jwt.payloadOrNull(jwt, secretPattern) ?: return null

        return mapOf(
            "id" to (payload["id"] as Number).toLong(),
            "username" to payload["username"] as String,
            "nickname" to payload["nickname"] as String
        )
    }
}