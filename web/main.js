var canvas = document.getElementById("canvas");
var ctx = canvas.getContext("2d");
var players = {};
var boxes = {};
var uuid = 0;
var Keys = {
	up: false,
	down: false,
	left: false,
	right: false
};

//Change the ip to local host when testing
var jacob = 'ws://192.168.197.181:25565';
var james = 'ws://192.168.215.57:25565';
var socket = new WebSocket(jacob);

var spectator = false;
var pause = true;

socket.onopen = function() {
	console.log('Send: hello as ' + uuid);
	socket.send('hello');
};

window.onkeydown = function(e) {
	if (e.keyCode === 87) Keys.up = true;
	else if (e.keyCode === 83) Keys.down = true;
};
window.onkeyup = function(e) {
	if (e.keyCode === 87) Keys.up = false;
	else if (e.keyCode === 83) Keys.down = false;
};

socket.onmessage = function(s) {
	var a = s.data.split('@');

	console.log("Received: ");
	console.log(a);

	if(a[0] === 'uuid') {
		uuid = a[1];
		players[a[1]] = {rectx:a[2], recty:a[3], width:20, height:80, score:0};
	}
	else if(a[0] === 'player'){
		if(players.hasOwnProperty(a[1])) {
			players[a[1]].rectx = a[2];
			players[a[1]].recty = a[3];
		} else {
			players[a[1]] = {rectx:a[2], recty:a[3], width:20, height:80, score:0 };
		}
	}
	else if(a[0] === 'mkbox') { // mkbox packet should be mkbox, id, width, height, xpos, ypos
		makeBox(a[1], a[2], a[3], a[4], a[5]);
	}
	else if(a[0] === 'mvbox') {
		moveBox(a[1], a[2], a[3]);
	}
	else if(a[0] === 'delbox'){
		if(boxes.hasOwnProperty(a[1])){
			console.log(boxes);
			delete(boxes[a[1]]);
		}
	}
	else if (a[0] === 'disconnect') {
		if (players.hasOwnProperty(a[1])) {
			delete(players[a[1]]);
		}
	}
	else if (a[0] === 'score') {
		players[a[1]].score = a[2];
	}
	else if (a[0] === 'spectator') {
		spectator = true;	
	} else if(a[0] === 'pause') {
		pause = true;
	} else if(a[0] === 'play') {
		pause = false;
	} else if(a[0] === 'cleanup') {
		boxes = {};
		players = {};
	}
};

let makeBox = function(id, width, height, xpos, ypos){
	boxes[id] = {w:width, h:height, x:xpos, y:ypos}
};

let moveBox = function(id, xpos, ypos) {
	boxes[id].x = xpos;
	boxes[id].y = ypos;
}

setInterval(function() {
	//console.log('Send: ping as ' + uuid);
//	socket.send('ping');
	ctx.clearRect(0, 0, canvas.width, canvas.height);
	ctx.beginPath();

	if(spectator === false) {
		if(Keys.up) {
			players[uuid.toString()].recty-=1;
			socket.send('up');
		}
		if(Keys.down) {
			players[uuid.toString()].recty +=1;
			socket.send('down');
		}
	}

	if(pause === false) {
		for (let key in players) {
			//if(players.hasOwnProperty(key)){
				ctx.rect(players[key].rectx, players[key].recty, players[key].width, players[key].height);
				ctx.stroke();
				ctx.font = "30px Arial";
				ctx.fillText(players[key].score, players[key].rectx, 25);
			//}
			
		}
		for (let key in boxes){
			ctx.rect(boxes[key].x, boxes[key].y, boxes[key].w, boxes[key].h);
			ctx.stroke();
		}
	} else {
		ctx.font = "30px Arial";
		ctx.fillText("Waiting for Opponent...", 50, 50);
	}
}, 20);

