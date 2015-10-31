package club.toggle

import co.technius.scalajs.mithril._
import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

object Main extends js.JSApp {
  def main(): Unit = {
    println("Booting up")
    m.mount(dom.document.getElementById("app"), ToggleClubComponent)
  }
}

object ToggleClubComponent extends Component {

  override val controller: js.Function = () => new Controller

  val view: js.Function = (ctrl: Controller) => {
    val Login = js.Dynamic.global.Login.asInstanceOf[MithrilComponent]
    val Room = js.Dynamic.global.Room.asInstanceOf[MithrilComponent]

    js.Array(
      m("div.pure-u-1-5"),
      if (ctrl.conn() == null) m.component(Login, ctrl.compArgs) else "",
      if (ctrl.conn() != null) m.component(Room, ctrl.compArgs) else ""
    )
  }

  @JSExportAll
  class Controller {
    val name = m.prop("")
    val conn: MithrilProp[dom.WebSocket] = m.prop(null)
    val msgQueue = js.Array[js.Object]()

    val compArgs = js.Dynamic.literal(
      name = name,
      conn = conn,
      msgQueue = msgQueue
    ).asInstanceOf[LoggedInArgs]

    js.Dynamic.global.m.redraw.strategy("diff")
  }
}

@js.native
trait LoggedInArgs extends js.Object {
  def name: MithrilProp[String]
  def conn: MithrilProp[dom.WebSocket]
  def msgQueue: js.Array[js.Object]
}
