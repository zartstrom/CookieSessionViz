package com.example.playscalajs.components


import scala.scalajs.js
import japgolly.scalajs.react.{CtorType, _}
import japgolly.scalajs.react
import japgolly.scalajs.react.component.Scala.Component
import japgolly.scalajs.react.vdom.{TagOf, VdomElement}
import japgolly.scalajs.react.vdom.all.{a, div, href, onChange, onClick, option, p, select, target, value}
import org.scalajs.dom.svg.G
// import japgolly.scalajs.react.vdom.svg.all._
import japgolly.scalajs.react.vdom.svg_<^._
import org.scalajs.dom.Node
import org.scalajs.dom.raw.Attr
// import japgolly.scalajs.react.vdom.svg.all
// import paths.high.{Tree, TreeNode}
// import demo.colors._

object Tree {
  val NoArgs =
    ScalaComponent.static("No args")(div("Hello!"))
  case class Trace(timestamp: Int, referer: String, url: String)
  val firstTrace =
    (1,
      "https://www.google.de/search?q=kühlschrank+kaufen&oq=kühlschrank",
      "https://www.idealo.de/preisvergleich/ProductCategory/2800.html")

  val sampleTraces = List(
    Trace(2, "a", "b"),
    Trace(5, "b", "c"),
    Trace(8, "c", "d")
  )

  val sampleTraces2 = List(
    Trace(2, "a", "b"),
    Trace(3, "b", "c"),
    Trace(6, "c", "d"),
    Trace(7, "d", "e")
  )

  val sessionList = Vector(sampleTraces, sampleTraces2)

  class ChoiceBackend($ : BackendScope[Unit, Int]) {
    def chose(index: Int): Any => Unit = { x: Any =>
      println(s"chose ${index}")
      $.setState(index)
    }
    def onC(e: ReactEventFromInput) = {
      val x = e.target.value.toInt
      //.asInstanceOf[Int]
      println(s"event fun ${x}")
      $.setState(x)
    }
    def render(choiceState: Int): VdomElement = {
      val sel = select(option(value := "0", "Session1"),
        option(value := "1", "Session2"),
        onChange ==> onC _)

      def treeChart(x: Int) = TreeChart2(sessionList(x))
      val testP = p(choiceState.toString)

      div(sel, testP, treeChart(choiceState))

    }
  }
  val TreeChoice = ScalaComponent.builder[Unit]("Choose a tree")
    .initialState(0)
      .renderBackend[ChoiceBackend]
    .build
    /*
    .backend(new ChoiceBackend(_))
    .render_PCS((_, backend, choice) => {

      val sel = select(option(value := "0", text("Session1"), onChange ==> backend.chose(0)),
        option(value := "1", text("Session2"), onChange ==> backend.chose(1)),
        onChange ==> backend.onC _)

      def treeChart(x: Int) = TreeChart2(sessionList(x))
      val testP = p(text(choice.toString))

      div(sel, testP, treeChart(choice))
    })
    */
    // class Backend2($ : BackendScope[List[Trace], Trace]) {
    class Backend2($ : BackendScope[List[Trace], (Int, String, String)]) {
      def movie(t: Int, tmax: Int, tmin: Int): String = {
        val padding = 40
        val width = 470
        val ynew = 200
        val xnew = padding + ((width - 2 * padding) * (t - tmin).toFloat / (tmax - tmin).toFloat).toInt
        s"translate(${xnew},${ynew})"
      }
      def blub(trace: Trace) = { e: onClick.Event =>
        println("clicked circle")
        CallbackTo[Unit]( $.setState((3, trace.referer, trace.url)) )
      }
      def blub4(trace: Trace) = { e: onClick.Event =>
        println("clicked circle")
         $.setState((trace.timestamp, trace.referer, trace.url))
      }
      // def blub2(trace: Trace) = Callback( $.setState((3, trace.referer, trace.url)) )
      def blub3(trace: Trace) = $.setState((3, trace.referer, trace.url))
      def render(traces: List[Trace], stateTrace: (Int, String, String)) = {
        val myRect: TagOf[G] =
          <.g(^.transform := "translate(0,10)",
            <.rect(^.fill := "#eeeeee", ^.width := 60, ^.height := 30),
            <.text(^.transform := "translate(5, 20)",
              stateTrace.toString))
        val myLink = <.g(
          ^.transform := "translate(0,10)",
          a(<.text("brabbel"), href := "http://www.github.com", target := "_blank"))

        val maxTime = traces.map(x => x.timestamp).max
        val minTime = traces.map(x => x.timestamp).min

        // val nodes: js.Array[ReactTag] = traces.map({
        val nodes = traces.map({ trace: Trace =>
          <.g(
            ^.transform := movie(trace.timestamp, maxTime, minTime),
            <.circle(onClick ==> blub4(trace), ^.r := 30, ^.cx := 0, ^.cy := 0),
            <.text(
              ^.transform := "translate(10,0)",
              ^.textAnchor := "end",
              trace.url
            )
          )
        }).toVdomArray

        <.svg(^.width := 460,
          ^.height := 400,
          <.g(^.transform := "translate(0,0)", nodes, myRect, myLink))


      }
    }

  val TreeChart2 = ScalaComponent.builder[List[Trace]]("Traces")
    .initialState(firstTrace)
    .renderBackend[Backend2]
      .build
    /*
    .render((traces, firstTrace, backend) => {
      val myRect: ReactTag =
        g(transform := "translate(0,10)",
          rect(fill := "#eeeeee", width := 60, height := 30),
          text(`class` := "desc",
            transform := "translate(5, 20)",
            firstTrace.toString))
      val myLink: ReactTag = g(
        transform := "translate(0,10)",
        a(text("brabbel"), href := "http://www.github.com", target := "_blank"))

      val maxTime = traces.map(x => x.timestamp).max
      val minTime = traces.map(x => x.timestamp).min

      // val nodes: js.Array[ReactTag] = traces.map({
      val nodes: List[ReactTag] = traces.map({ trace: Trace =>
        g(
          transform := movie(trace.timestamp, maxTime, minTime),
          circle(onClick ==> backend.blub(trace), r := 30, cx := 0, cy := 0),
          text(
            transform := "translate(10,0)",
            textAnchor := "end",
            trace.url
          )
        )
      })

      svg(width := 460,
        height := 400,
        g(transform := "translate(0,0)", nodes, myRect, myLink))

    })
    .build
    */
}
