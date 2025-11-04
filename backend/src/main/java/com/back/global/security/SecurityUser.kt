package com.back.global.security

import lombok.Getter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.oauth2.core.user.OAuth2User
import java.util.Map

class SecurityUser(
    val id: Long,
    username: String,
    password: String,
    val nickname: String,
    authorities: Collection<GrantedAuthority>
) : User(username, password, authorities), OAuth2User {
    override fun getAttributes(): MutableMap<String, Any> = mutableMapOf()

    override fun getName(): String = nickname
}
