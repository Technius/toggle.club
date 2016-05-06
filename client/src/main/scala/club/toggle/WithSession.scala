package club.toggle

import scala.scalajs.js
import org.scalajs.dom

import co.technius.scalajs.mithril._

class WithSession(comp: Component)(implicit session: MithrilProp[Session]) extends Component {
  override val controller: js.Function = () => {
    if (session() == null) {
      dom.window.setTimeout(() => m.route("/", true), 0) // Workaround for bug
    }
  }

  val view: js.Function = () => {
    val s = session()

    if (s != null)
      m.component(comp, s)
    else
      m("div")
  }
}

object WithSession {
  def apply(comp: Component)(implicit s: MithrilProp[Session]) =
    new WithSession(comp)
}
