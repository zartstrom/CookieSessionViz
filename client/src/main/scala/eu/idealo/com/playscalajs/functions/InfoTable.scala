package eu.idealo.com.playscalajs.functions

import japgolly.scalajs.react._
// import japgolly.scalajs.react.vdom.all.{`class`, div, id, key, onClick, onMouseMove, table, tbody, td, tr}
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.html.Table
import org.scalajs.dom.html.Div
import org.scalajs.dom.html.Anchor

object InfoTable {

  sealed trait Content {
    def render: VdomTagOf[_]
  }

  case class Text(string: String) extends Content {
    def render: VdomTagOf[Div] = <.div(string)
  }
  case class Link(url: String) extends Content {
    def render: VdomTagOf[Anchor] = {
      val urlShortened = if (url.length < 120) url else url.take(119) + "…"
      <.a(^.href := url, ^.target := "_blank", urlShortened)
    }
  }

  object Content {
    def empty: Content = Text("")
  }


  def toLeft(s: String) = <.div(^.`class` := "toLeft", s)
  def toRight(content: Content) = <.div(^.`class` := "toRight", content.render)
  def cell(key: String, content: Content) =
    <.td(toLeft(key), toRight(content))

  def makeInfoTableRow(data: Seq[(String, Content)]) = {
    <.tr(data.map(t => cell(t._1, t._2)).toVdomArray)
  }

  def makeInfoTable(data: Seq[(String, Content)], nofColumns: Int): VdomTagOf[Table]  = {
    val trs = data.grouped(nofColumns).map(makeInfoTableRow(_)).toVdomArray
    <.table(<.tbody(trs))
  }
}
