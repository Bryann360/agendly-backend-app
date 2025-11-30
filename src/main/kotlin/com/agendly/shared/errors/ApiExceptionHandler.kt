package com.agendly.shared.errors

import com.agendly.shared.errors.exceptions.*
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException::class)
  fun handleValidation(ex: MethodArgumentNotValidException, req: HttpServletRequest): ResponseEntity<ApiError> {
    val fieldErrors = ex.bindingResult.allErrors
      .filterIsInstance<FieldError>()
      .associate { it.field to (it.defaultMessage ?: "invalid") }

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
      ApiError(code = "validation_error", message = "Validation failed", details = mapOf("fields" to fieldErrors))
    )
  }

  @ExceptionHandler(NotFoundException::class)
  fun handleNotFound(ex: NotFoundException): ResponseEntity<ApiError> =
    ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError("not_found", ex.message ?: "Not found"))

  @ExceptionHandler(ConflictException::class)
  fun handleConflict(ex: ConflictException): ResponseEntity<ApiError> =
    ResponseEntity.status(HttpStatus.CONFLICT).body(ApiError("conflict", ex.message ?: "Conflict"))

  @ExceptionHandler(UnauthorizedException::class)
  fun handleUnauthorized(ex: UnauthorizedException): ResponseEntity<ApiError> =
    ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiError("unauthorized", ex.message ?: "Unauthorized"))

  @ExceptionHandler(ForbiddenException::class)
  fun handleForbidden(ex: ForbiddenException): ResponseEntity<ApiError> =
    ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiError("forbidden", ex.message ?: "Forbidden"))

  @ExceptionHandler(BadRequestException::class)
  fun handleBadRequest(ex: BadRequestException): ResponseEntity<ApiError> =
    ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError("bad_request", ex.message ?: "Bad request"))

  @ExceptionHandler(IllegalStateException::class)
  fun handleIllegalState(ex: IllegalStateException): ResponseEntity<ApiError> =
    ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError("illegal_state", ex.message ?: "Illegal state"))

  @ExceptionHandler(Exception::class)
  fun handleGeneric(ex: Exception): ResponseEntity<ApiError> =
    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiError("internal_error", ex.message ?: "Unexpected error"))
}
