var canvas = document.getElementById("canvas");
var ctx = canvas.getContext("2d");
var players = [];
players.push({uuid:0,rectx:20,recty:20, width:40, height:40});
var Keys = {
    up: false,
    down: false,
    left: false,
    right: false
};

//Change the ip to local host when testing
var socket = new WebSocket('ws://192.168.232.185:25565');

socket.onopen = function() {
	console.log('Send: hello as ' + players[0].uuid);
	socket.send('hello');
};

window.onkeydown = function(e) {
    if (e.keyCode === 87) Keys.up = true;
    else if (e.keyCode === 83) Keys.down = true;
    else if (e.keyCode === 68) Keys.right = true;
    else if (e.keyCode === 65) Keys.left = true;
};
window.onkeyup = function(e) {
    if (e.keyCode === 87) Keys.up = false;
    else if (e.keyCode === 83) Keys.down = false;
    else if (e.keyCode === 68) Keys.right = false;
    else if (e.keyCode === 65) Keys.left = false;
};

socket.onmessage = function(s) {
	var a = s.data.split('@');

	console.log("Received: ");
	console.log(a);

	if(a[0] === 'uuid') {
		players[0].uuid = parseInt(a[1]);
	}
};

let collision = new function(x1,x2,y1,y2,w1,w2,h1,h2){
    return (x1<x2+w2 &&
            x1+w1>x2 &&
            y1<y2+h2 &&
            y1+h1>y2)
};

setInterval(function() {
	//console.log('Send: ping as ' + uuid);
//	socket.send('ping');
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    ctx.beginPath();
	if(Keys.up) {
	    players[0].rectx-=1;
	    socket.send('up');
    }
	if(Keys.down) {
        players[0].rectx +=1;
	    socket.send('down');
    }
	if(Keys.left) {
        players[0].recty -=1;
	    socket.send('left')
    }
	if(Keys.right) {
        players[0].recty +=1;
	    socket.send('right')
    }
	for (var i=0; i < players.length; i++) {
        ctx.rect(players[i].recty, players[i].rectx, players[i].width, players[i].height);
        ctx.stroke();
    }
}, 20);
