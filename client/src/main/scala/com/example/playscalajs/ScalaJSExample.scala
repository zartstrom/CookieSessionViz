package com.example.playscalajs

import com.example.playscalajs.shared.SharedMessages
import org.scalajs.dom
// import org.scalajs.d3v4._
import japgolly.scalajs.react.{CtorType, ReactDOM}
import com.example.playscalajs.components.Tree
import japgolly.scalajs.react.component.Scala.Component

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
// import org.singlespaced.d3js.d3
// import org.singlespaced.d3js.Ops._

import scala.scalajs.js

// @JSExportTopLevel("ScalaJSExample")
object ScalaJSExample {

  // @JSExport
  def main(args: Array[String]): Unit = {
    dom.document.getElementById("scalajsShoutOut").textContent =
      SharedMessages.itWorks

    //ReactDOM.render(Tree, dom.document.getElementById("root"))
    val x = Tree.TreeChoice()
    x.renderIntoDOM(dom.document.getElementById("react"))
    // Tree.NoArgs().renderIntoDOM(dom.document.getElementById("react"))

    val graphHeight = 450

    //The width of each bar.
    val barWidth = 80

    //The distance between each bar.
    val barSeparation = 10

    //The maximum value of the data.
    val maxData = 50

    //The actual horizontal distance from drawing one bar rectangle to drawing the next.
    val horizontalBarDistance = barWidth + barSeparation

    //The value to multiply each bar's value by to get its height.
    val barHeightMultiplier = graphHeight / maxData;

    //Color for start
    /*
    val c = d3.hcl("DarkSlateBlue")

    val rectXFun = (i: Int) => i * horizontalBarDistance
    val rectYFun = (d: Int) => graphHeight - d * barHeightMultiplier
    val rectHeightFun = (d: Int) => d * barHeightMultiplier
    val rectColorFun = (i: Int) => c.toString

    val svg = d3.select("#chart").append("svg").attr("width", "100%").attr("height", "450px")
    val sel = svg.selectAll("rect").data(js.Array(8, 22, 31, 36, 48, 17, 25))
    sel.enter()
      .append("rect")
      .attr("x", rectXFun)
      .attr("y", rectYFun)
      .attr("width", barWidth)
      .attr("height", rectHeightFun)
      .style("fill", rectColorFun)

   */
  }
}
