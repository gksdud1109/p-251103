package com.back.global.globalExceptionHandler

import com.back.global.exception.ServiceException
import com.back.global.rsData.RsData
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import java.util.function.Function
import java.util.stream.Collectors

@ControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(NoSuchElementException::class)
    @ResponseBody
    fun handleException(e: NoSuchElementException?): RsData<Void?> =
        RsData("404-1","존재하지 않는 데이터입니다.")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseBody
    fun handleException(e: MethodArgumentNotValidException): RsData<Void?> {
        val message = e.bindingResult.allErrors
            .filterIsInstance<FieldError>()
            .sortedWith(compareBy({ it.field }, { it.code }))
            .joinToString("\n") { "${it.field}-${it.code}-${it.defaultMessage}"}

        return RsData("400-1", message)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    @ResponseBody
    fun handleException(e: HttpMessageNotReadableException?): RsData<Void?> =
        RsData("400-2", "잘못된 형식의 요청 데이터입니다.")

    @ExceptionHandler(ServiceException::class)
    @ResponseBody
    fun handleException(e: ServiceException): RsData<Void?> =
        RsData(e.resultCode, e.msg)
}
