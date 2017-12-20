package com.example.playscalajs.shared

object CookieSession {

  case class Trace(timestamp: Int, referer: String, url: String)

  type Session = List[Trace]

}
