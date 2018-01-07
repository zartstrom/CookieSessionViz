package org.markgrafendamm.playscalajs.shared

object CookieSession {

  case class Trace(timestamp: Int, referer: String, url: String) extends Ordered[Trace] {
    def compare(that: Trace) = this.timestamp - that.timestamp
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


  type Session = List[Trace]

}
