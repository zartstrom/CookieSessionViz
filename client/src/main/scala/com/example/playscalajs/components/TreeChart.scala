package com.example.playscalajs.components

import com.example.playscalajs.shared.CookieSession.Trace
import com.example.playscalajs.shared.{Coordinate, SessionGraph}
import com.example.playscalajs.shared.Forest._
import japgolly.scalajs.react.{CtorType, _}
import japgolly.scalajs.react
import japgolly.scalajs.react.component.Scala.Component
import japgolly.scalajs.react.vdom.{TagOf, VdomElement}
import japgolly.scalajs.react.vdom.all.{
  a,
  `class`,
  div,
  href,
  id,
  key,
  onChange,
  onClick,
  option,
  p,
  select,
  table,
  tbody,
  td,
  tr,
  value
}
import org.scalajs.dom.svg.{G, SVG}
import japgolly.scalajs.react.vdom.svg_<^._
// import japgolly.scalajs.react.vdom.html_<^._

case class Point(x: Int, y: Int)
case class Line(start: Point, end: Point)

object TreeChart {

  class BackendTreeChart($ : BackendScope[SessionGraph, Option[Trace]]) {
    def movieX(time: Float, timeMax: Int, timeMin: Int): Int = {
      val padding = 40
      val width = 470
      val xnew = padding + ((width - 2 * padding) * (time - timeMin) / (timeMax - timeMin).toFloat).toInt
      xnew
    }

    def movieY(coordinateY: Float): Int = (200 + 60 * coordinateY).toInt

    def translateStr(p: Point): String = {
      s"translate(${p.x},${p.y})"
    }

    def onClickTrace(trace: Trace) = { e: onClick.Event =>
      println("clicked circle")
      // $.setState((trace.timestamp, trace.referer, trace.url))
      $.setState(Some(trace))
    }

    def render(sessionGraph: SessionGraph, state: Option[Trace]) = {
      val sessionInfo = renderSessionInfo(sessionGraph)
      val traceInfo = renderTraceInfo(state)
      val svg = renderSvg(sessionGraph)
      div(sessionInfo, traceInfo, svg)
    }

    def toLeft(s: String) = div(`class` := "toLeft", s)
    def toRight(s: String) = div(`class` := "toRight", s)
    def cell(key: String, content: Any) =
      td(toLeft(key), toRight(content.toString))

    def renderSessionInfo(sessionGraph: SessionGraph) = {
      // TODO: rewrite this DRY
      div(
        `class` := "info",
        table(
          tbody(
            tr(
              td(div(`class` := "toLeft", "cookie value: "),
                 div(`class` := "toRight", sessionGraph.cookie)),
              td(div(`class` := "toLeft", "nof clickpaths: "),
                 div(`class` := "toRight", 1))
            ),
            tr(
              td(div(`class` := "toLeft", "nof traces: "),
                 div(`class` := "toRight", sessionGraph.nodes.length)),
              td(div(`class` := "toLeft", "nof leadouts: "),
                 div(`class` := "toRight", 0))
            ),
            tr(
              td(div(`class` := "toLeft", "session start: "),
                 div(`class` := "toRight", sessionGraph.minTime)),
              td(div(`class` := "toLeft", "session end: "),
                 div(`class` := "toRight", sessionGraph.maxTime))
            )
          ))
      )
    }

    def renderTraceInfo(traceOption: Option[Trace]) = {
      traceOption match {
        case None => div(id := "traceInfo", `class` := "info")
        case Some(trace) =>
          div(id := "traceInfo",
              `class` := "info",
              table(
                tbody(
                  tr(
                    cell("timestamp:", trace.timestamp),
                    cell("referer:", trace.referer),
                    cell("url:", trace.url)
                  ))))
      }
    }

    def renderSvg(sessionGraph: SessionGraph) = {
      println("render tree chart")
      /*
      val myLink = <.g(^.transform := "translate(0,10)",
                       a(<.text("brabbel"),
                         href := "http://www.github.com",
                         target := "_blank"))
       */

      val maxTime = sessionGraph.maxTime
      val minTime = sessionGraph.minTime

      def scale(unscaled: Coordinate): Point = {
        val x = movieX(unscaled.x, maxTime, minTime)
        val y = movieY(unscaled.y)
        Point(x, y)
      }

      val edges = sessionGraph.edges
        .map({ edge =>
          val start = scale(edge.start)
          val end = scale(edge.end)
          <.line(key := s"${start.x}-${start.y}-${end.x}-${end.y}",
                 ^.x1 := start.x,
                 ^.y1 := start.y,
                 ^.x2 := end.x,
                 ^.y2 := end.y,
                 ^.stroke := "rgb(255,0,0)",
                 ^.strokeWidth := 2)
        })
        .toVdomArray

      // val nodes: js.Array[ReactTag] = traces.map({
      val nodes = sessionGraph.nodes
        .map({ sessionNode =>
          <.g(
            key := s"${sessionNode.data.timestamp}-${sessionNode.data.url}",
            ^.transform := translateStr(scale(sessionNode.coordinate)),
            <.circle(onClick ==> onClickTrace(sessionNode.data),
                     ^.r := 12,
                     ^.cx := 0,
                     ^.cy := 0)
            // <.text( ^.transform := "translate(10,0)", ^.textAnchor := "end", trace.url )
          )
        })
        .toVdomArray

      <.svg(^.width := "100%",
            ^.height := 400,
            <.g(key := "chart", ^.transform := "translate(0,0)", edges, nodes))

    }
  }

  val treeChartComp = ScalaComponent
    .builder[SessionGraph]("Traces Chart")
    .initialState(Option.empty[Trace])
    .renderBackend[BackendTreeChart]
    .build

}
