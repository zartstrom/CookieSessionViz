package org.markgrafendamm.playscalajs.components

import org.markgrafendamm.playscalajs.shared.CookieSession._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.all.{`class`, div, id, key, onClick, onMouseMove, table, tbody, td, tr}
import japgolly.scalajs.react.vdom.svg_<^._
import org.querki.jquery.{JQueryPosition, $ => jquery}


case class Point(x: Int, y: Int)
case class Line(start: Point, end: Point)
case class TCState(traceOpt: Option[Trace], lineXOpt: Option[Int])
object TCState {
  def empty: TCState = TCState(Option.empty[Trace], Option.empty[Int])
}

object TreeChart {

  class BackendTreeChart($ : BackendScope[SessionGraph, TCState]) {
    val svgHeight = 400
    val svgId = "clickPathChart"
    val svgDisplayWidth = 500
    val svgPadding = 40

    def movieX(time: Float, timeMax: Int, timeMin: Int): Int = {
      val padding = svgPadding
      val width = svgDisplayWidth
      val xnew = padding + ((width - 2 * padding) * (time - timeMin) / (timeMax - timeMin).toFloat).toInt
      xnew
    }

    def movieY(coordinateY: Float): Int = (200 + 60 * coordinateY).toInt

    def translateStr(p: Point): String = {
      s"translate(${p.x},${p.y})"
    }

    def onClickTrace(trace: Trace) = { e: onClick.Event =>
      $.modState(s => s.copy(traceOpt = Some(trace)))
    }

    def drawLine(e: ReactMouseEventFromHtml) = {
      // didn't find another way without jquery yet.
      val offset: JQueryPosition = jquery(s"#${svgId}").offset()

      val x = (e.pageX - offset.left).toInt
      $.modState(s => s.copy(lineXOpt = Some(x)))
    }

    def render(sessionGraph: SessionGraph, state: TCState) = {
      val sessionInfo = renderSessionInfo(sessionGraph)
      val traceInfo = renderTraceInfo(state.traceOpt)
      val timeInfo = renderTimeInfo(sessionGraph, state.lineXOpt)
      val svg = renderSvg(sessionGraph, state)
      div(sessionInfo, traceInfo, svg, timeInfo)
    }

    // move this stuff
    def toLeft(s: String) = div(`class` := "toLeft", s)
    def toRight(s: String) = div(`class` := "toRight", s)
    def cell(key: String, content: Any) =
      td(toLeft(key), toRight(content.toString))

    def makeInfoTableRow(data: Seq[(String, Any)]) = {
      tr(data.map(t => cell(t._1, t._2)).toVdomArray)
    }

    def makeInfoTable(data: Seq[(String, Any)], nofColumns: Int) = {
      val trs = data.grouped(nofColumns).map(makeInfoTableRow(_)).toVdomArray
      table(tbody(trs))
    }

    def renderSessionInfo(sessionGraph: SessionGraph) = {
      val names = List("cookie value:",
                       "nof clickpaths:",
                       "nof traces:",
                       "nof leadouts:",
                       "session start:",
                       "session end:")
      val values = List(sessionGraph.cookie,
                        1,
                        sessionGraph.nodes.length,
                        0,
                        sessionGraph.minTime,
                        sessionGraph.maxTime)
      val sessionInfoTable = makeInfoTable(names zip values, 2)
      div(`class` := "info", sessionInfoTable)
    }

    def renderTraceInfo(traceOption: Option[Trace]) = {
      val names = List("timestamp:", "referer:", "url:")
      val traceTable = traceOption match {
        case None => makeInfoTable(names zip List("", "", ""), 3)
        case Some(trace) =>
          makeInfoTable(
            names zip List(trace.timestamp, trace.referer, trace.url),
            3)
      }
      div(id := "traceInfo", `class` := "info", traceTable)

    }

    def renderTimeInfo(sg: SessionGraph, lineXOpt: Option[Int]) = {
      val values = lineXOpt match {
        case None => List("", "")
        case Some(x) =>
          // need to generalize this, i.e. for actual timestamps
          val time = (sg.maxTime - sg.minTime) * (x - svgPadding) / (svgDisplayWidth - 2 * svgPadding) + sg.minTime
          val s = sg.nodes.filter(_.data.timestamp <= time).length
          List(time, s)
      }
      val data = List("tm", "nof smaller") zip values
      div(id := "timeInfo", `class` := "info", makeInfoTable(data, 2))
    }

    def renderLine(lineXOpt: Option[Int]) = {
      lineXOpt match {
        case None => <.line()
        case Some(x) =>
          <.line(^.x1 := x,
                 ^.y1 := 0,
                 ^.x2 := x,
                 ^.y2 := svgHeight,
                 ^.stroke := "rgb(155, 155, 155)",
                 ^.strokeWidth := 1)
      }
    }

    def renderSvg(sessionGraph: SessionGraph, state: TCState) = {
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

      val verticalLine = renderLine(state.lineXOpt)

      <.svg(
        id := svgId,
        `class` := "info",
        ^.width := "100%",
        ^.height := svgHeight,
        onMouseMove ==> drawLine,
        <.g(key := "chart",
            ^.transform := "translate(0,0)",
            verticalLine,
            edges,
            nodes)
      )

    }
  }

  val treeChartComp = ScalaComponent
    .builder[SessionGraph]("Traces Chart")
    .initialState(TCState.empty)
    .renderBackend[BackendTreeChart]
    .build

}
