package eu.idealo.com.playscalajs.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._

import eu.idealo.com.playscalajs.stylesheets.Style
import scalacss.ScalaCssReact._

object SessionChoice {
  val cookieDefaultText: String = "your cookie here"

  case class CookieFormState(currentText: String, submitted: String)

  class ChoiceBackend($ : BackendScope[String, CookieFormState]) {

    def onSubmit(e: ReactEventFromInput) = {
      e.preventDefaultCB >> $.modState(s => {
        if (s.currentText == "") s
        else CookieFormState(s.currentText, s.currentText)
      })
    }

    def onChange(e: ReactEventFromInput) = {
      val cookieFormText = e.target.value
      $.modState(s => s.copy(currentText = cookieFormText))
    }

    def renderForm(cookieFormState: CookieFormState): VdomElement = {
      <.form(
        ^.onSubmit ==> onSubmit,
        <.label("Cookie"),
        <.input(^.`type` := "text", ^.onChange ==> onChange),
        <.input(^.`type` := "submit", ^.value := "Submit")
      )
    }

    def render(dummy: String, cookieFormState: CookieFormState): VdomElement = {
      val formDiv = <.div(renderForm(cookieFormState), Style.infoBox)
      def contentDiv =
        <.div(
          SessionDisplayer.sessionDisplayerComponent(cookieFormState.submitted))

      <.div(formDiv, contentDiv)
    }
  }

  val sessionChoiceComp = ScalaComponent
    .builder[String]("Enter a cookie value")
    .initialState(CookieFormState(cookieDefaultText, cookieDefaultText))
    .renderBackend[ChoiceBackend]
    .build
}
