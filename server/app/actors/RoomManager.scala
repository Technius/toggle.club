package actors

import akka.actor.{ Actor, ActorRef, Props, Terminated }

import club.toggle.Protocol._
import RoomActor._

class RoomManager extends Actor {
  var rooms = Map[String, ActorRef]()

  def receive = {
    case msg @ JoinRoom(roomName, _) =>
      val (room, spawned) = rooms.get(roomName) match {
        case Some(r) => r -> false
        case None =>
          val r = context.actorOf(RoomActor.props(roomName))
          context.watch(r)
          rooms = rooms + (roomName -> r)
          r -> true
      }
      sender() ! room
      room forward msg
      if (spawned) room ! RoomActor.PromoteUser(sender())
    case Terminated(r) =>
      rooms = rooms.filterNot(_._2 == r)
  }
}

object RoomManager {
  def props: Props = Props(new RoomManager)
}
