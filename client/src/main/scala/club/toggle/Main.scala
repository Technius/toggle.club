package club.toggle

import co.technius.scalajs.mithril._
import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.annotation._

object Main extends js.JSApp {
  def main(): Unit = {
    println("Booting up")
    m.mount(dom.document.getElementById("app"), ToggleClubComponent)
  }
}

object ToggleClubComponent extends Component {

  override val controller: js.Function = () => new Controller

  val view: js.Function = (ctrl: Controller) => {
    js.Array(
      m("div.pure-u-1-5"),
      if (ctrl.conn() == null) m.component(LoginComponent, ctrl.compArgs) else "",
      if (ctrl.conn() != null) m.component(RoomComponent, ctrl.compArgs) else ""
    )
  }

  @JSExportAll
  class Controller {
    val name = m.prop("")
    val conn: MithrilProp[dom.WebSocket] = m.prop(null)
    val msgQueue = collection.mutable.Queue.empty[Protocol.Message]

    val compArgs = js.use(new js.Object {
      val name = Controller.this.name
      val conn = Controller.this.conn
      val msgQueue = Controller.this.msgQueue
    }).as[LoggedInArgs]
  }
}

@ScalaJSDefined
trait LoggedInArgs extends js.Object {
  def name: MithrilProp[String]
  def conn: MithrilProp[dom.WebSocket]
  def msgQueue: collection.mutable.Queue[Protocol.Message]
}
