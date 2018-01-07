package org.markgrafendamm.playscalajs.logic

import scala.collection.immutable.Seq

import org.markgrafendamm.playscalajs.shared.CookieSession._
import Forest._

object ClickPathForest {
  def sessionData(traces: Seq[Trace], cookieVal: String): SessionGraph = {
    // collect all data that is sent to the frontend
    val maxTime = traces.map(x => x.timestamp).max
    val minTime = traces.map(x => x.timestamp).min

    val forest: List[DRTree[Trace]] = buildForest(traces, edgesClickForest(traces))
    // SessionNode sucks
    val forestSessionNode: List[DRTree[SessionNode]] =
      forest.map(addRelPos(_, 0)) // fixed 0 is a problem

    val nodesNew = forestSessionNode.flatMap(_.nodes)
    val edgesNew = forestSessionNode
      .flatMap(_.edges)
      .map(diEdge => SessionEdge(diEdge.from.coordinate, diEdge.to.coordinate))

    SessionGraph(nodesNew,
      edgesNew,
      maxTime,
      minTime,
      forest.map(_.width).max,
      cookieVal)
  }

  def addRelPos(tree: DRTree[Trace], startPos: Float): DRTree[SessionNode] = {
    /* For each trace in the tree we compute a Coordinate.  [SessionNode(c: Coordinate, t: Trace)]
     * We will use the Coordinate in the fronted to render the tree.
     *
     * The x and the y axis have different scales.
     * - the x-axis value it determined by the timestamp of the trace
     * - the y-axis is calculated below using the width of the tree. The 0 of the y-axis is vertically centered
     *   and the root of a single tree forest has y-value 0. Each branch has distance of 1.
     *
     * The frontend rescales the coordinates to fit the svg.

    // Example: Imagine a tree with a root, two children A and B, A has a child C, B has children D and E.
      The nodes would be provided with y-values indicated in this diagram:

       -1          A----C
                  /
                 /
                /
       0    root      ----D
                \    /
       0.5       ---B
                    \
       1             --------E
     */
    val widths = tree.children.map(_.width)
    val upperY: Seq[Int] = widths
      .foldLeft(List(0))({ (b, i) =>
        b.head + 1 + i :: b
      })
      .reverse
    val startsRel = widths.zip(upperY).map(t => t._2 + t._1.toFloat / 2)
    val starts = startsRel.map(x => startPos - tree.width.toFloat / 2 + x)

    val children =
      tree.children.zip(starts).map({ case (c, s) => addRelPos(c, s) })

    DRTree(SessionNode(Coordinate(tree.node.timestamp, startPos), tree.node),
      children)
  }

  def edgesClickForest(traces: Seq[Trace]): List[DiEdge[Trace]] = {
    // order traces to an url descending by timestamp
    val urlToTraces: Map[String, Seq[Trace]] =
      traces.groupBy(_.url).mapValues(ts => ts.sorted.reverse) // sort descending

    val result: Seq[DiEdge[Trace]] = traces.flatMap({ trace =>
      urlToTraces
        .getOrElse(trace.referer, Seq.empty[Trace])
        .filter(_ < trace) // use implicit Ordering[Trace]
        // Now in the real world there is only one previous page; but we don't know which exactly.
        // Because different click paths can visit the same page.
        // Take the trace with latest timestamp
        // (descending sort above ensures it is the first one)
        .take(1)
        .map(DiEdge(_, trace))
    })
    result.toList
  }

}
