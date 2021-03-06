package club.toggle

import co.technius.scalajs.mithril._
import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExportAll
import scala.scalajs.js.Dynamic.{ literal => json }
import upickle.default._

import Protocol._

object RoomComponent extends Component {

  override val controller: js.Function = (args: Session) => {
    new Controller(args)
  }

  val view: js.Function = (ctrl: Controller, args: Session) => {
    val ws = args.conn
    val readyCount = ctrl.status.users.values.count(_ == true)
    val totalUsers = ctrl.status.users.size
    val isMod = isModerator(args.name, ctrl.status.moderators)

    val lockIndicator = {
      val settings =
        if (isMod)
          json(
            style = json(cursor = "pointer"),
            onclick = ws.toggleRoomLock(ctrl.status)
          )
        else json()
      m(s"i.fa.fa-${if(ctrl.status.locked) "" else "un"}lock", settings)
    }

    val modControls =
      if (isMod)
        js.Array(
          m("span", "(moderator)"),
          m("button.pure-button", json(onclick = () => ws.send(UnreadyAll)), "Unready All")
        ).map(e => m("li", e))
      else js.Array[String]()

    val handList =
      m("ul.hand-list", (ctrl.status.users map { case (name, state) =>
        @inline def kickBtn =
          m("button.pure-button", json(onclick = ws.kickUser(name)), "Kick")
        @inline def toggleBtn =
          m("button.pure-button", json(onclick = ws.toggleReady(name, !state.ready)), "Toggle")
        @inline def statusTextEditBtn =
          m("i.fa.fa-pencil.status-text-btn", json(
            style = json(cursor = "pointer"),
            onclick = () => {
              // TODO: Use an editable
              val s = dom.window.prompt("Enter your status", state.status)
              if (s != null) ws.send(ChangeStatus(name, s))
            }
          ))

        val statusText = m("span", js.Array(
          m("span.status-text", if (state.status.isEmpty) "(status not set)" else state.status),
          if (args.name == name) statusTextEditBtn else ""
        ))
        
        m("li", js.Array(
          m("span", name),
          m(s"div.status.status-${if(state.ready) "" else "not"}ready"),
          statusText,
          if (isMod && args.name != name) kickBtn else "",
          if (args.name == name) toggleBtn else ""
        ))
      }).toJSArray)

    m("div.pure-u-3-5", js.Array(
      m("div.room-heading", js.Array(
        m("span.room-title", ctrl.status.title),
        lockIndicator,
        m("span.readystatus", s"($readyCount/$totalUsers ready)"),
        m("span.username", s"You are ${args.name}")
      )),
      m("ul.room-controls", js.Array() ++ modControls),
      handList
    ))
  }

  @JSExportAll
  class Controller(args: Session) {
    var status = RoomStatus(
      title = "",
      users = Map.empty,
      moderators = Seq.empty,
      locked = false
    )

    args.conn.onmessage = (e: dom.MessageEvent) => {
      val msg = read[Message](e.data.asInstanceOf[String])
      handleMessage(msg)
      m.redraw()
    }

    private[this] def handleMessage(msg: Message) = msg match {
      case s: RoomStatus =>
        println("Room update")
        status = s
      case Disconnected(msg) =>
        val dispMsg = "Disconnected by server: " + msg
        println(dispMsg)
        dom.window.alert(dispMsg)
      case _ =>
    }

    while (!args.msgQueue.isEmpty) {
      handleMessage(args.msgQueue.dequeue())
    }
    if (status.users.isEmpty) args.conn.send(RequestStatus)
  }

  @inline implicit class SendFunctions(val ws: dom.WebSocket) extends AnyVal {

    @inline def send(msg: Message) =
      ws.send(write(msg))

    @inline def toggleRoomLock(status: RoomStatus) =
      () => send(ChangeRoomLock(status.title, !status.locked))

    @inline def kickUser(name: String) =
      () => send(KickUser(name))

    @inline def toggleReady(selfName: String, ready: Boolean) =
      () => send(ChangeReady(selfName, ready))

    @inline def updateStatus(selfName: String, status: String) =
      () => send(ChangeStatus(selfName, status))
  }

  @inline def isModerator(name: String, mods: Seq[String]): Boolean =
    mods.exists(_ == name)
}
