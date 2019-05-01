package capstone

import kotlin.math.abs
import io.ktor.http.cio.websocket.Frame
import kotlinx.coroutines.channels.SendChannel

abstract class Rectangle{
	var m_posX: Double = 0.0
	var m_posY: Double = 0.0
	var m_width: Double = 0.0
	var m_height: Double = 0.0

	fun collides(rect: Rectangle): Boolean {
		return (this.m_posX <= rect.m_posX + rect.m_width
				&& this.m_posX + this.m_width >= rect.m_posX
				&& this.m_posY <= rect.m_posY + rect.m_height
				&& this.m_posY + this.m_height >= rect.m_posY)
	}
	fun collideSide(rect: Rectangle): Char{
		val holdX = (rect.m_posX + rect.m_width/2) - (this.m_posX + this.m_width/2)
		val holdY = (rect.m_posY + rect.m_height/2) - (this.m_posY + this.m_height/2)
		val holdWidth = .5 * (rect.m_width + this.m_width)
		val holdHeight =.5 * (rect.m_height + this.m_height)
		val holdWidthY = holdWidth * holdY
		val holdHeightX = holdHeight * holdX

		if(holdHeightX <= holdWidthY){
			if(holdWidthY >= -holdHeightX){//top
				return 'u'
			}
			else{//right
				return 'r'
			}
		}
		else{
			if(holdWidthY >= -holdHeightX){//left
				return 'l'
			}
			else{//botton
				return 'd'
			}
		}

	}
}

class Block(posX: Double, posY: Double, width: Double, height: Double, delete: Boolean, id: Int): Rectangle(){
	var m_delete : Boolean
	var m_id : Int

	init {
		this.m_posX = posX
		this.m_posY = posY
		this.m_width = width
		this.m_height = height
		this.m_delete = delete
		this.m_id = id
	}
}

class Ball(posX: Double, posY: Double, width: Double, height: Double, id: Int, moveX: Double, moveY: Double): Rectangle(){
	var m_id: Int
	var m_moveX: Double
	var m_moveY: Double
	var m_orgMoveX: Double
	var m_orgMoveY : Double

	init {
		this.m_posX = posX
		this.m_posY = posY
		this.m_width = width
		this.m_height = height
		this.m_id = id
		this.m_moveX = moveX
		this.m_moveY = moveY
		this.m_orgMoveX = moveX
		this.m_orgMoveY = m_moveY
	}

	fun reset() {
		if(this.m_posX > 360) {
			this.m_posX = (720.0 * (2.0/3.0))
		} else {
			this.m_posX = (720.0 * (1.0/3.0))
		}

		m_posY = 480.0 / 2.0

		if((0..1).random() == 0) {
			this.m_moveX = -this.m_orgMoveX
		} else {
			this.m_moveX = this.m_orgMoveX
		}

		if((0..1).random() == 0) {
			this.m_moveY = -this.m_orgMoveY
		} else {
			this.m_moveY = this.m_orgMoveY
		}
	}

	suspend fun update() {
		m_posX += m_moveX
		m_posY += m_moveY
		var hitLeft = false
		var hitRight = false

		if(m_posX < 0 || m_posX > 720 - this.m_width) {
			m_moveX *= -1.0
		}

		if(m_posY < 0 || m_posY > 480 - this.m_height) {
			m_moveY *= -1.0
		}

		if(m_posX < 0) {
			reset()
			hitLeft = true
		} else if(m_posX > 720 - this.m_width) {
			hitRight = true
			reset()
		}

		if(hitLeft){
			for((_, user) in users){
				if(user.m_posX > 360.0){
					user.addScore()
				}
			}
			if(isAI && ai.m_posX > 360.0){
				ai.addScore()
			}
		}
		if(hitRight){
			for((_, user) in users){
				if(user.m_posX < 360.0){
					user.addScore()
				}
			}
			if(isAI && ai.m_posX < 360.0){
				ai.addScore()
			}
		}
	}
}

class User(uuid: Int, outgoing: SendChannel<Frame>): Rectangle() {
	var m_uuid: Int
	var m_outgoing: SendChannel<Frame>

	var m_score: Int

	init {
		this.m_posX = 20.0
		this.m_posY = 200.0
		this.m_height = 80.0
		this.m_width = 20.0
		this.m_uuid = uuid
		this.m_outgoing = outgoing
		this.m_score = 0
	}

	suspend fun addScore() {
		this.m_score += 1
		broadcast("score@${this.m_uuid}@${this.m_score}")
	}
}

class AI(uuid: Int):Rectangle(){
	var m_uuid: Int

	var m_score: Int

	init {
		this.m_posX = 20.0
		this.m_posY = 200.0
		this.m_height = 80.0
		this.m_width = 20.0
		this.m_uuid = uuid
		this.m_score = 0
	}

	suspend fun addScore() {
		this.m_score += 1
		broadcast("score@${this.m_uuid}@${this.m_score}")
	}

	fun update(balls:List<Ball>){
		var closeX = 1000.0
		var closeY = 0.0
		balls.forEach {
			val hold = abs(it.m_posX - this.m_posX)
			if(hold < closeX){
				closeX = hold
				closeY = it.m_posY
			}
		}
		if (closeY < this.m_posY ) {
			this.m_posY -= 6
		}
		else if(closeY > this.m_posY + this.m_height){
			this.m_posY += 6
		}

		if(this.m_posY <= 0.0){
			this.m_posY = 0.0
		}
		if(this.m_posY >= 480-this.m_height){
			this.m_posY = 480-this.m_height
		}

	}
}
