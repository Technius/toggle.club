package club.toggle

import co.technius.scalajs.mithril._
import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll
import scala.scalajs.js.Dynamic.{ literal => json }
import upickle.default._

import Protocol._

class LoginComponent(session: MithrilProp[Session]) extends Component {

  override val controller: js.Function = () => new Controller

  val view: js.Function = (ctrl: Controller) => {
    @inline def input(placeholder: String, prop: MithrilProp[String]) = {
      m("input[type=text]", json(
        placeholder = placeholder,
        onchange = m.withAttr("value", prop),
        value = prop()
      ))
    }

    val formAttr = json(onsubmit = openConn(ctrl) _)

    m("form.login-form.pure-form.pure-form-aligned.pure-u-3-5", formAttr, js.Array(
      m("div.pure-control-group", js.Array(
        m("label", "Room name"),
        input("Room name", ctrl.room)
      )),
      m("div.pure-control-group", js.Array(
        m("label", "Name"),
        input("Name", ctrl.name)
      )),
      m("div.pure-controls", js.Array(
        m("button.pure-button.pure-button-primary", "Connect")
      ))
    ))
  }

  @JSExportAll
  class Controller {
    val name = m.prop("")
    val room = m.prop("")
    val conn = m.prop[dom.WebSocket](null)
  }

  def openConn(ctrl: Controller)(e: dom.Event): Unit = {
    e.preventDefault()

    if (ctrl.conn() != null) {
      ctrl.conn().close()
      ctrl.conn(null)
    }

    val msgQueue = scala.collection.mutable.Queue[Message]()
    val name = ctrl.name()

    val params = s"room=${ctrl.room()}&name=$name"
    val protocol = if (dom.location.protocol == "http:") "ws:" else "wss:"
    val url = s"$protocol//${dom.location.host}/connect?$params"
    val ws = new dom.WebSocket(url)
    ws.onopen = (_: dom.Event) => {
      println("Connected")
      ctrl.conn(ws)
      m.redraw()
    }
    ws.onmessage = (e: dom.MessageEvent) => {
      val msg = read[Message](e.data.asInstanceOf[String])
      msgQueue += msg
      msg match {
        case s: RoomStatus =>
          session(Session(name, ws, msgQueue))
          m.route("/" + s.title)
        case Disconnected(msg) =>
          val dispMsg = "Disconnected by server: " + msg
          println(dispMsg)
          dom.window.alert(dispMsg)
        case _ =>
      }
      m.redraw()
    }
    ws.onclose = (_: dom.Event) => {
      println("Connection lost")
      ctrl.conn(null)
      m.redraw()
    }
  }
}
