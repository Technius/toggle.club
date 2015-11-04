package club.toggle

import co.technius.scalajs.mithril._
import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.annotation._

object Main extends js.JSApp {
  def main(): Unit = {
    println("Booting up")

    implicit val session: MithrilProp[Session] = m.prop(null)

    val routes = Map[String, MithrilComponent](
      "/:room" -> WithSession(RoomComponent),
      "/" -> new LoginComponent(session)
    ).mapValues(padComponent _)

    m.route.mode = "pathname"
    m.route(dom.document.getElementById("app"), "/")(routes.toSeq: _*)
  }

  def padComponent(c: MithrilComponent): MithrilComponent =
    new ComponentPadding(c)

  class ComponentPadding(underlying: MithrilComponent) extends Component {
    override val view: js.Function = () => js.Array(
      m("div.pure-u-1-5"),
      m.component(underlying),
      m("div.pure-u-1-5")
    )
  }
}

@ScalaJSDefined
trait Session extends js.Object {
  def name: String
  def conn: dom.WebSocket
  def msgQueue: collection.mutable.Queue[Protocol.Message]
}

object Session {
  def apply(_name: String,
            _conn: dom.WebSocket,
            _msgQueue: collection.mutable.Queue[Protocol.Message]): Session = {
    js.use(new js.Object {
      val name = _name
      val conn = _conn
      val msgQueue = _msgQueue
    }).as[Session]
  }
}
