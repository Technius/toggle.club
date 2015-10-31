package actors

import akka.actor.{ Actor, ActorRef, PoisonPill, Props }
import scala.util.{ Failure, Try, Success }
import upickle.default._

import club.toggle.Protocol._
import UserActor._

class UserActor(name: String, out: ActorRef, manager: ActorRef, roomName: String) extends Actor {
  override def preStart(): Unit = {
    manager ! JoinRoom(roomName, name)
  }

  def receive = beforeRoom

  def beforeRoom: Receive = {
    case r: ActorRef => context.become(handle(r))
  }
  
  def handle(room: ActorRef): Receive = {
    case msg: Disconnected =>
      out ! write(msg)
      self ! PoisonPill
    case msg: Message => out ! write(msg)
    case msg: String =>
      Try(read[Message](msg)) match {
        case Success(m: Disconnected) =>
        case Success(m) => processMessage(room)(m)
        case Failure(_) =>
      }
  }

  def processMessage(room: ActorRef): Receive = {
    case m: ChangeReady if m.name == name => room ! m
    case RequestStatus => room ! RequestStatus
    case UnreadyAll => room ! UnreadyAll
    case m: KickUser => room ! m
    case m: ChangeRoomLock => room ! m
    case _ =>
  }
}

object UserActor {
  object Parsing {
    val ready = "ready:(true|false)".r
  }

  def props(name: String, out: ActorRef, manager: ActorRef, roomName: String): Props = {
    Props(new UserActor(name, out, manager, roomName: String))
  }
}
