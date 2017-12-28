package com.example.playscalajs.controllers

import javax.inject._
import play.api.mvc._

import com.example.playscalajs.data.Storage._
import com.example.playscalajs.shared.SharedMessages

@Singleton
class Application @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def index = Action {
    Ok(views.html.index(SharedMessages.itWorks))
  }

  def sessions = Action {
    Ok(sessionsSerial)
  }

}
