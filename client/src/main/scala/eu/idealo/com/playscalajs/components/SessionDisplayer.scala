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

    def ajax(cookie: String): Unit = {
      println("in ajax")
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
                  SessionDisplayerState(s.refresh, Some(sessionGraph))
                }).runNow()
              }
            }
          }
        }
    }

    def fetchData: Unit = {
      // clean this up; i.e make ajax(cookie) return a Callback and do $.props.flatMap(ajax).runNow()
      // need also to figure out how to replace deprecate onSuccess and still return a callback.
      println("fetch data from backend")
      $.props
        .map(cookie => { println(s"cookie: ${cookie}"); ajax(cookie) })
        .runNow()
    }

    def refresh(refreshNow: Boolean): Unit = {
      println("refresh")
      println($.props.runNow())
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
        case None     => { <.div("no data available") }
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
    // .componentDidUpdate(life => Callback { life.backend.fetchData })
    // .componentWillReceiveProps(life => Callback { life.backend.fetchData })
    //.componentWillReceivePropsConst(life => Callback { life.backend.fetchData })
    //.shouldComponentUpdate(
    .componentWillReceiveProps(life =>
      Callback {
        if (life.currentProps != life.nextProps) { life.backend.ajax(life.nextProps) }
    })
    .componentDidMount(life =>
      Callback { life.backend.refresh(refreshNow = false) })
    .build
}
