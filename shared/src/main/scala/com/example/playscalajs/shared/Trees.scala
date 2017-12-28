package com.example.playscalajs.shared

import CookieSession._
import scala.collection.immutable.Seq

case class Coordinate(x: Float, y: Float)
case class SessionNode(coordinate: Coordinate, data: Trace)
case class SessionEdge(start: Coordinate, end: Coordinate)
case class SessionGraph(nodes: Seq[SessionNode],
                        edges: Seq[SessionEdge],
                        maxTime: Int,
                        minTime: Int,
                        treeWidth: Float)

object Trees {
  case class TreeT(children: List[TreeT], trace: Trace)
  case class TreeH(width: Int, children: List[TreeH], trace: Trace)
  case class TreeR(positionY: Float,
                   width: Int,
                   children: List[TreeR],
                   trace: Trace)

  object TreeT {
    def addTrace(tree: TreeT, trace: Trace): TreeT = {
      if (tree.trace.url == trace.referer) {
        TreeT(TreeT(List(), trace) :: tree.children, tree.trace)
      } else {
        val childrenNew = tree.children.map({ c =>
          addTrace(c, trace)
        })
        TreeT(childrenNew, tree.trace)
      }
    }
  }

  def sessionNodes(treeR: TreeR): List[SessionNode] = {
    val fromChildren = treeR.children.flatMap(sessionNodes)
    val x = treeR.trace.timestamp
    val y = treeR.positionY
    SessionNode(Coordinate(x, y), treeR.trace) :: fromChildren
  }

  def sessionEdges(tree: TreeR): List[SessionEdge] = {
    val childrenLines = tree.children.flatMap(sessionEdges)
    val myLines = tree.children.map({ c =>
      SessionEdge(Coordinate(tree.trace.timestamp, tree.positionY),
                  Coordinate(c.trace.timestamp, c.positionY))
    })
    (myLines ++ childrenLines).toList
  }

  def buildSessionGraph(traces: Seq[Trace]): SessionGraph = {
    val maxTime = traces.map(x => x.timestamp).max
    val minTime = traces.map(x => x.timestamp).min

    val treeR = addPosition(addWidth(buildTree(traces)), 0)
    val nodes = sessionNodes(treeR)
    val edges = sessionEdges(treeR)

    SessionGraph(nodes, edges, maxTime, minTime, treeR.width)
  }

  def buildTree(traces: Seq[Trace]): TreeT = {
    // creating a tree / forest from traces is most essential
    // this implementation does only create one tree -> improve it!
    val urlToTrace: Map[String, Trace] = traces.map(t => (t.url, t)).toMap
    val refToTrace: Map[String, Trace] = traces.map(t => (t.referer, t)).toMap

    val root = traces.head
    val others = traces.tail

    others.foldLeft(TreeT(List(), root))({ (th, t) =>
      TreeT.addTrace(th, t)
    })
  }

  def width(cs: List[TreeH]): Int = {
    val s1 = math.max(cs.length - 1, 0)
    val s2 = cs.map(c => c.width).sum
    s1 + s2
  }

  def addWidth(tt: TreeT): TreeH = {
    val childrenH = tt.children.map(c => addWidth(c))
    TreeH(width(childrenH), childrenH, tt.trace)
  }

  def addPosition(h: TreeH, start: Float): TreeR = {
    val widths: Seq[Int] = h.children.map(_.width)
    val upperY: Seq[Int] = widths
      .foldLeft(List(0))({ (b, i) =>
        b.head + 1 + i :: b
      })
      .reverse
    val startsRel = widths.zip(upperY).map(t => t._2 + t._1.toFloat / 2)
    val starts = startsRel.map(x => start - h.width.toFloat / 2 + x)

    val children =
      h.children.zip(starts).map({ case (c, s) => addPosition(c, s) })

    TreeR(start, h.width, children, h.trace)
  }

  val e = TreeT(List(), Trace(5, "b", "e"))
  val d = TreeT(List(), Trace(5, "b", "d"))
  val c = TreeT(List(), Trace(5, "a", "c"))
  val b = TreeT(List(d, e), Trace(5, "a", "b"))
  val a = TreeT(List(b, c), Trace(5, "gogol", "a"))

  val th = addWidth(a)
  val tr = addPosition(th, 0)
}
