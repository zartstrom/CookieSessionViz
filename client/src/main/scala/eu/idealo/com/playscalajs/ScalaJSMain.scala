package eu.idealo.com.playscalajs

import org.scalajs.dom
// import eu.idealo.com.playscalajs.components.SessionDisplayer
import eu.idealo.com.playscalajs.components.SessionChoice

object ScalaJSMain {

  def main(args: Array[String]): Unit = {
    // dom.document.getElementById("scalajsShoutOut").textContent = SharedMessages.itWorks

    // val x = SessionLoader.sessionLoaderComp("new stuff")
    val x = SessionChoice.sessionChoiceComp("unused string")
    x.renderIntoDOM(dom.document.getElementById("react"))

  }
}