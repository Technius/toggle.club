package club.toggle

import co.technius.scalajs.mithril._
import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll
import scala.scalajs.js.Dynamic.{ literal => json }
import upickle.default._

import Protocol._

object LoginComponent extends Component {

  override val controller: js.Function = () => new Controller

  val view: js.Function = (ctrl: Controller, args: Session) => {
    @inline def input(placeholder: String, prop: MithrilProp[String]) = {
      m("input[type=text]", json(
        placeholder = placeholder,
        onchange = m.withAttr("value", prop),
        value = prop()
      ))
    }

    val formAttr = json(onsubmit = openConn(ctrl, args) _)

    m("form.login-form.pure-form.pure-form-aligned.pure-u-3-5", formAttr, js.Array(
      m("div.pure-control-group", js.Array(
        m("label", "Room name"),
        input("Room name", ctrl.room)
      )),
      m("div.pure-control-group", js.Array(
        m("label", "Name"),
        input("Name", args.name)
      )),
      m("div.pure-controls", js.Array(
        m("button.pure-button.pure-button-primary", "Connect")
      ))
    ))
  }

  @JSExportAll
  class Controller {
    val room = m.prop("")
  }

  def openConn(ctrl: Controller, args: Session)(e: dom.Event): Unit = {
    e.preventDefault()

    if (args.conn() != null) {
      args.conn().close()
      args.conn(null)
    }

    val params = s"room=${ctrl.room()}&name=${args.name()}"
    val protocol = if (dom.location.protocol == "http:") "ws:" else "wss:"
    val url = s"$protocol//${dom.location.host}/connect?$params"
    val ws = new dom.WebSocket(url)
    ws.onopen = (_: dom.Event) => {
      println("Connected")
      args.conn(ws)
      m.redraw()
    }
    ws.onmessage = (e: dom.MessageEvent) => {
      args.msgQueue += read[Message](e.data.asInstanceOf[String])
      m.redraw()
    }
    ws.onclose = (_: dom.Event) => {
      println("Connection lost")
      args.conn(null)
      m.redraw()
    }
  }
}
