var Login = {};

Login.controller = function(args) {
  this.name = args.name;
  this.room = m.prop("");
  this.openConn = function(room, name) {
    return function(e) {
      e.preventDefault();

      if (args.conn()) {
        args.conn().close();
        args.conn(null);
      }

      var protocol = location.protocol == "http:" ? "ws:" : "wss:"
      var params = "?room=" + room + "&name=" + name;
      var url = protocol + "//" + location.host + "/connect" + params;
      var ws = new WebSocket(url);
      ws.onopen = function() {
        console.info("Connected");
        args.conn(ws);
        m.redraw();
      };
      ws.onmessage = function(e) {
        args.msgQueue.push(Protocol(args.conn()).parse(e.data));
        m.redraw();
      };
      ws.onclose = function() {
        console.info("Connection lost");
        args.conn(null);
        m.redraw();
      };
    }
  };
};

Login.view = function(ctrl) {
  var formAttr = { onsubmit: ctrl.openConn(ctrl.room(), ctrl.name()) }
  return m("form.login-form.pure-form.pure-form-aligned.pure-u-3-5", formAttr, [
    m("p", "Join or create a room!"),
    m("div.pure-control-group", [
      m("label", "Room name"),
      m("input[type=text]", {
        placeholder: "Room name",
        onchange: m.withAttr("value", ctrl.room),
        value: ctrl.room()
      })
    ]),
    m("div.pure-control-group", [
      m("label", "Name"),
      m("input[type=text]", {
        placeholder: "Name",
        onchange: m.withAttr("value", ctrl.name),
        value: ctrl.name()
      }), 
    ]),
    m("div.pure-controls",
      m("button.pure-button.pure-button-primary", "Connect")
    )
  ]);
};
