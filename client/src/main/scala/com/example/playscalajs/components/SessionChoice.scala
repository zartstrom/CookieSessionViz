package com.example.playscalajs.components

import com.example.playscalajs.shared.CookieSession.Session
import TreeChart.treeChartComp

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.all.{div, onChange, option, select, value}
import japgolly.scalajs.react.vdom.svg_<^._

object SessionChoice {
  val NoArgs =
    ScalaComponent.static("No args")(div("Hello!"))

  class ChoiceBackend($ : BackendScope[Seq[Session], Int]) {
    def onC(e: ReactEventFromInput) = {
      val x = e.target.value.toInt
      println(s"event fun ${x}")
      $.setState(x)
    }

    def render(sessions: Seq[Session], choiceState: Int): VdomElement = {
      //just use length of sessions
      println("render session choice")
      println(sessions.length)
      if (sessions.length > 0) {
        val options = sessions.toList.zipWithIndex
          .map({
            case (_, index) => option(value := index.toString, s"Session${index}")
          })
          .toVdomArray
        // .map({(trace, idx) => option(value := idx.toString)})
        val sel = select(options, onChange ==> onC _)

        def treeChart(x: Int) = treeChartComp(sessions(x))
        div(sel, treeChart(choiceState))
      } else {
        div("no data yet")
      }
    }
  }

  val sessionChoiceComp = ScalaComponent
    .builder[Seq[Session]]("Choose a session")
    .initialState(0)
    .renderBackend[ChoiceBackend]
    .build
}
