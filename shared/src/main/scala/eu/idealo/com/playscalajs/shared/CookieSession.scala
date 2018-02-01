package eu.idealo.com.playscalajs.shared

import com.github.nscala_time.time.Imports._

object CookieSession {

  sealed trait Trace extends Ordered[Trace] {
    def url: String
    def referer: String
    def timestamp: Int
    def displayTimestamp: String
    def compare(that: Trace): Int = this.timestamp - that.timestamp
  }

  case class SimpleTrace(timestamp: Int, referer: String, url: String) extends Trace {
    val displayTimestamp: String = timestamp.toString
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

  /*
   */
  case class General(country: String, media: String, siteId: String, trackerSite: String)
  case class Tracker(createdByHost: String)
  case class WebApp(isgData: IsgData, page: Option[Page], tests: List[Test])
  case class IsgData(ipAddress: String, ipcVisitor: String, url: String, referer: String, requestMethod: String, responseStatus: String)
  case class Page(scriptName: String, template: String, `type`: String)
  case class Test(name: String, variant: String)
  case class Content(general: General, tracker: Tracker, webApp: WebApp)

  case class IdealoTrace(requestTime: Long, startTime: Long, endTime: Long, content: Content) extends Trace {
    lazy val url: String = content.webApp.isgData.url

    lazy val timestamp: Int = (requestTime / 1000000).toInt // epoch seconds

    val format = "yyyy-MM-dd HH:mm:ss"
    lazy val displayTimestamp: String = (requestTime / 1000).toDateTime.toString(format)

    lazy val referer: String = content.webApp.isgData.referer
  }
}
