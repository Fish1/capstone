var canvas = document.getElementById("canvas");
var ctx = canvas.getContext("2d");
var rectx = 20;
var recty = 20;
var Keys = {
    up: false,
    down: false,
    left: false,
    right: false
};

var uuid = 0;

//Change the ip to local host when testing
var socket = new WebSocket('ws://192.168.232.185:25565');

socket.onopen = function() {
	console.log('Send: hello as ' + uuid);
	socket.send('hello');
};

window.onkeydown = function(e) {
    if (e.keyCode == 87) Keys.up = true;
    else if (e.keyCode == 83) Keys.down = true;
    else if (e.keyCode == 68) Keys.right = true;
    else if (e.keyCode == 65) Keys.left = true;
};
window.onkeyup = function(e) {
    if (e.keyCode == 87) Keys.up = false;
    else if (e.keyCode == 83) Keys.down = false;
    else if (e.keyCode == 68) Keys.right = false;
    else if (e.keyCode == 65) Keys.left = false;
};

socket.onmessage = function(s) {
	var a = s.data.split('@');

	console.log("Received: ");
	console.log(a);

	if(a[0] === 'uuid') {
		uuid = parseInt(a[1]);
	}
};

setInterval(function() {
	//console.log('Send: ping as ' + uuid);
//	socket.send('ping');
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    ctx.beginPath();
	if(Keys.up) {
	    rectx-=1;
	    socket.send('up');
    }
	if(Keys.down) {
	    rectx +=1;
	    socket.send('down');
    }
	if(Keys.left) {
	    recty -=1;
	    socket.send('left')
    }
	if(Keys.right) {
	    recty +=1;
	    socket.send('right')
    }
    ctx.rect(recty, rectx, 150, 100);
    ctx.stroke();
}, 20);
