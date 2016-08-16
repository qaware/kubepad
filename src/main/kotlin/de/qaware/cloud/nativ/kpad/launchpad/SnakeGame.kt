/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 QAware GmbH, Munich, Germany
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.qaware.cloud.nativ.kpad.launchpad

import de.qaware.cloud.nativ.kpad.launchpad.LaunchpadMK2.Square
import org.slf4j.Logger
import java.util.*
import java.util.concurrent.TimeUnit
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.event.Event
import javax.enterprise.event.Observes
import javax.enterprise.util.AnnotationLiteral
import javax.inject.Inject

/**
 * This class takes control over the launchpad and lets the user play a game of snake.
 * After the game has finished the application exits.
 */
@ApplicationScoped
open class SnakeGame @Inject constructor(private val launchpad: Event<LaunchpadEvent>,
                                         private val logger: Logger) : Runnable {

    private val snake = mutableListOf<Square>()
    private val food = mutableListOf<Square>()
    private val random = Random()

    private var direction = Direction.UP
    private var score = 0
    private var running = false
    open fun running() = running //private set does not work in this case because it creates a final getter

    private val snakeColor = LaunchpadMK2.Color.PURPLE
    private val foodColor = LaunchpadMK2.Color.LIGHT_GREEN

    open fun init() {
        resetLaunchpad()
        resetButtonOn()
        snake.clear()
        snake.add(Square(0, 3))
        on(Square(0, 3), snakeColor)
        food.clear()
        spawnFood()
        direction = Direction.UP
        score = 0
    }


    override fun run() {
        running = true
        init()
        while (step()) {
            if (!running) {
                return
            }
            TimeUnit.MILLISECONDS.sleep(500L + score * 50)
        }
        logger.info("Game Over! Score: {}", score)
        text("Game Over! Score: $score")
    }

    private fun onSquarePressed(@Observes @SwitchableEvent.Pressed event: SwitchableEvent) {
        if (running && event.switchable == LaunchpadMK2.Button.USER_2) {
            running = false
        }

        if (!running || event.switchable !is Square) {
            return
        }

        val square = event.switchable
        logger.debug("Game: Square pressed: {}", square)
        val head = snake.last()
        direction = when (direction) {
            Direction.UP, Direction.DOWN -> if (square.column < head.column) Direction.LEFT else Direction.RIGHT
            Direction.RIGHT, Direction.LEFT -> if (square.row < head.row) Direction.DOWN else Direction.UP
        }
    }

    private fun step(): Boolean {
        val head = snake.last()
        val newHead = when (direction) {
            Direction.UP -> Square(head.row + 1, head.column)
            Direction.DOWN -> Square(head.row - 1, head.column)
            Direction.RIGHT -> Square(head.row, head.column + 1)
            Direction.LEFT -> Square(head.row, head.column - 1)
        }

        if (!valid(newHead)) {
            return false
        }

        snake.add(newHead)
        on(newHead, snakeColor)

        if (food.contains(newHead)) {
            score++
            spawnFood()
        } else {
            off(snake.removeAt(0))
        }

        return true
    }

    private fun spawnFood() {
        var spawnLocation = Square(random.nextInt(8), random.nextInt(8))
        while (snake.contains(spawnLocation)) {
            spawnLocation = Square(random.nextInt(8), random.nextInt(8))
        }
        food.add(spawnLocation)
        on(spawnLocation, foodColor)
    }

    private fun valid(square: Square) = !snake.contains(square)
            && 0.until(8).contains(square.row) && 0.until(8).contains(square.column)

    private fun on(square: Square, color: LaunchpadMK2.Color) {
        val event = LaunchpadEvent.light(LaunchpadEvent.Switch.ON, square, color)
        launchpad.select(object : AnnotationLiteral<LaunchpadEvent.Light>() {}).fire(event)
    }

    private fun resetButtonOn() {
        val event = LaunchpadEvent.light(LaunchpadEvent.Switch.ON, LaunchpadMK2.Button.USER_2, LaunchpadMK2.Color.YELLOW)
        launchpad.select(object : AnnotationLiteral<LaunchpadEvent.Light>() {}).fire(event)
    }

    private fun off(square: Square) = on(square, LaunchpadMK2.Color.NONE)

    private fun text(text: String) {
        val event = LaunchpadEvent.text(text, LaunchpadMK2.Color.RED)
        launchpad.select(object : AnnotationLiteral<LaunchpadEvent.Text>() {}).fire(event)
    }

    private fun resetLaunchpad() {
        val event = LaunchpadEvent.reset()
        launchpad.select(object : AnnotationLiteral<LaunchpadEvent.Reset>() {}).fire(event)
    }

    private enum class Direction { UP, DOWN, LEFT, RIGHT }
}