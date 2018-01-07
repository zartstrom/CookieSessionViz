package org.markgrafendamm.playscalajs

import org.scalajs.dom
import org.markgrafendamm.playscalajs.components.SessionLoader

object ScalaJSExample {

  def main(args: Array[String]): Unit = {
    // dom.document.getElementById("scalajsShoutOut").textContent = SharedMessages.itWorks

    val x = SessionLoader.sessionLoaderComp("new stuff")
    x.renderIntoDOM(dom.document.getElementById("react"))
  }
}
