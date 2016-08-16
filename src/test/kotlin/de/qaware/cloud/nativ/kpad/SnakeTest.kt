package de.qaware.cloud.nativ.kpad

import org.junit.Test
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.TimeUnit
import javax.sound.midi.*

class SnakeTest {
    val snake = mutableListOf<Square>(Square(0, 0))
    val food = mutableListOf<Square>()
    val snakeColor = 53
    val foodColor = 17
    val random = Random()
    var direction = Direction.UP
    var score = 0

    val receiver = lazy {
        System.setProperty("javax.sound.midi.Receiver", "com.sun.media.sound.MidiOutDeviceProvider#Launchpad MK2")
        MidiSystem.getReceiver()
    }
    val transmitter = lazy {
        System.setProperty("javax.sound.midi.Transmitter", "com.sun.media.sound.MidiInDeviceProvider#Launchpad MK2")
        MidiSystem.getTransmitter()
    }

    @Test
    fun snakeGame() {
        init()
        transmitter.value.receiver = simpleReceiver { square ->
            val head = snake.last()
            direction = when(direction) {
                Direction.UP, Direction.DOWN -> if (square.col < head.col) Direction.LEFT else Direction.RIGHT
                Direction.RIGHT, Direction.LEFT -> if (square.row < head.row) Direction.DOWN else Direction.UP
            }
        }

        spawnFood()
        on(snake.last(), snakeColor)
        while(step()) {
            TimeUnit.MILLISECONDS.sleep(500L + score * 50)
        }
        snake.forEach { off(it) }
        food.forEach { off(it) }
        text("Game Over! Score: $score")
    }

    fun init() {
        for(i in 0..7) {
            for(j in 0..7) {
                off(Square(i, j))
            }
        }
    }

    private fun simpleReceiver(receiver : (Square) -> Unit): Receiver {
        return object : Receiver {
            override fun send(message: MidiMessage, timeStamp: Long) {
                if (message !is ShortMessage || message.data2 != 127)
                    return
                val row = message.data1 / 10 - 1
                val col = message.data1 - row * 10 - 11
                receiver(Square(row, col))
            }

            override fun close() {}
        }
    }

    fun step() : Boolean {
        val head = snake.last()
        val newHead = when(direction) {
            Direction.UP -> Square(head.row + 1, head.col)
            Direction.DOWN -> Square(head.row - 1, head.col)
            Direction.RIGHT -> Square(head.row, head.col + 1)
            Direction.LEFT -> Square(head.row, head.col - 1)
        }

        if(!valid(newHead)) {
            return false
        }

        snake.add(newHead)
        on(newHead, snakeColor)

        if(food.contains(newHead)) {
            score++
            spawnFood()
        } else {
            off(snake.removeAt(0))
        }

        return true
    }

    fun spawnFood() {
        var spawnLocation = Square(random.nextInt(8), random.nextInt(8))
        while (snake.contains(spawnLocation)) {
            spawnLocation = Square(random.nextInt(8), random.nextInt(8))
        }
        food.add(spawnLocation)
        on(spawnLocation, foodColor)
    }

    fun valid(square : Square) = !snake.contains(square)
            && 0.until(8).contains(square.row) && 0.until(8).contains(square.col)

    fun on(square : Square, color : Int) {
        receiver.value.send(ShortMessage(144, 0, 11 + square.col + 10 * square.row, color), -1)
    }

    fun off(square : Square) = on(square, 0)

    open fun text(text : String) {
        val preamble = byteArrayOf(240.toByte(), 0, 32, 41, 2, 24, 20)
        val bytes = text.toByteArray(Charset.defaultCharset())

        val data = ArrayList<Byte>()
        data.addAll(preamble.toList())
        data.add(5)
        data.add(0) // do not loop
        data.addAll(bytes.toList())
        data.add(247.toByte())

        receiver.value.send(SysexMessage(data.toByteArray(), data.size), -1)
    }

    data class Square (val row: Int, val col: Int)
    enum class Direction { UP, DOWN, LEFT, RIGHT }
}
