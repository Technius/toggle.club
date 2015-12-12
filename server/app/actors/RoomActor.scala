package actors

import akka.actor.{ Actor, ActorRef, PoisonPill, Props, Terminated }

import club.toggle.Protocol._
import club.toggle.UserState
import RoomActor._

class RoomActor(title: String) extends Actor {
  var users = Map[String, UserRef]()
  var moderators = Seq[String]()
  var locked = false

  @inline def roomStatus(users: Map[String, UserRef]): RoomStatus =
    RoomStatus(title, users.mapValues(_.state), moderators, locked)

  @inline def broadcast(msg: Message): Unit =
    users.foreach(_._2.ref ! msg)

  @inline def isModerator(r: ActorRef): Boolean =
    moderators.map(users(_).ref).contains(r)

  def receive = {
    case JoinRoom(roomName, name) if roomName == title =>
      if (locked) {
        sender() ! Disconnected("This room is locked.")
      } else {
        users.get(name) match {
          case Some(_) =>
            sender () ! Disconnected("A user with this name has already joined")
          case None =>
            context.watch(sender())
            val newUsers = users + (name -> UserRef(name, sender()))
            broadcast(roomStatus(newUsers))
            users = newUsers
        }
      }
    case Terminated(ref) =>
      users = users.filterNot(_._2.ref == ref)
      broadcast(roomStatus(users))
      if (users.size == 0) {
        self ! PoisonPill
      }
    case ChangeReady(name, ready) =>
      updateAndBroadcast(name)(_.copy(ready = ready))
    case ChangeStatus(name, status) =>
      updateAndBroadcast(name)(_.copy(status = status))
    case UnreadyAll if isModerator(sender()) =>
      users = users.mapValues(r => r.copy(state = r.state.copy(ready = false)))
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
    case ChangeRoomLock(rn, l) if rn == title && isModerator(sender()) =>
      locked = l
      broadcast(roomStatus(users))
  }

  def updateAndBroadcast(name: String)(f: UserState => UserState) = {
    users.get(name) match {
      case Some(r) =>
        users = users collect {
          case (n, r) if n == name =>
            n -> r.copy(state = f(r.state))
          case kp => kp
        }
        broadcast(roomStatus(users))
      case None =>
    }
  }
}

object RoomActor {
  def props(title: String): Props = Props(new RoomActor(title))

  case class UserRef(
      name: String,
      ref: ActorRef,
      state: UserState = UserState(false))

  case class PromoteUser(ref: ActorRef)
}
