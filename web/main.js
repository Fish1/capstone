var canvas = document.getElementById("canvas");
var ctx = canvas.getContext("2d");

ctx.rect(20, 20, 150, 100);
ctx.stroke();
//Change the ip to local host when testing
var socket = new WebSocket('ws://192.168.215.57:25565');

socket.onopen = function() {
	console.log("hello there");
	socket.send('client send message :D');
}

socket.onmessage = function(s) {
	alert('got reply ' + s.data);
}

