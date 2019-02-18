var canvas = document.getElementById("canvas");
var ctx = canvas.getContext("2d");

ctx.rect(20, 20, 150, 100);
ctx.stroke();

var socket = new WebSocket('ws://localhost:8000');

socket.onopen = function() {
	socket.send('client send message :D');
}

socket.onmessage = function(s) {
	alert('got reply ' + s.data);
}

