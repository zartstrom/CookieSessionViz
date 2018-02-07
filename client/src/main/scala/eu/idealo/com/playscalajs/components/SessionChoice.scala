package eu.idealo.com.playscalajs.components

import eu.idealo.com.playscalajs.shared.CookieSession._
import TreeChart.treeChartComp
// import eu.idealo.com.playscalajs.shared.ClickPathForest
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.all.{div, key, onChange, option, select, value}
import japgolly.scalajs.react.vdom.html_<^._


object SessionChoice {
  val NoArgs =
    ScalaComponent.static("No args")(div("Hello!"))

  class ChoiceBackend($ : BackendScope[String, Option[String]]) {
    def onC(e: ReactEventFromInput) = {
      val x = e.target.value.toString
      println(s"event fun ${x}")
      $.setState(Some(x))
    }

    def onAction(e: ReactEventFromInput) = {
      val x = e.target.value.toString
      println(s"event fun ${x}")
      e.preventDefaultCB >>
      $.setState(Some(x))
    }

    def renderForm(): VdomElement = {
      // value := "01ye0h0300j720a0zr",
      <.form(^.onSubmit ==> onAction,  <.input(^.`type` := "text", ^.onChange ==> onAction), <.input(^.`type` := "submit", value := "Submit"))
    }
    val formDiv = <.div(renderForm)

    def render(dummy: String, cookie: Option[String]): VdomElement = {
      //just use length of sessions
      println("render session choice")
      println(cookie)
      val contentDiv = cookie match {
        case None => div("No data yet")
        case Some(cookieString) => <.div(SessionLoader.sessionLoaderComp(cookieString))
      }

      // <.div(contentDiv)
      <.div(formDiv, contentDiv)
    }
  }

  val sessionChoiceComp = ScalaComponent
    .builder[String]("Enter a cookie value")
    // .initialState(Option.empty[String])
    .initialState(Option("blub"))
    .renderBackend[ChoiceBackend]
    .build
}
