package eu.idealo.com.playscalajs.components

import eu.idealo.com.playscalajs.shared.CookieSession._
import eu.idealo.com.playscalajs.stylesheets.Style
import eu.idealo.com.playscalajs.functions.InfoTable._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.all.{
  `class`,
  div,
  id,
  key,
  onClick,
  onMouseMove,
  table,
  tbody,
  td,
  tr
}
import japgolly.scalajs.react.vdom.svg_<^._
import org.querki.jquery.{JQueryPosition, $ => jquery}
import org.scalajs.dom.html.Table
import scalacss.ScalaCssReact._

case class Point(x: Int, y: Int)
case class Line(start: Point, end: Point)
case class TCState(traceOpt: Option[Trace], lineXOpt: Option[Int])
object TCState {
  def empty: TCState = TCState(Option.empty[Trace], Option.empty[Int])
}

object TreeChart {

  class BackendTreeChart($ : BackendScope[SessionGraph, TCState]) {
    val svgHeight = 500
    val svgId = "clickPathChart"
    val svgDisplayWidth = 900
    val svgPadding = 40

    def movieX(time: Double, timeMax: Long, timeMin: Long): Int = {
      val padding = svgPadding
      val width = svgDisplayWidth
      val xnew = padding + ((width - 2 * padding) * (time - timeMin) / (timeMax - timeMin).toFloat).toInt
      xnew
    }

    def movieY(coordinateY: Double, treeWidth: Float): Int = {
      val heightFactor = math.min(60, (svgHeight - svgPadding) / treeWidth) // 60 is good spacing but maybe we need to be tighter
      (svgHeight / 2 + heightFactor * coordinateY).toInt
    }

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

    def renderSessionInfo(sessionGraph: SessionGraph) = {
      val names = List("cookie value:",
                       "nof clickpaths:",
                       "nof traces:",
                       "nof leadouts:",
                       "session start:",
                       "session end:")
      val values =
        List(sessionGraph.cookie,
             1,
             sessionGraph.nodes.length,
             0,
             renderEpochMillis(sessionGraph.minTime),
             renderEpochMillis(sessionGraph.maxTime)).map(x => Text(x.toString))
      val sessionInfoTable: VdomTagOf[Table] =
        makeInfoTable(names zip values, 2)
      div(Style.infoBox, sessionInfoTable)
    }

    def renderTraceInfo(traceOption: Option[Trace]) = {
      val names = List("timestamp:", "referer:", "url:")
      val traceTable = traceOption match {
        case None => makeInfoTable(names zip List.fill(3)(Content.empty), 1)
        case Some(trace) =>
          makeInfoTable(names zip List(Text(trace.displayTimestamp),
                                       Link(trace.referer),
                                       Link(trace.url)),
                        1)
      }
      div(id := "traceInfo", Style.infoBox, traceTable)

    }

    def renderTimeInfo(sg: SessionGraph, lineXOpt: Option[Int]) = {
      val values = lineXOpt match {
        case None => List.fill(2)(Content.empty)
        case Some(x) =>
          val time = (sg.maxTime - sg.minTime) * (x - svgPadding) / (svgDisplayWidth - 2 * svgPadding) + sg.minTime
          val s = sg.nodes.filter(_.data.timestamp <= time).length
          val timeString = renderEpochMillis(time)
          List(Text(timeString), Text(s.toString))
      }
      val data = List("tm", "nof smaller") zip values
      div(id := "timeInfo", Style.infoBox, makeInfoTable(data, 2))
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

      def scale(unscaled: Coordinate): Point = {
        val x = movieX(unscaled.x, sessionGraph.maxTime, sessionGraph.minTime)
        val y = movieY(unscaled.y, sessionGraph.treeWidth)
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

      div(
        Style.infoBox,
        <.svg(
          id := svgId,
          ^.width := "100%",
          ^.height := svgHeight,
          onMouseMove ==> drawLine,
          <.g(key := "chart",
              ^.transform := "translate(0,0)",
              verticalLine,
              edges,
              nodes)
        )
      )

    }
  }

  val treeChartComp = ScalaComponent
    .builder[SessionGraph]("Traces Chart")
    .initialState(TCState.empty)
    .renderBackend[BackendTreeChart]
    .build

}
