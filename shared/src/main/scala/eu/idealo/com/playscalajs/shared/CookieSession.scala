package eu.idealo.com.playscalajs.shared

import java.time._
import java.util.Date
import java.text.SimpleDateFormat

object CookieSession {

  type MilliSeconds = Long

  val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  def renderEpochMillis(millis: MilliSeconds): String =
    // LocalDateTime.now(Clock.fixed(Instant.ofEpochMilli(millis), ZoneId.of("Europe/Berlin"))).toString
    format.format(new Date(millis))

  sealed trait Trace extends Ordered[Trace] {
    def url: String
    def referer: String
    def timestamp: Long

    lazy val displayTimestamp: String = renderEpochMillis(timestamp)

    def compare(that: Trace): Int = {
      val diff = this.timestamp - that.timestamp
      if (diff < 0) {
        -1
      } else if (diff > 0) {
        1
      } else 0
    }
  }

  case class SimpleTrace(timestamp: Long, referer: String, url: String) extends Trace {
    // here timestamp are epoch seconds :(
    override lazy val displayTimestamp: String = renderEpochMillis(timestamp)
  }

  case class Coordinate(x: Double, y: Double)
  case class SessionNode(coordinate: Coordinate, data: Trace)
  case class SessionEdge(start: Coordinate, end: Coordinate)
  case class SessionGraph(nodes: Seq[SessionNode],
                          edges: Seq[SessionEdge],
                          maxTime: Long,
                          minTime: Long,
                          treeWidth: Float,
                          cookie: String)

  /*
   */
  case class TraceGeneral(country: String, media: String, siteId: String, trackerSite: String)
  case class TraceTracker(createdByHost: String)
  case class TraceWebApp(isgData: TraceIsgData, page: Option[TracePage], tests: List[TraceTest])
  case class TraceIsgData(ipAddress: String, ipcVisitor: String, url: String, referer: String, requestMethod: String, responseStatus: String)
  case class TracePage(scriptName: String, template: String, `type`: String)
  case class TraceTest(name: String, variant: String)
  case class TraceContent(general: TraceGeneral, tracker: TraceTracker, webApp: TraceWebApp)

  case class IdealoTrace(requestTime: Long, startTime: Long, endTime: Long, content: TraceContent) extends Trace {
    lazy val url: String = content.webApp.isgData.url

    lazy val timestamp: MilliSeconds = requestTime / 1000

    lazy val referer: String = content.webApp.isgData.referer
  }
}
