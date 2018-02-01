package eu.idealo.com.playscalajs.shared

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
    def url: String = content.webApp.isgData.url

    override def timestamp: Int = (requestTime / 1000000).toInt

    override def comparableTimestamp: Int = timestamp

    override def referer: String = content.webApp.isgData.referer
  }
}
