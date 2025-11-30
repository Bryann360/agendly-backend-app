package com.agendly.shared.errors.exceptions

open class NotFoundException(message: String): RuntimeException(message)
open class ConflictException(message: String): RuntimeException(message)
open class BadRequestException(message: String): RuntimeException(message)
open class UnauthorizedException(message: String): RuntimeException(message)
open class ForbiddenException(message: String): RuntimeException(message)
