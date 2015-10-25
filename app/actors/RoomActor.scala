package actors

import akka.actor.{ Actor, ActorRef, PoisonPill, Props, Terminated }

import RoomActor._
import Protocol._

class RoomActor(title: String) extends Actor {
  var users = Map[String, UserState]()

  @inline def roomStatus(users: Map[String, UserState]): RoomStatus =
    RoomStatus(title, users.mapValues(_.ready))

  @inline def broadcast(msg: Message): Unit =
    users.foreach(_._2.ref ! msg)

  def receive = {
    case JoinRoom(roomName, name) if roomName == title =>
      users.get(name) match {
        case Some(_) =>
        case None =>
          context.watch(sender())
          val newUsers = users + (name -> UserState(name, sender(), false))
          broadcast(roomStatus(newUsers))
          users = newUsers
      }
    case Terminated(ref) =>
      users = users.filterNot(_._2.ref == ref)
      broadcast(roomStatus(users))
      if (users.size == 0) {
        self ! PoisonPill
      }
    case ChangeReady(name, ready) =>
      users.get(name) match {
        case Some(u) =>
          users = users collect {
            case (n, u) if n == name => n -> u.copy(ready = ready)
            case kp => kp
          }
          broadcast(roomStatus(users))
        case None =>
      }
    case UnreadyAll =>
      users = users.mapValues(_.copy(ready = false))
    case RequestStatus => sender() ! roomStatus(users)
  }
}

object RoomActor {
  def props(title: String): Props = Props(new RoomActor(title))

  case class UserState(name: String, ref: ActorRef, ready: Boolean = false)
}
