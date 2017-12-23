package com.example.playscalajs.data

import com.example.playscalajs.shared.CookieSession.Trace
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
import com.redis._

object Sessions {

  val sampleTraces3 = List(
    Trace(2, "a", "b"),
    Trace(8, "c", "d")
  )

  val sampleTraces4 = List(
    Trace(2, "a", "b"),
    Trace(3, "b", "c"),
    Trace(6, "c", "d"),
    Trace(10, "d", "e")
  )

  // val sessionList = Vector(sampleTraces, sampleTraces2)

  def sessionsSerial = {
    val r = new RedisClient("localhost", 6379)
    val data = r.lrange("cookie1", 0, -1)
    // List(sampleTraces3, sampleTraces4).asJson.noSpaces
    data match {
      case None => ""
      case Some(list) => {
        // dirty
        val x = list.flatten.mkString(",")
        s"[[${x}]]"
      }
    }
  }
}
