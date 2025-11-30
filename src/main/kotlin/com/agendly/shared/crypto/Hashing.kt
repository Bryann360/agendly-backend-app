package com.agendly.shared.crypto

import java.security.MessageDigest

object Hashing {
  fun sha256Hex(input: String): String {
    val md = MessageDigest.getInstance("SHA-256")
    val bytes = md.digest(input.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
  }
}
