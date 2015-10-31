var ToggleClub = {};

ToggleClub.controller = function() {
  this.name = m.prop("");
  this.conn = m.prop(null);
  this.msgQueue = [];
  this.compArgs = {
    name: this.name,
    conn: this.conn,
    msgQueue: this.msgQueue
  };
};

ToggleClub.view = function(ctrl) {
  return [
    m("div.pure-u-1-5"),
    ctrl.conn() ? "" : m.component(Login, ctrl.compArgs),
    ctrl.conn() ? m.component(Room, ctrl.compArgs) : "",
    m("div.pure-u-1-5")
  ];
};

// m.mount(document.getElementById("app"), ToggleClub);
