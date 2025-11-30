package com.agendly.shared.text

import java.text.Normalizer

object Slugify {
  fun slugify(raw: String): String {
    val n = Normalizer.normalize(raw.trim().lowercase(), Normalizer.Form.NFD)
      .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    return n.replace("[^a-z0-9]+".toRegex(), "-")
      .trim('-')
      .take(80)
  }
}
