package com.example.playscalajs.components

import com.example.playscalajs.shared.CookieSession.Trace
import com.example.playscalajs.shared.{SessionGraph, Coordinate}
import com.example.playscalajs.shared.Trees._
import japgolly.scalajs.react.{CtorType, _}
import japgolly.scalajs.react
import japgolly.scalajs.react.component.Scala.Component
import japgolly.scalajs.react.vdom.{TagOf, VdomElement}
import japgolly.scalajs.react.vdom.all.{a, div, href, key, onChange, onClick, option, p, select, target, value}
import org.scalajs.dom.svg.G
import japgolly.scalajs.react.vdom.svg_<^._

case class Point(x: Int, y: Int)
case class Line(start: Point, end: Point)

object TreeChart {
  val emptyTrace = (0, "", "")

  class BackendTreeChart($ : BackendScope[SessionGraph, (Int, String, String)]) {
    def movie(t: Int, tmax: Int, tmin: Int): Point = {
      val padding = 40
      val width = 470
      val ynew = 200
      val xnew = padding + ((width - 2 * padding) * (t - tmin).toFloat / (tmax - tmin).toFloat).toInt
      Point(xnew, ynew)
    }

    def movieX(t: Float, tmax: Int, tmin: Int): Int = {
      val padding = 40
      val width = 470
      val xnew = padding + ((width - 2 * padding) * (t - tmin) / (tmax - tmin).toFloat).toInt
      xnew
    }

    def movieY(position: Float): Int = (200 + 60 * position).toInt

    def pointFromTree(treeR: TreeR, maxTime: Int, minTime: Int): Point = {
      val x = movieX(treeR.trace.timestamp, maxTime, minTime)
      val y = movieY(treeR.positionY)
      Point(x, y)
    }

    def translateStr(p: Point): String = {
      s"translate(${p.x},${p.y})"
    }

    def onClickTrace(trace: Trace) = { e: onClick.Event =>
      println("clicked circle")
      $.setState((trace.timestamp, trace.referer, trace.url))
    }

    def render(sessionGraph: SessionGraph, stateTrace: (Int, String, String)) = {
      val myRect: TagOf[G] =
        <.g(
          key := "infoSheet",
          ^.transform := "translate(0,10)",
          <.rect(^.fill := "#eeeeee", ^.width := 130, ^.height := 30),
          <.text(^.transform := "translate(5, 20)", stateTrace.toString)
        )
      val myLink = <.g(^.transform := "translate(0,10)",
                       a(<.text("brabbel"),
                         href := "http://www.github.com",
                         target := "_blank"))

      val maxTime = sessionGraph.maxTime
      val minTime = sessionGraph.minTime

      def scale(unscaled: Coordinate): Point = {
        val x = movieX(unscaled.x, maxTime, minTime)
        val y = movieY(unscaled.y)
        Point(x, y)
      }

      /*
      def pointsAndTraces(treeR: TreeR): List[(Point, Trace)] = {
        val fromChildren = treeR.children.flatMap(pointsAndTraces)
        val x = movieX(treeR.trace.timestamp, maxTime, minTime)
        val y = movieY(treeR.positionY)
        (Point(x, y), treeR.trace) :: fromChildren
      }

      val pAndT = pointsAndTraces(treeR)
      // println(pAndT.length)
      // println(pAndT)

      def linesFromTree(tree: TreeR): List[Line] = {
        val childrenLines = tree.children.flatMap(linesFromTree)
        val myLines = tree.children.map({ c =>
          Line(pointFromTree(tree, maxTime, minTime),
               pointFromTree(c, maxTime, minTime))
        })
        (myLines ++ childrenLines).toList
      }
      val lines: List[Line] = linesFromTree(treeR)
      */

      // val points = traces.map(t => movie(t.timestamp, maxTime, minTime))
      // val pairs: Seq[List[Point]] = points.sliding(2).toList
      // val edges = pairs
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
        .map({
          sessionNode =>
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

      <.svg(^.width := 460,
            ^.height := 400,
            <.g(key := "chart",
                ^.transform := "translate(0,0)",
                nodes,
                edges,
                myRect))

    }
  }

  val treeChartComp = ScalaComponent
    .builder[SessionGraph]("Traces Chart")
    .initialState(emptyTrace)
    .renderBackend[BackendTreeChart]
    .build

}
