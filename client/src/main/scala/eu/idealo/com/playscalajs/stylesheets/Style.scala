package eu.idealo.com.playscalajs.stylesheets

import CssSettings._

object Style extends StyleSheet.Inline {

  import dsl._

  /*
  val bootstrapButton = style(
    addClassName("btn btn-default"),
    fontSize(200 %%)
  )
   */
  val infoBox = style(
    backgroundColor(c"#EAECEE"),
    padding(8 px),
    marginBottom(5 px)
  )
}
