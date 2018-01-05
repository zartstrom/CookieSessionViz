package com.example.playscalajs.data

import com.example.playscalajs.shared.CookieSession.Trace
import com.example.playscalajs.shared.Forest
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import com.redis._

object Storage {

  val sampleTraces3 = List(
    Trace(2, "a", "b"),
    Trace(8, "c", "d")
  )

  val sampleTraces4 = List(
    Trace(1, "start", "a"),
    Trace(3, "a", "b"),
    Trace(5, "a", "f"),
    Trace(6, "b", "c"),
    Trace(9, "c", "d"),
    Trace(12, "c", "g"),
    Trace(15, "d", "e")
  )

  def sessionsSerial = {
    val cookieVal = "cookie1"
    val r = new RedisClient("127.0.0.1", 6379)
    val data = r.lrange(cookieVal, 0, -1)
    // List(sampleTraces3, sampleTraces4).asJson.noSpaces
    data match {
      case None => ""
      case Some(list) => {
        // dirty
        val x = list.flatten.mkString(",")
        val x2: Seq[Either[Error, Trace]] = list.flatten.map(decode[Trace](_))
        // val x3 = x2.flatten
        val x4: List[Trace] = list.flatten.map(decode[Trace](_)).collect({ case Right(t) => t }).toList
        // buildTree(sampleTraces4)
        Forest.sessionData(x4, cookieVal).asJson.noSpaces
      }
    }
    // Trees.buildSessionGraph(sampleTraces4).asJson.noSpaces
  }
}
