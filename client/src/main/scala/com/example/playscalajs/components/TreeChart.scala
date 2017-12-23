package com.example.playscalajs.components

import com.example.playscalajs.shared.CookieSession.Trace
import japgolly.scalajs.react.{CtorType, _}
import japgolly.scalajs.react
import japgolly.scalajs.react.component.Scala.Component
import japgolly.scalajs.react.vdom.{TagOf, VdomElement}
import japgolly.scalajs.react.vdom.all.{
  a,
  div,
  href,
  onChange,
  onClick,
  option,
  p,
  select,
  target,
  value
}
import org.scalajs.dom.svg.G
import japgolly.scalajs.react.vdom.svg_<^._


object TreeChart {
  val firstTrace = (1, "x", "x")

  class BackendTreeChart($: BackendScope[List[Trace], (Int, String, String)]) {
    def movie(t: Int, tmax: Int, tmin: Int): String = {
      val padding = 40
      val width = 470
      val ynew = 200
      val xnew = padding + ((width - 2 * padding) * (t - tmin).toFloat / (tmax - tmin).toFloat).toInt
      s"translate(${xnew},${ynew})"
    }

    def onClickTrace(trace: Trace) = { e: onClick.Event =>
      println("clicked circle")
      $.setState((trace.timestamp, trace.referer, trace.url))
    }

    def render(traces: List[Trace], stateTrace: (Int, String, String)) = {
      val myRect: TagOf[G] =
        <.g(^.transform := "translate(0,10)",
          <.rect(^.fill := "#eeeeee", ^.width := 130, ^.height := 30),
          <.text(^.transform := "translate(5, 20)", stateTrace.toString))
      val myLink = <.g(^.transform := "translate(0,10)",
        a(<.text("brabbel"),
          href := "http://www.github.com",
          target := "_blank"))

      val maxTime = traces.map(x => x.timestamp).max
      val minTime = traces.map(x => x.timestamp).min

      // val nodes: js.Array[ReactTag] = traces.map({
      val nodes = traces
        .map({ trace: Trace =>
          <.g(
            ^.transform := movie(trace.timestamp, maxTime, minTime),
            <.circle(onClick ==> onClickTrace(trace),
              ^.r := 12,
              ^.cx := 0,
              ^.cy := 0)
            // <.text( ^.transform := "translate(10,0)", ^.textAnchor := "end", trace.url )
          )
        })
        .toVdomArray

      <.svg(^.width := 460,
        ^.height := 400,
        <.g(^.transform := "translate(0,0)", nodes, myRect))

    }
  }

  val treeChartComp = ScalaComponent
    .builder[List[Trace]]("Traces")
    .initialState(firstTrace)
    .renderBackend[BackendTreeChart]
    .build

}
