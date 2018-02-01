package org.markgrafendamm.playscalajs.shared

object CookieSession {

  sealed trait Trace extends Ordered[Trace] {
    def url: String
    def referer: String
    def timestamp: Int
    def comparableTimestamp: Int // this is probably not the best idea
    def compare(that: Trace): Int = this.comparableTimestamp - that.comparableTimestamp
  }

  case class SimpleTrace(timestamp: Int, referer: String, url: String) extends Trace {
    val comparableTimestamp = timestamp
  }

  case class Coordinate(x: Float, y: Float)
  case class SessionNode(coordinate: Coordinate, data: Trace)
  case class SessionEdge(start: Coordinate, end: Coordinate)
  case class SessionGraph(nodes: Seq[SessionNode],
                          edges: Seq[SessionEdge],
                          maxTime: Int,
                          minTime: Int,
                          treeWidth: Float,
                          cookie: String)

}
