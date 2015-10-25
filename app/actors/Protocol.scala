package actors

object Protocol {
  sealed trait Message

  case object CreateRoom extends Message

  case class JoinRoom(room: String, user: String) extends Message

  case object LeaveRoom extends Message

  case class ChangeReady(name: String, ready: Boolean) extends Message

  case object UnreadyAll extends Message

  case class RoomStatus(title: String, users: Map[String, Boolean]) extends Message

  case object RequestStatus extends Message
}
