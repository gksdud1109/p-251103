package com.back.domain.member.member.entity

import com.back.global.jpa.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import lombok.NoArgsConstructor
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.*

@NoArgsConstructor
@Entity
class Member(
    id: Long = 0,
    @Column(unique = true)
    var username: String,
    var password: String,
    var nickname: String,
    @Column(unique = true)
    var apiKey: String,
    var profileImgUrl: String?
) : BaseEntity(id) {

    constructor(username: String, password: String, nickname: String, profileImgUrl: String?) : this(
        0,
        username,
        password,
        nickname,
        UUID.randomUUID().toString(),
        profileImgUrl
    )

    constructor(id: Long, username: String, nickname: String) : this(
        id,
        username,
        "",
        nickname,
        "",
        null
    )

    val name: String
        get() = this.nickname

    fun updateApiKey(apiKey: String) {
        this.apiKey = apiKey
    }

    val isAdmin: Boolean
        get() = "admin" == this.username

    val authorities: Collection<GrantedAuthority>
        get() {
            val authorities: MutableList<GrantedAuthority> = ArrayList()

            if (isAdmin) {
                authorities.add(SimpleGrantedAuthority("ROLE_ADMIN"))
            }

            return authorities
        }

    fun update(nickname: String, profileImgUrl: String?) {
        this.nickname = nickname
        this.profileImgUrl = profileImgUrl
    }
}