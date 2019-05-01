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
import io.ktor.http.cio.websocket.Frame

import kotlinx.coroutines.channels.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.GlobalScope

import java.io.File

class App {
	val greeting: String
		get() {
			return "Hello world."
		}
}

var uuid = 0

val users = HashMap<Int, User>()

val spectators = HashMap<Int, User>()

val blocks: MutableList<Block> = mutableListOf()

val balls: MutableList<Ball> = mutableListOf()

var isAI: Boolean = false

var ai:AI = AI(-1)

suspend fun broadcast(data: String) {
	//println("Sending: " + data)
	for((_, value) in users) {
		try {
			value.m_outgoing.send(Frame.Text(data))
		} catch (t : Throwable) {

		}
	}
	for((_, value) in spectators) {
		try {
			value.m_outgoing.send(Frame.Text(data))
		} catch (t : Throwable) {

		}
	}
}

suspend fun checkCollision() {

	val removeBlocks: MutableList<Block> = mutableListOf()

	for(ball in balls) {
		for(block in blocks) {
			if(ball.collides(block)) {
				removeBlocks.add(block)
				val side = ball.collideSide(block)
				when (side){
					'u' ->{
						if(ball.m_moveY > 0) {
							ball.m_moveY *= -1
						}
					}
					'd' ->{
						if(ball.m_moveY < 0){
							ball.m_moveY *= -1
						}
					}
					'r' ->{
						if(ball.m_moveX < 0){
							ball.m_moveX *= -1
						}
					}
					'l' ->{
						if(ball.m_moveX > 0){
							ball.m_moveX *= -1
						}
					}
				}
			}
		}
		for((_, user) in users) {
			if(user.collides(ball)) {
				val side = ball.collideSide(user)
				when (side){
					'u' ->{
						if(ball.m_moveY > 0) {
							ball.m_moveY *= -1
						}
					}
					'd' ->{
						if(ball.m_moveY < 0){
							ball.m_moveY *= -1
						}
					}
					'r' ->{
						if(ball.m_moveX < 0){
							ball.m_moveX *= -1
						}
					}
					'l' ->{
						if(ball.m_moveX > 0){
							ball.m_moveX *= -1
						}
					}
				}

				ball.m_moveX *= (1.0 + kotlin.random.Random.nextDouble() * 0.05)
				ball.m_moveY *= (1.0 + kotlin.random.Random.nextDouble() * 0.05)
			}
		}
		if(isAI){
			if(ai.collides(ball)){
				val side = ball.collideSide(ai)
				when (side){
					'u' ->{
						if(ball.m_moveY > 0) {
							ball.m_moveY *= -1
						}
					}
					'd' ->{
						if(ball.m_moveY < 0){
							ball.m_moveY *= -1
						}
					}
					'r' ->{
						if(ball.m_moveX < 0){
							ball.m_moveX *= -1
						}
					}
					'l' ->{
						if(ball.m_moveX > 0){
							ball.m_moveX *= -1
						}
					}
				}

				ball.m_moveX *= (1.0 + kotlin.random.Random.nextDouble() * 0.05)
				ball.m_moveY *= (1.0 + kotlin.random.Random.nextDouble() * 0.05)
			}
		}

		broadcast("mvbox@${ball.m_id}@${ball.m_posX}@${ball.m_posY}")
	}

	for(block in removeBlocks) {
		blocks.remove(block)
		broadcast("delbox@${block.m_id}")
	}
}

suspend fun broadcastPlayers(){
	for((key, value)in users) {
		broadcast("player@$key@${value.m_posX}@${value.m_posY}")
	}
	if(isAI){
		broadcast("player@${ai.m_uuid}@${ai.m_posX}@${ai.m_posY}")
	}
}

fun setup() {
	blocks.clear()
	var posHold = 0
	var blockID = 0
	while (posHold < 480) {
		var block = Block(350.0, (posHold).toDouble(), 10.0, 10.0, false, blockID)
		blockID++
		blocks.add(block)
		block = Block(360.0, (posHold).toDouble(), 10.0, 10.0, false, blockID)
		blockID++
		blocks.add(block)
		posHold += 10
	}

	balls.clear()
	balls.add(Ball(480.0/2.0, 240.0, 10.0, 10.0, -1, -2.0, -2.0))
	balls.add(Ball(720.0-240.0,  240.0, 10.0, 10.0, -2, 2.0, 2.0))
	
	for((_, value)in users) {
		value.m_score = 0
	}
}

suspend fun sendSetupData() {
	broadcast("cleanup")
	broadcastPlayers()

	for (block in blocks) {
		broadcast("mkbox@${block.m_id}@${block.m_width}@${block.m_height}@${block.m_posX}@${block.m_posY}")
	}

	for(ball in balls) {
		broadcast("mkbox@${ball.m_id}@${ball.m_width}@${ball.m_height}@${ball.m_posX}@${ball.m_posY}")
	}
}

fun main() {
	setup()

	val server = embeddedServer(Netty, port = 25565) {

		install(WebSockets)

		routing {

			get("/") {
				val file = File("web/index.html")
				call.respondText(file.readText(), ContentType.Text.Html)
			}

			get("/main.js") {
				val file = File("web/main.js")
				call.respondText(file.readText(), ContentType.Text.Plain)
			}

			webSocket("/") {
				val t_uuid = uuid
				var isSpectator = false
				uuid += 1
				val user = User(t_uuid, outgoing)
				if (users.size < 2) {
					users.forEach { _, it ->
						if (it.m_posX < 360.0) {
							user.m_posX = 700.0 - user.m_width
						}
					}
					users.put(t_uuid, user)
				} else {
					spectators.put(t_uuid, user)
					isSpectator = true
					outgoing.send(Frame.Text("spectator"))
				}

				broadcastPlayers()
				if(isAI){
					isAI = false
					broadcast("disconnect@${ai.m_uuid}@${ai.m_posX}@${ai.m_posY}")
					user.m_posX = ai.m_posX
					user.m_posY = ai.m_posY
					user.m_score = ai.m_score
				}

				var blockIndex = 0
				for (block in blocks) {
					outgoing.send(Frame.Text("mkbox@${block.m_id}@${block.m_width}@${block.m_height}@${block.m_posX}@${block.m_posY}"))
					++blockIndex
				}

				for (ball in balls) {
					outgoing.send(Frame.Text("mkbox@${ball.m_id}@${ball.m_width}@${ball.m_height}@${ball.m_posX}@${ball.m_posY}"))
				}

//				sendSetupData();

				try {
					while (true) {
						val frame = incoming.receive()

						if (isSpectator) {
							continue
						}

						when (frame) {
							is Frame.Text -> {
								val text = frame.readText()
								println("Receive: $text from $t_uuid")
								when (text) {
									"hello" -> {
										outgoing.send(Frame.Text("uuid@$t_uuid@${user.m_posX}@${user.m_posY}"))
									}
									"up" -> {
										user.m_posY -= 6.0
										broadcast("player@$t_uuid@${user.m_posX}@${user.m_posY}")
									}
									"down" -> {
										user.m_posY += 6.0
										broadcast("player@$t_uuid@${user.m_posX}@${user.m_posY}")
									}
									"ai" -> {
										if (users.size == 1 && !isAI) {
											isAI = true
											ai = AI(-1)
											val side = user.m_posX
											if (side < 360) {
												ai.m_posX = 700.0 - user.m_width
											}
											broadcast("player@${ai.m_uuid}@${ai.m_posX}@${ai.m_posY}")
										}
									}
								}

								if (user.m_posY < 0.0) {
									user.m_posY = 0.0
								} else if (user.m_posY > 480.0 - user.m_height) {
									user.m_posY = 480.0 - user.m_height
								}
							}
						}
					}
				} catch (e: ClosedReceiveChannelException) {
					println("onClose ${closeReason.await()}")
					users.remove(t_uuid)
					broadcast("disconnect@$t_uuid@${user.m_posX}@${user.m_posY}")
				}
			}
		}
	}


	server.start(wait = false)

	GlobalScope.launch {
		var paused = false
		while (true) {
			Thread.sleep(1000 / 60)

			if (users.size < 2 && !isAI) {
				if (!paused) {
					setup()
					sendSetupData()
				}
				broadcast("pause")
				paused = true
				continue
			} else {
				broadcast("play")
				paused = false
			}

			checkCollision()

			for (ball in balls) {
				ball.update()
			}
			if(isAI){
				ai.update(balls)
				broadcastPlayers()
			}
		}
	}
}
