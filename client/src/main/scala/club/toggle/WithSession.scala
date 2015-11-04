package club.toggle

import scala.scalajs.js
import org.scalajs.dom

import co.technius.scalajs.mithril._

class WithSession(comp: Component)(implicit session: MithrilProp[Session]) extends Component {
  override val controller: js.Function = () => {
    if (session() == null) {
      dom.window.setTimeout(() => m.route("/", true), 0) // Workaround for bug
    } else {
      new Wrapper(comp)
    }
  }

  val view: js.Function = (ctrl: Any) => ctrl match {
    case wrapper: Wrapper =>
      wrapper.view.call(wrapper.ctrl, wrapper.ctrl, session())
    case _ =>
      m("div")
  }

  class Wrapper(comp: Component) {

    val ctrl = comp.controller.call(comp.controller, session())

    val view: js.Function = comp.view
  }
}

object WithSession {
  def apply(comp: Component)(implicit s: MithrilProp[Session]) =
    new WithSession(comp)
}
