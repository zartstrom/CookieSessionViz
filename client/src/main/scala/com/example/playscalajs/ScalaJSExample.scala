package com.example.playscalajs

import com.example.playscalajs.shared.{CookieSession, SharedMessages}
import org.scalajs.dom

import com.example.playscalajs.components.SessionLoader

object ScalaJSExample {

  // @JSExport
  def main(args: Array[String]): Unit = {
    dom.document.getElementById("scalajsShoutOut").textContent =
      SharedMessages.itWorks

    //ReactDOM.render(Tree, dom.document.getElementById("root"))
    val x = SessionLoader.sessionLoaderComp("new stuff")
    x.renderIntoDOM(dom.document.getElementById("react"))
    // Tree.NoArgs().renderIntoDOM(dom.document.getElementById("react"))
  }
}
