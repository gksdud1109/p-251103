package com.back.standard.ut

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.nio.charset.StandardCharsets
import java.security.Key
import java.util.*

class Ut {
    object jwt {

        @JvmStatic
        fun toString(secret: String, expireSeconds: Long, body: Map<String, Any>): String {
            val issuedAt = Date()
            val expiration = Date(issuedAt.time + 1000L * expireSeconds)
            val secretKey: Key = Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8))

            return Jwts.builder()
                .claims(body)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(secretKey)
                .compact()
        }
        @JvmStatic
        fun isValid(jwt: String?, secretPattern: String): Boolean{
            if(jwt.isNullOrBlank()) return false

            val secretKey = Keys.hmacShaKeyFor(secretPattern.toByteArray(StandardCharsets.UTF_8))
            return runCatching {
                Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(jwt)
            }.isSuccess
        }
        @JvmStatic
        fun payloadOrNull(jwt: String?, secretPattern: String): Map<String, Any>?{
            if(jwt.isNullOrBlank() || !isValid(jwt,secretPattern)) return null

            val secretKey = Keys.hmacShaKeyFor(secretPattern.toByteArray(StandardCharsets.UTF_8))
            return runCatching {
                val parsed = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(jwt)

                parsed.payload as Map<String, Any>
            }.getOrNull()
        }
    }
}