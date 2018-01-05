package com.example.playscalajs

import com.example.playscalajs.shared.{CookieSession, SharedMessages}
import org.scalajs.dom

import com.example.playscalajs.components.SessionLoader

object ScalaJSExample {

  def main(args: Array[String]): Unit = {
    // dom.document.getElementById("scalajsShoutOut").textContent = SharedMessages.itWorks

    val x = SessionLoader.sessionLoaderComp("new stuff")
    x.renderIntoDOM(dom.document.getElementById("react"))
  }
}
