package eu.idealo.com.playscalajs

import org.scalajs.dom
import eu.idealo.com.playscalajs.components.SessionDisplayer

object ScalaJSExample {

  def main(args: Array[String]): Unit = {
    // dom.document.getElementById("scalajsShoutOut").textContent = SharedMessages.itWorks

    val x = SessionDisplayer.sessionDisplayerComponent("cookie1")
    x.renderIntoDOM(dom.document.getElementById("react"))

  }
}
