/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package capstone

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

import io.ktor.websocket.*
import io.ktor.http.cio.websocket.*
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame

import java.io.File

class App {
    val greeting: String
        get() {
            return "Hello world."
        }
}

var uuid = 0;

fun main() {
	val server = embeddedServer(Netty, port = 25565) {
	
		install(WebSockets);

		routing {
			get("/") {
				val file = File("web/index.html");
				call.respondText(file.readText(), ContentType.Text.Html);

			}

			get("/main.js") {
				val file = File("web/main.js");
				call.respondText(file.readText(), ContentType.Text.Plain);
			}

			webSocket("/") {
				var t_uuid = uuid;
				uuid += 1;
				while(true) {
					val frame = incoming.receive()
					when(frame) {
						is Frame.Text -> {
							val text = frame.readText();
							println("Receive: " + text + " from " + t_uuid.toString());
							if(text == "ping") {
								println("Send: pong " + t_uuid.toString());
								outgoing.send(Frame.Text("pong"));
							} else if(text == "hello") {
								println("Send: welcome");
								outgoing.send(Frame.Text(t_uuid.toString()));
							}
						}
						is Frame.Ping -> {
							println("Pingggg");
						}
						is Frame.Pong -> {
							println("Pongggg");
						}
					}
				}
			}
		}
	}

	server.start(wait = true);
}
