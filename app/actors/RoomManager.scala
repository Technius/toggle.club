package actors

import akka.actor.{ Actor, ActorRef, Props, Terminated }

import RoomActor._
import Protocol._

class RoomManager extends Actor {
  var rooms = Map[String, ActorRef]()

  def receive = {
    case msg @ JoinRoom(roomName, _) =>
      val room = rooms.get(roomName) match {
        case Some(r) => r
        case None =>
          val r = context.actorOf(RoomActor.props(roomName))
          context.watch(r)
          rooms = rooms + (roomName -> r)
          r
      }
      sender() ! room
      room forward msg
    case Terminated(r) =>
      rooms = rooms.filterNot(_._2 == r)
  }
}

object RoomManager {
  def props: Props = Props(new RoomManager)
}
