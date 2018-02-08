package eu.idealo.com.playscalajs.components

import scala.scalajs.js.JSON.stringify
import eu.idealo.com.playscalajs.shared.CookieSession._
import TreeChart.treeChartComp
// import eu.idealo.com.playscalajs.shared.ClickPathForest
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.all.{
  div,
  key,
  onChange,
  option,
  select,
  value
}
import japgolly.scalajs.react.vdom.html_<^._

object SessionChoice {
  val NoArgs =
    ScalaComponent.static("No args")(div("Hello!"))

  class ChoiceBackend($ : BackendScope[String, Option[String]]) {

    def onSubmit(e: ReactEventFromInput) = {
      println("in onSubmit")
      //println(stringify(e))
      // println(e.target)
      // val x = e.target.value.toString
      // println(s"event fun ${x}")
      e.preventDefaultCB >> $.modState(s => s)
    }

    def onFormChange(e: ReactEventFromInput) = Callback {}

    def onChange(e: ReactEventFromInput) = {
      val newValue = e.target.value
      $.modState(_ => Some(newValue))
    }
    def renderForm(cookie: Option[String]): VdomElement = {
      // value := "01ye0h0300j720a0zr",
      <.form(^.onSubmit ==> onSubmit,
        <.input(^.`type` := "text", ^.onChange ==> onChange, ^.value := cookie.get),
        <.input(^.`type` := "submit", value := "Submit"))
    }

    def render(dummy: String, cookie: Option[String]): VdomElement = {
      //just use length of sessions
      println("render session choice")
      println(cookie)
      val formDiv = <.div(renderForm(cookie))
      def contentDiv = cookie match {
        case None => div("No data yet")
        case Some(cookieString) =>
          <.div(SessionDisplayer.sessionDisplayerComponent(cookieString))
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
