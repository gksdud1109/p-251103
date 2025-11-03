package com.back.domain.home.home.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpSession
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors

@RestController
@Tag(name = "Home", description = "홈 컨트롤러")
class HomeController {
    @GetMapping(produces = [MediaType.TEXT_HTML_VALUE])
    @Operation(summary = "홈 화면", description = "홈 화면입니다.")
    fun home(): String {
        val localhost = InetAddress.getLocalHost()

        return """
                <h1>Welcome to Rest1</h1>
                <p>Server IP Address: ${localhost.hostAddress}</p>
                <p>Server Host Name: ${localhost.hostName}</p>
                <div>
                    <a href="swagger-ui/index.html">API 문서로 이동</a>
                </div>
                
                """.trimIndent()
    }

    @GetMapping("/session")
    @Operation(summary = "세션 확인용")
    fun session(session: HttpSession): Map<String, Any> {

        return session
            .attributeNames
            .toList()
            .associateWith {
                session.getAttribute(it)!!
            }
    }
}
