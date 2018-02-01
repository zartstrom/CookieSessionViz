package eu.idealo.com.playscalajs.controllers

import javax.inject._

import eu.idealo.com.playscalajs.data.Storage._
import play.api.mvc._

@Singleton
class Application @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def index = Action {
    Ok(views.html.index("it did work for sure"))
  }

  def sessions = Action {
    Ok(sessionsSerial)
  }

}
