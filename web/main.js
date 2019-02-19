var canvas = document.getElementById("canvas");
var ctx = canvas.getContext("2d");

ctx.rect(20, 20, 150, 100);
ctx.stroke();
var uuid = 0;

//Change the ip to local host when testing
var socket = new WebSocket('ws://192.168.232.185:25565');

socket.onopen = function() {
	console.log('Send: hello as ' + uuid);
	socket.send('hello');
}

socket.onmessage = function(s) {
	var a = s.data.split('@');

	console.log("Received: ");
	console.log(a);

	if(a[0] === 'uuid') {
		uuid = parseInt(a[1]);
	}
}

setInterval(function() {
	console.log('Send: ping as ' + uuid);
	socket.send('ping');
	socket.send('d');
}, 500);
