package eu.idealo.com.playscalajs

import org.scalajs.dom
import scalacss.DevDefaults._
import eu.idealo.com.playscalajs.components.SessionChoice
import eu.idealo.com.playscalajs.stylesheets.Style

object ScalaJSMain {

  def main(args: Array[String]): Unit = {
    Style.addToDocument()

    val x = SessionChoice.sessionChoiceComp("unused string")
    x.renderIntoDOM(dom.document.getElementById("react"))
  }
}
