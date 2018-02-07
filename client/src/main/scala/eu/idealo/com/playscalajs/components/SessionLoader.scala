package eu.idealo.com.playscalajs.components
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import eu.idealo.com.playscalajs.components.TreeChart.treeChartComp
import eu.idealo.com.playscalajs.shared.CookieSession.SessionGraph
//import japgolly.scalajs.react.vdom.all.{div, onChange, option, select, value}
import io.circe.generic.auto._
import io.circe.parser._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.scalajs.js.timers._

case class SessionLoaderState(refresh: Boolean, data: Option[SessionGraph])

object SessionLoader {
  val jsonHeaders =
    Map("Accept" -> "application/json", "Content-Type" -> "application/json")

  class SessionLoaderBackend($ : BackendScope[String, SessionLoaderState]) {
    def sessionsCall: Future[String] =
      dom.ext.Ajax
        .get(url = "/sessions", headers = jsonHeaders)
        .map(_.responseText)

    def onSelectRefresh(e: ReactEventFromInput) = {
      val checked = e.target.checked // need to persist value before using it in callback
      $.modState({ s =>
        SessionLoaderState(checked, s.data)
      })
    }

    def fetch3: Unit = {
      def getData(cookie: String) = {
        Ajax
          .get(url = s"/sessions/${cookie}", headers = jsonHeaders)
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
                  $.modState({ s =>
                    SessionLoaderState(s.refresh, Some(sessionGraph))
                  }).runNow()
                }
              }
            }
          }
      }
      $.props.map(getData).runNow()
    }

    def refresh(refreshNow: Boolean): Unit = {
      val refreshToggle = $.state.runNow().refresh

      if (refreshToggle || refreshNow) {
        fetch3
      }

      setTimeout(10 seconds) { // note the absence of () =>
        refresh(false)
      }
    }

    def render(name: String, state: SessionLoaderState): VdomElement = {
      println("render session loader")
      val refreshDiv = <.div(
        <.form(^.onChange ==> onSelectRefresh _,
               <.input(^.value := "refresh",
                       ^.name := "refresh",
                       ^.`type` := "checkbox"),
               <.label(^.`for` := "refresh", "auto refresh")))
      val dataDiv = state.data match {
        case None     => { <.div("no data available") }
        case Some(sg) => <.div(treeChartComp(sg))
      }
      <.div(dataDiv, refreshDiv)
    }
  }

  val emptySessionGraph: Option[SessionGraph] = None

  val sessionLoaderComp = ScalaComponent
    .builder[String]("Load sessions")
    .initialState(SessionLoaderState(false, emptySessionGraph))
    .renderBackend[SessionLoaderBackend]
    .componentDidMount(life =>
      Callback { life.backend.refresh(refreshNow = true) })
    .build
}
