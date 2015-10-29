var Protocol = function(ws) {
  var send = function(json) {
    ws.send(JSON.stringify(json));
  };

  var buildMsg = function($type, data) {
    var msg = { $type: "actors.Protocol." + $type };
    for (k in data) msg[k] = data[k];
    return msg;
  }

  return {
    parse: function(string) {
      var obj = JSON.parse(string);
      obj.$type = obj.$type.substring(obj.$type.lastIndexOf(".") + 1);
      return obj;
    },
    send: send,
    requestStatus: function() {
      send(buildMsg("RequestStatus"));
    },
    updateReady: function(name, newStatus) {
      send(buildMsg("ChangeReady", { name: name, ready: newStatus }));
    },
    unreadyAll: function() {
      send(buildMsg("UnreadyAll"));
    },
    kickUser: function(name) {
      send(buildMsg("KickUser", { name: name }));
    },
    changeRoomLock: function(room, locked) {
      send(buildMsg("ChangeRoomLock", { room: room, locked: locked }));
    }
  };
};
