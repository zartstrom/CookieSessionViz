package com.example.playscalajs.components
import com.example.playscalajs.shared.CookieSession.{Session, Trace}
import TreeChart.treeChartComp
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.all.{div, onChange, option, select, value}
import japgolly.scalajs.react.vdom.svg_<^._
import org.scalajs.dom

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import io.circe.generic.auto._
import io.circe.parser._
import scala.concurrent.duration._
import dom.ext.Ajax

import scala.concurrent.{Await, Future}

object SessionLoader {
  val jsonHeaders =
    Map("Accept" -> "application/json", "Content-Type" -> "application/json")



  val sampleTraces = List(
    Trace(2, "a", "b"),
    Trace(5, "b", "c"),
    Trace(8, "c", "d")
  )

  val sampleTraces2 = List(
    Trace(2, "a", "b"),
    Trace(3, "b", "c"),
    Trace(6, "c", "d"),
    Trace(7, "d", "e")
  )

  val sessionList = Vector(sampleTraces, sampleTraces2)

  class SessionLoaderBackend($ : BackendScope[String, Seq[Session]]) {
    def sessionsCall: Future[String] = dom.ext.Ajax
      .get(url = "/sessions", headers = jsonHeaders)
      .map(_.responseText)

    def fetch3 = {
      Ajax.get(url = "/sessions", headers = jsonHeaders).map(_.responseText).onSuccess {
        case r => {
          decode[Vector[Session]](r) match {
            case Left(_) => {
              println(s"Could not parse <${r}>")
              $.modState(s => s)
            }
            case Right(sessionVec) => {
              println("got new bread")
              println(r)
              $.setState(sessionVec).runNow()
            }
          }
        }
      }
    }

    def render(name: String, sessions: Seq[Session]): VdomElement = {
      // Await.ready(fetch, 5 seconds)

      def x = SessionChoice.sessionChoiceComp(sessions)
      div(x)
    }
  }

  val emptySessions: Seq[Session] = Nil

  val sessionLoaderComp = ScalaComponent
    .builder[String]("Load sessions")
    .initialState(emptySessions)
    .renderBackend[SessionLoaderBackend]
    .componentDidMount(life => Callback { life.backend.fetch3 } )
    .build
}
