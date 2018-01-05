package com.example.playscalajs.shared

import CookieSession._

import scala.collection.immutable.Seq
import scala.collection.mutable

case class Coordinate(x: Float, y: Float)
case class SessionNode(coordinate: Coordinate, data: Trace)
case class SessionEdge(start: Coordinate, end: Coordinate)
case class SessionGraph(nodes: Seq[SessionNode],
                        edges: Seq[SessionEdge],
                        maxTime: Int,
                        minTime: Int,
                        treeWidth: Float,
                        cookie: String)

case class TraceAndWidth(trace: Trace, width: Int)

object Forest {
  case class DiEdge[N](from: N, to: N)

  // DRTree = Directed Rooted Tree
  case class DRTree[N](node: N, children: List[DRTree[N]]) {
    def map[M](f: N => M): DRTree[M] = {
      DRTree(f(node), children.map(_.map(f)))
    }

    lazy val height: Int = {
      children match {
        case Nil => 0
        case cs  => 1 + cs.map(_.height).max
      }
    }

    lazy val width: Int = math.max(0, children.length - 1) + children
      .map(_.width)
      .sum

    def twoStepMap[M](f: (N, List[DRTree[M]]) => M): DRTree[M] = {
      val childrenM = children.map(_.twoStepMap(f))
      DRTree(f(node, childrenM), childrenM)
    }

    def nodes: List[N] = {
      node :: children.flatMap(_.nodes)
    }

    def edges: List[DiEdge[N]] = {
      children.map(c => DiEdge(node, c.node)) ++ children.flatMap(_.edges)
    }

    def addTreeByEdge(that: DRTree[N], edge: DiEdge[N]): DRTree[N] = {
      // TODO: beware of multiple appends to the tree. I.e. if more than one child adds the tree via the edge.
      if (node == edge.from) {
        DRTree(node, that :: children)
      } else {
        DRTree(node, children.map(_.addTreeByEdge(that, edge)))
      }
    }
  }

  object DRTree {
    def singleton[N](node: N): DRTree[N] = DRTree(node, Nil)
    def build[N](nodes: List[N], edges: List[DiEdge[N]]): DRTree[N] = {
      // assume for now nodes and edges form a proper rooted directed tree
      val disjointSet: DisjointSet[N] = DisjointSet(nodes: _*)

      val nodeToTree = mutable.Map.empty[N, DRTree[N]]

      edges.foreach({ e: DiEdge[N] =>
        val startRoot = disjointSet(e.from)
        val hiTree = nodeToTree.getOrElse(startRoot, singleton(startRoot))
        // there must be e.end == disjointSet(e.end) because of assumption we have a directed rooted tree
        val loTree = nodeToTree.getOrElse(e.to, singleton(e.to))

        nodeToTree(startRoot) = hiTree.addTreeByEdge(loTree, e)

        disjointSet.unionInOrder(e.from, e.to)
      })
      val bigRoot = disjointSet.roots().head // there is exactly one because we have a rooted directed tree
      nodeToTree(bigRoot)
    }
  }

  def buildForest[N](nodes: Seq[N], edges: Seq[DiEdge[N]]): List[DRTree[N]] = {
    // Create a list of trees from given edges.
    // Uses a disjoint set for help.
    // val nodes: List[N] = edges.flatMap(e => List(e.from, e.to)).distinct
    require(nodes.length > edges.length)
    require(edges.map(e => e.from != e.to).forall(identity))
    val disjointSet: DisjointSet[N] = DisjointSet(nodes: _*)

    val nodesToTrees = edges.foldLeft(Map.empty[N, DRTree[N]])({ (m, e) =>
      val rootOfStart = disjointSet(e.from)
      val hiTree = m.getOrElse(rootOfStart, DRTree.singleton(rootOfStart))
      // there must be e.end == disjointSet(e.end) because of assumption we have a directed rooted tree
      val loTree = m.getOrElse(e.to, DRTree.singleton(e.to))

      disjointSet.unionInOrder(e.from, e.to)
      val extendedTree = hiTree.addTreeByEdge(loTree, e)

      m ++ Map(rootOfStart -> extendedTree)
    })

    // trees made of a single node don't have an entry in nodesToTrees yet -> use .getOrElse and singleton
    val trees = disjointSet.roots.map({ t => nodesToTrees.getOrElse(t, DRTree.singleton(t))}).toList
    assert(nodes.toSet == trees.flatMap(_.nodes).toSet)
    assert(nodes.length == trees.map(_.nodes.length).sum)
    assert(nodes.length == edges.length + trees.length)
    trees
  }

  def sessionData(traces: Seq[Trace], cookieVal: String): SessionGraph = {
    // collect all data that is sent to the frontend
    val maxTime = traces.map(x => x.timestamp).max
    val minTime = traces.map(x => x.timestamp).min

    val forest: List[DRTree[Trace]] = buildForest(traces, edgesClickTree(traces))
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

  def edgesClickTree(traces: Seq[Trace]): List[DiEdge[Trace]] = {
    // order traces to an url descending by timestamp
    val urlToTraces: Map[String, Seq[Trace]] =
      traces.groupBy(_.url).mapValues(ts => ts.sortBy(t => -t.timestamp))

    val result: Seq[DiEdge[Trace]] = traces.flatMap({ trace =>
      urlToTraces
        .getOrElse(trace.referer, Seq.empty[Trace])
        .filter(_.timestamp < trace.timestamp)
        .take(1) // there can be only one previous page; take the trace with latest timestamp (see sortBy above)
        .map(DiEdge(_, trace))
    })
    result.toList
  }

}
