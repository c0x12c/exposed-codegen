package spartan.exposed.codegen

/**
 * Generate current timestamp in the format of [Timestamp].
 */
internal val Timestamp.now: String
  get() {
    return "$type.now()"
  }
