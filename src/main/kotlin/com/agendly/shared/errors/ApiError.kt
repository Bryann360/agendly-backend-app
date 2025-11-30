package com.agendly.shared.errors

data class ApiError(
  val code: String,
  val message: String,
  val details: Map<String, Any?>? = null
)
