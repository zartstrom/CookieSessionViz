package eu.idealo.com.playscalajs

import org.scalajs.dom
import eu.idealo.com.playscalajs.components.SessionLoader

object ScalaJSExample {

  def main(args: Array[String]): Unit = {
    // dom.document.getElementById("scalajsShoutOut").textContent = SharedMessages.itWorks

    val x = SessionLoader.sessionLoaderComp("new stuff")
    x.renderIntoDOM(dom.document.getElementById("react"))
  }
}
