package org.daron.board.service

trait RichString {

  implicit class RichString(s: String) {
    import java.util.Base64
    import java.nio.charset.StandardCharsets._

    def toBase64String: String = Base64.getEncoder.encodeToString(s.getBytes(UTF_8))

    def fromBase64String: String = new String(Base64.getDecoder.decode(s), UTF_8)
  }

}
