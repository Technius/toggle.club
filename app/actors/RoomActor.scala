package actors

import akka.actor.{ Actor, ActorRef, PoisonPill, Props, Terminated }

import RoomActor._
import Protocol._

class RoomActor(title: String) extends Actor {
  var users = Map[String, UserState]()
  var moderators = Seq[String]()

  @inline def roomStatus(users: Map[String, UserState]): RoomStatus =
    RoomStatus(title, users.mapValues(_.ready), moderators)

  @inline def broadcast(msg: Message): Unit =
    users.foreach(_._2.ref ! msg)

  @inline def isModerator(r: ActorRef): Boolean =
    moderators.map(users(_).ref).contains(r)

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
    case UnreadyAll if isModerator(sender()) =>
      users = users.mapValues(_.copy(ready = false))
      broadcast(roomStatus(users))
    case RequestStatus => sender() ! roomStatus(users)
    case PromoteUser(ref) =>
      users.find(_._2.ref == ref) match {
        case Some((name, _)) =>
          moderators = moderators :+ name
          broadcast(roomStatus(users))
        case None =>
      }
    case KickUser(name) if isModerator(sender()) =>
      users find (_._1 == name) match {
        case Some((n, d)) if d.ref != sender() =>
          users = users - name
          broadcast(roomStatus(users))
          d.ref ! Disconnected("You have been kicked out of the room.")
        case None =>
      }
  }
}

object RoomActor {
  def props(title: String): Props = Props(new RoomActor(title))

  case class UserState(name: String, ref: ActorRef, ready: Boolean = false)

  case class PromoteUser(ref: ActorRef)
}
