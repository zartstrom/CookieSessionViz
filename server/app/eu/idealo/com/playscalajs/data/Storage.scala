package eu.idealo.com.playscalajs.data

import com.redis._
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

import eu.idealo.com.playscalajs.shared.CookieSession._
import eu.idealo.com.playscalajs.logic.ClickPathForest.sessionData

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

  def sessionsSerial(cookie: String): String = {
    /* cookie is a string like "01ts0h0900jcsx1btz" */
    println(cookie)
    val r = new RedisClient("127.0.0.1", 6379)
    val data = r.lrange(cookie, 0, -1)
    data match {
      case None => println("no data"); ""
      case Some(list) => {
        println(list)
        // val x4: List[IdealoTrace] = list.flatten.map(decode[IdealoTrace](_)).collect({ case Right(t) => t })
        val decoded = list.flatten.map(decode[SimpleTrace](_))
        println(decoded)
        val x4: List[SimpleTrace] = decoded.collect({ case Right(t) => t })
        println(x4)
        // buildTree(sampleTraces4)
        if (x4.length > 0) {
          sessionData(x4, cookie).asJson.noSpaces
        } else {
          ""
        }
        // println(sd)
      }
    }
  }
}
