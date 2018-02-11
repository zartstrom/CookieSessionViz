package eu.idealo.com.playscalajs.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import scalacss.ScalaCssReact._
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import scala.scalajs.js.timers._

import eu.idealo.com.playscalajs.components.TreeChart.treeChartComp
import eu.idealo.com.playscalajs.stylesheets.Style
import eu.idealo.com.playscalajs.shared.CookieSession.SessionGraph

import io.circe.generic.auto._
import io.circe.parser._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Try, Success, Failure}

case class SessionDisplayerState(refresh: Boolean, data: Option[SessionGraph])

object SessionDisplayer {
  val jsonHeaders =
    Map("Accept" -> "application/json", "Content-Type" -> "application/json")

  class SessionLoaderBackend($ : BackendScope[String, SessionDisplayerState]) {
    def sessionsCall: Future[String] =
      dom.ext.Ajax
        .get(url = "/sessions", headers = jsonHeaders)
        .map(_.responseText)

    def onSelectRefresh(e: ReactEventFromInput) = {
      val checked = e.target.checked // need to persist value before using it in callback
      $.modState({ s =>
        SessionDisplayerState(checked, s.data)
      })
    }

    def decodeSessionGraph(string: String): Callback = {
      decode[SessionGraph](string) match {
        case Left(_) => Callback.empty
        case Right(sessionGraph) => {
          $.modState({ s =>
            SessionDisplayerState(s.refresh, Some(sessionGraph))
          })
        }
      }
    }

    def fetchDataAjax(cookie: String): Unit = {
      println(s"in ajax to request session data for cookie ${cookie}")
      Ajax
        .get(url = s"/sessions/${cookie}", headers = jsonHeaders)
        .map(_.responseText)
        .onComplete {
          case Success(string) => {
            decodeSessionGraph(string).runNow()
          }
          case Failure(_) => println("Failure")
        }
    }

    def fetchData: Unit = {
      /* query backend props and fetch data with ajax call */
      println("fetch data from backend")
      $.props
        .map(cookie => fetchDataAjax(cookie))
        .runNow()
    }

    def refresh(refreshNow: Boolean): Unit = {
      val refreshToggle = $.state.runNow().refresh

      if (refreshToggle || refreshNow) {
        fetchData
      }

      setTimeout(10 seconds) { // note the absence of () =>
        refresh(false)
      }
    }

    def render(name: String, state: SessionDisplayerState): VdomElement = {
      println("render session displayer")
      //refresh(refreshNow = true)
      // fetchData
      val refreshDiv = <.div(
        <.form(^.onChange ==> onSelectRefresh _,
               <.input(^.value := "refresh",
                       ^.name := "refresh",
                       ^.`type` := "checkbox"),
               <.label(^.`for` := "refresh", "auto refresh")))
      val dataDiv = state.data match {
        case None => {
          <.div(Style.infoBox, "No data available, enter a cookie value")
        }
        case Some(sg) => <.div(treeChartComp(sg))
      }
      <.div(dataDiv, refreshDiv)
    }
  }

  val emptySessionGraph: Option[SessionGraph] = None

  val sessionDisplayerComponent = ScalaComponent
    .builder[String]("display session")
    .initialState(SessionDisplayerState(false, emptySessionGraph))
    .renderBackend[SessionLoaderBackend]
    .componentWillReceiveProps(life =>
      Callback {
        if (life.currentProps != life.nextProps) {
          life.backend.fetchDataAjax(life.nextProps)
        }
    })
    .componentDidMount(life =>
      Callback { life.backend.refresh(refreshNow = false) })
    .build
}
