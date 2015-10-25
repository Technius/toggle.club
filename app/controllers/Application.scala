package controllers

import akka.actor.ActorSystem
import javax.inject.Inject
import play.api._
import play.api.Play.current
import play.api.mvc._

import actors._

class Application @Inject() (implicit system: ActorSystem) extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  val managerActor = system.actorOf(RoomManager.props)

  def socket(room: String, name: String) = WebSocket.acceptWithActor[String, String] { _ => out =>
    UserActor.props(name, out, managerActor, room)
  }

}
