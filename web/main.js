var canvas = document.getElementById("canvas");
var ctx = canvas.getContext("2d");
var players = {};
var uuid = 0;
var Keys = {
    up: false,
    down: false,
    left: false,
    right: false
};

//Change the ip to local host when testing
//jacop = 192.168.232.185
//james = 192.168.215.57
var socket = new WebSocket('ws://192.168.232.185:25565');

socket.onopen = function() {
	console.log('Send: hello as ' + uuid);
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
	    uuid = a[1];
		players[uuid.toString()] = {rectx:0, recty:0, width:40, height:40};
	}
	else if(a[0] === 'player'){
	    if(players[a[1].toString()] === null){
	        players[a[1].toString()] = {rectx:a[2], recty:a[3], width:40, height:40 };
        }
	    else{
            players[a[1].toString()].rectx = a[2];
            players[a[1].toString()].recty = a[3];
        }
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
	    players[uuid.toString()].rectx-=1;
	    socket.send('up');
    }
	if(Keys.down) {
        players[uuid.toString()].rectx +=1;
	    socket.send('down');
    }
	if(Keys.left) {
        players[uuid.toString()].recty -=1;
	    socket.send('left')
    }
	if(Keys.right) {
        players[uuid.toString()].recty +=1;
	    socket.send('right')
    }
	for (var key in players) {
        if(players.hasOwnProperty(key)){
    	    ctx.rect(players[key].recty, players[key].rectx, players[key].width, players[key].height);
            ctx.stroke();
        }
    }
}, 20);

