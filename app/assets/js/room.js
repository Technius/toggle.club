var Room = {};

Room.controller = function(args) {
  var self = this;
  this.name = args.name();
  this.conn = args.conn();
  this.status = { title: "", users: {} };

  this.toggleReady = function() {
    Protocol(self.conn).updateReady(self.name, !self.status.users[self.name]);
  };

  this.conn.onmessage = function(e) {
    var msg = Protocol(this.conn).parse(e.data);
    self.handleMessage(msg);
    m.redraw();
  };

  this.handleMessage = function(msg) {
    console.info("Handling", msg);
    if (msg.$type == "RoomStatus") {
      console.info("Room update");
      self.status = msg;
    }
  };

  while (args.msgQueue.length > 0) {
    this.handleMessage(args.msgQueue.pop());
  }
  Protocol(this.conn).requestStatus();
};

Room.view = function(ctrl) {
  var readyCount = 0;
  var userNames = Object.keys(ctrl.status.users);
  var totalUsers = userNames.length;
  for (var i = 0; i < totalUsers; i++) {
    if (ctrl.status.users[userNames[i]]) readyCount++;
  }

  return m("div.pure-u-3-5", [
    m("div.room-heading", [
      m("span.room-title", ctrl.status.title),
      m("span.readystats", "(" + readyCount + "/" + totalUsers + " ready)"),
      m("span.username", "You are " + ctrl.name)
    ]),
    m("ul.hand-list", userNames.map(function(k) {
      var toggleBtn =
        k == ctrl.name
          ? m("button.pure-button", { onclick: ctrl.toggleReady }, "Toggle")
          : "";

      return m("li", [
        m("span", k),
        m("div.status.status-" + (ctrl.status.users[k] ? "ready" : "notready"), " "),
        toggleBtn
      ]);
    }))
  ])
};
