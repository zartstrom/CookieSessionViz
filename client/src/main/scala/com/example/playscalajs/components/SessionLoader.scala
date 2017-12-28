package com.example.playscalajs.components
import com.example.playscalajs.shared.CookieSession.{Session, Trace}
import TreeChart.treeChartComp
import com.example.playscalajs.shared.SessionGraph
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
//import japgolly.scalajs.react.vdom.all.{div, onChange, option, select, value}
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom

import scala.scalajs.js.timers._
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import io.circe.generic.auto._
import io.circe.parser._

import scala.concurrent.duration._
import dom.ext.Ajax

import scala.concurrent.{Await, Future}

case class SessionLoaderState(refresh: Boolean, data: Option[SessionGraph])

object SessionLoader {
  val jsonHeaders =
    Map("Accept" -> "application/json", "Content-Type" -> "application/json")

  class SessionLoaderBackend($ : BackendScope[String, SessionLoaderState]) {
    def sessionsCall: Future[String] =
      dom.ext.Ajax
        .get(url = "/sessions", headers = jsonHeaders)
        .map(_.responseText)

    def fetch3: Unit = {
      Ajax
        .get(url = "/sessions", headers = jsonHeaders)
        .map(_.responseText)
        .onSuccess {
          case r => {
            decode[SessionGraph](r) match {
              case Left(_) => {
                println(s"Could not parse <${r}>")
                $.modState(s => s)
              }
              case Right(sessionGraph) => {
                println("got new bread")
                println(r)

                // $.setState(Some(sessionGraph)).runNow()
                $.modState({s => SessionLoaderState(s.refresh, Some(sessionGraph))}).runNow()
              }
            }
          }
        }
    }

    def refresh(refreshNow: Boolean): Unit = {
      val refreshToggle = true
      if (refreshToggle || refreshNow) {
        fetch3
      }
      // val p: CallbackTo[$.Props] = $.props
      //$.pro

      setTimeout(1000 seconds) { // note the absence of () =>
        refresh(false)
      }
    }

    def render(name: String,
               state: SessionLoaderState): VdomElement = {
      val refreshDiv = <.div(<.form(<.input("refresh")))
      val dataDiv = state.data match {
        case None     => { <.div("no data available") }
        case Some(sg) => <.div(treeChartComp(sg))
      }
      <.div(refreshDiv, dataDiv)
    }
  }

  val emptySessionGraph: Option[SessionGraph] = None

  val sessionLoaderComp = ScalaComponent
    .builder[String]("Load sessions")
    .initialState(SessionLoaderState(true, emptySessionGraph))
    .renderBackend[SessionLoaderBackend]
    .componentDidMount(life => Callback { life.backend.refresh(refreshNow = true) })
    .build
}
