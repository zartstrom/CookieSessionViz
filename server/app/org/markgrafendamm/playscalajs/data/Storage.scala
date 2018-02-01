package org.markgrafendamm.playscalajs.data

import com.redis._
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

import org.markgrafendamm.playscalajs.shared.CookieSession._
import org.markgrafendamm.playscalajs.logic.ClickPathForest.sessionData

object Storage {

  val sampleTraces3 = List(
    SimpleTrace(2, "a", "b"),
    SimpleTrace(8, "c", "d")
  )

  val sampleTraces4 = List(
    SimpleTrace(1, "start", "a"),
    SimpleTrace(3, "a", "b"),
    SimpleTrace(5, "a", "f"),
    SimpleTrace(6, "b", "c"),
    SimpleTrace(9, "c", "d"),
    SimpleTrace(12, "c", "g"),
    SimpleTrace(15, "d", "e")
  )

  def sessionsSerial = {
    val cookieVal = "cookie1"
    val r = new RedisClient("127.0.0.1", 6379)
    val data = r.lrange(cookieVal, 0, -1)
    data match {
      case None => ""
      case Some(list) => {
        // dirty
        println(list)
        // val x = list.flatten.mkString(",")
        // val x2: Seq[Either[Error, Trace]] = list.flatten.map(decode[Trace](_))
        // val x3 = x2.flatten
        val x4: List[SimpleTrace] = list.flatten.map(decode[SimpleTrace](_)).collect({ case Right(t) => t }).toList
        println(x4)
        // buildTree(sampleTraces4)
        val sd = sessionData(x4, cookieVal).asJson.noSpaces
        println(sd)
        sd
      }
    }
  }
}
